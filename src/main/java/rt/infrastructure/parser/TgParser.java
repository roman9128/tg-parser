package rt.infrastructure.parser;

import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import rt.infrastructure.config.AppPropertiesHandler;
import rt.infrastructure.config.ParsingPropertiesChanger;
import rt.infrastructure.config.ParsingPropertiesHandler;
import rt.infrastructure.notifier.Notifier;
import rt.model.service.NoteStorageService;
import rt.model.service.ParserService;
import rt.service_manager.ErrorInformer;
import rt.service_manager.InteractionStarter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class TgParser implements ParserService {

    private final SimpleTelegramClient client;
    private final ChatHistoryLoader chatHistoryLoader = new ChatHistoryLoader();
    private final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, String> foldersInfo = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, long[]> chatsInFolders = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<>();
    private final NoteStorageService storage;
    private final InteractionStarter starter;
    private final ParsingPropertiesChanger parsingPropertiesChanger = new ParsingPropertiesChanger();
    private final ExecutorService blockingExecutor = Executors.newSingleThreadExecutor();
    private final ErrorHandler errorHandler;

    public TgParser(SimpleTelegramClientFactory clientFactory,
                    NoteStorageService storage,
                    InteractionStarter starter,
                    ErrorInformer errorInformer) {

        this.storage = storage;
        this.starter = starter;
        this.errorHandler = new ErrorHandler(errorInformer);

        APIToken apiToken = new APIToken(AppPropertiesHandler.getApiID(), AppPropertiesHandler.getApiHash());
        TDLibSettings settings = TDLibSettings.create(apiToken);
        Path sessionPath = Paths.get("session");
        settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
        SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);
        SimpleTelegramClient client = clientBuilder.build(AuthenticationSupplier.qrCode());
        client.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        client.addUpdateHandler(TdApi.UpdateNewChat.class, this::onUpdateChat);
        client.addUpdateHandler(TdApi.UpdateSupergroup.class, this::onUpdateSuperGroup);
        client.addUpdateHandler(TdApi.UpdateChatFolders.class, this::onUpdateFolder);
        client.setClientInteraction(null);
        this.client = client;
    }

    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        TdApi.AuthorizationState authorizationState = update.authorizationState;
        switch (authorizationState) {
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation deviceConfirmation -> {
                String link = deviceConfirmation.link;
                starter.showQrCode(link);
            }
            case TdApi.AuthorizationStateWaitPassword waitPassword -> {
                String password = starter.ask2FAPassword();
                client.send(new TdApi.CheckAuthenticationPassword(password), errorHandler);
            }
            case TdApi.AuthorizationStateReady ready -> {
                getChats();
                getChannels();
                getFoldersInfo();
                stopBlockingExecutor();
                starter.startInteractions();
            }
            case TdApi.AuthorizationStateLoggingOut loggingOut -> {
                Notifier.getInstance().addNotification("Не авторизован");
                SessionEraser.deleteSession();
            }
            default -> {
            }
        }
    }

    private void onUpdateChat(TdApi.UpdateNewChat updateNewChat) {
        TdApi.Chat chat = updateNewChat.chat;
        chats.put(chat.id, chat);
    }

    private void onUpdateSuperGroup(TdApi.UpdateSupergroup updateSupergroup) {
        TdApi.Supergroup supergroup = updateSupergroup.supergroup;
        if (supergroup.status.getConstructor() == TdApi.ChatMemberStatusMember.CONSTRUCTOR && supergroup.isChannel) {
            supergroups.put(transferChatID(supergroup.id), supergroup);
        }
    }

    private void onUpdateFolder(TdApi.UpdateChatFolders updateChatFolders) {
        for (TdApi.ChatFolderInfo folder : updateChatFolders.chatFolders) {
            foldersInfo.put(folder.id, folder.title.replaceAll("[^\\p{L}\\p{N} ]", ""));
        }
    }

    private void getChats() {
//        client.send(new TdApi.LoadChats());
        client.send(new TdApi.GetChats());
    }

    private void getChannels() {
        client.send(new TdApi.GetSupergroup());
    }

    private void getFoldersInfo() {
        client.send(new TdApi.GetChatFolder());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Notifier.getInstance().addNotification(e.getMessage());
            }
            getChatsFromFolders();
        });
        executor.shutdown();
    }

    private void getChatsFromFolders() {
        for (int folderID : foldersInfo.keySet()) {
            client.send(new TdApi.GetChatFolder(folderID)).whenCompleteAsync((folder, error) -> {
                if (error != null) {
                    Notifier.getInstance().addNotification("Ошибка при получении содержимого папок: " + error.getMessage());
                } else {
                    chatsInFolders.put(folderID, folder.includedChatIds);
                }
            });
        }
    }

    @Override
    public Map<Integer, String> getFoldersIDsAndNames() {
        return new LinkedHashMap<>(foldersInfo);
    }

    @Override
    public Set<Long> getChatsInFolder(Integer folderID) {
        if (!foldersInfo.containsKey(folderID)) {
            return new TreeSet<>();
        } else {
            return Arrays.stream(chatsInFolders.get(folderID))
                    .filter(this::isSupergroupInChats)
                    .boxed()
                    .collect(Collectors.toCollection(TreeSet::new));
        }
    }

    @Override
    public Map<Long, String> getChannelsIDsAndNames() {
        return supergroups.keySet().stream().collect(Collectors.toMap(
                key -> key,
                key -> chats.get(key).title.replaceAll("[^\\p{L}\\p{N} ]", "")));
    }

    @Override
    public void loadChannelsHistory(Set<Long> channelsIDs, Long dateFromUnix, Long dateToUnix) {
        Long dateNowUnix = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        if (dateFromUnix >= dateToUnix) {
            Notifier.getInstance().addNotification("Вторая дата не может быть больше первой");
            return;
        }
        if (dateFromUnix > dateNowUnix) {
            Notifier.getInstance().addNotification("Этот день ещё не наступил");
            return;
        }

        chatHistoryLoader.setDateFromUnix(dateFromUnix);
        chatHistoryLoader.setDateToUnix(dateToUnix);

        if (channelsIDs.isEmpty()) {
            Notifier.getInstance().addNotification("Начинаю загрузку со всех каналов");
            for (Long chatID : supergroups.keySet()) {
                loadChatHistory(chatID, dateFromUnix);
            }
        } else {
            Notifier.getInstance().addNotification("Начинаю загрузку сообщений из указанных каналов");
            for (Long chatID : channelsIDs) {
                if (isSupergroupInChats(chatID)) {
                    loadChatHistory(chatID, dateFromUnix);
                }
            }
        }
        chatHistoryLoader.removeSurplus();
        prepareNotes();
        Notifier.getInstance().addNotification("Всего загружено " + storage.getAllNotesQuantity() + " сообщ., соотв. заданным параметрам");
    }

    private void loadChatHistory(Long channelID, Long dateFromUnix) {
        Notifier.getInstance().addNotification("Загружаю сообщения из " + chats.get(channelID).title);
        int messagesLeft = ParsingPropertiesHandler.getMessagesToDownload();
        int messagesToStop = ParsingPropertiesHandler.getMessagesToStop();
        long fromMessageID = 0;
        while (true) {
            client.send(
                    new TdApi.GetChatHistory(channelID, fromMessageID, 0, 50, false),
                    chatHistoryLoader);
            try {
                Thread.sleep(Randomizer.giveNumber());
            } catch (InterruptedException e) {
                Notifier.getInstance().addNotification(e.getMessage());
            }
            if (!chatHistoryLoader.isEmpty()) {
                fromMessageID = chatHistoryLoader.getLastMessageID();
                messagesLeft -= chatHistoryLoader.getCountArrived();
                messagesToStop -= chatHistoryLoader.getCountArrived();
            }
            if (chatHistoryLoader.getCountArrived() == 0) {
                break;
            }
            if (messagesToStop < 1) {
                break;
            }
            if (dateFromUnix != 0L) {
                if (channelID.equals(chatHistoryLoader.getLastMessageChatID())
                        && chatHistoryLoader.getLastMessageDate() <= dateFromUnix) {
                    break;
                }
            } else {
                if (messagesLeft < 1) {
                    break;
                }
            }
        }
        Notifier.getInstance().addNotification("Загрузка сообщений из " + chats.get(channelID).title + " закончена");
        chatHistoryLoader.zeroCounter();
    }

    private Long transferChatID(Long chatID) {
        return -(1000000000000L + chatID);
    }

    private boolean isSupergroupInChats(Long chatID) {
        return supergroups.containsKey(chatID) && supergroups.get(chatID).status.getConstructor() == TdApi.ChatMemberStatusMember.CONSTRUCTOR;
    }

    @Override
    public void logout() {
        client.send(new TdApi.LogOut()).thenAccept(ok -> {
            Notifier.getInstance().addNotification("Вышел из аккаунта");
        });
    }

    @Override
    public void close() {
        client.sendClose();
    }

    @Override
    public void waitForExit() throws InterruptedException {
        client.waitForExit();
    }

    private void stopBlockingExecutor() {
        blockingExecutor.shutdownNow();
    }

    private void prepareNotes() {
        while (!chatHistoryLoader.isEmpty()) {
            TdApi.Message message = chatHistoryLoader.takeMessage();
            String senderName = chats.get(message.chatId).title;
            String link = getMsgLink(message);
            storage.createNote(message, senderName, link);
        }
        storage.removeCopies();
    }

    private String getMsgLink(TdApi.Message message) {
        try {
            return client.send(new TdApi.GetMessageLink(
                            message.chatId,
                            message.id,
                            0,
                            true,
                            true))
                    .get().link;
        } catch (Exception e) {
            return "Не удалось получить ссылку";
        }
    }

    @Override
    public void setMessagesToDownload(int value) {
        parsingPropertiesChanger.setMessagesAmountToDownloadFromOneChannelIfStopDateIsNotSet(value);
    }
}