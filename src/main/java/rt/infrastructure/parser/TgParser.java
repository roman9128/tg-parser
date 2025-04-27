package rt.infrastructure.parser;

import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import rt.model.entity.*;
import rt.infrastructure.config.PropertyHandler;
import rt.model.service.ParserService;
import rt.model.service.NoteStorageService;
import rt.presenter.parser.ServiceHelper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TgParser implements ParserService {

    private final SimpleTelegramClient client;
    private final ServiceHelper helper;
    private final ChatHistoryLoader chatHistoryLoader = new ChatHistoryLoader();
    private final ExecutorService blockingExecutor = Executors.newSingleThreadExecutor();
    private final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, String> foldersInfo = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, long[]> chatsInFolders = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<>();
    private final NoteStorageService storage;
    private volatile boolean isReadyToLoadNewChannels = true;

    public TgParser(SimpleTelegramClientBuilder clientBuilder,
                    PhoneAuthentication authenticationData,
                    NoteStorageService storage, ServiceHelper helper) {
        this.storage = storage;
        this.helper = helper;
        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        clientBuilder.addUpdateHandler(TdApi.UpdateNewChat.class, this::onUpdateChat);
        clientBuilder.addUpdateHandler(TdApi.UpdateSupergroup.class, this::onUpdateSuperGroup);
        clientBuilder.addUpdateHandler(TdApi.UpdateChatFolders.class, this::onUpdateFolder);
        this.client = clientBuilder.build(authenticationData);
        client.setClientInteraction(new ClientInteractionImpl(blockingExecutor, client, helper));
    }

    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        TdApi.AuthorizationState authorizationState = update.authorizationState;
        if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            helper.print("Авторизован", true);
            helper.print("Начинаю загрузку чатов, каналов, папок", true);
            getChats();
            getChannels();
            getFoldersInfo();
            stopBlockingExecutor();
            helper.startInteractions();
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            helper.print("Соединение закрыто", true);
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            helper.print("Не авторизован", true);
        }
    }

    private void onUpdateChat(TdApi.UpdateNewChat updateNewChat) {
        TdApi.Chat chat = updateNewChat.chat;
        chats.put(chat.id, chat);
    }

    private void onUpdateSuperGroup(TdApi.UpdateSupergroup updateSupergroup) {
        TdApi.Supergroup supergroup = updateSupergroup.supergroup;
        if (isReadyToLoadNewChannels && supergroup.isChannel) {
            supergroups.put(transferChatID(supergroup.id), supergroup);
        }
    }

    private void onUpdateFolder(TdApi.UpdateChatFolders updateChatFolders) {
        for (TdApi.ChatFolderInfo folder : updateChatFolders.chatFolders) {
            foldersInfo.put(folder.id, folder.title);
        }
    }

    private void getChats() {
        client.send(new TdApi.LoadChats());
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
                helper.print(e.getMessage(), true);
            }
            getChatsFromFolders();
        });
        executor.shutdown();
    }

    private void getChatsFromFolders() {
        for (int folderID : foldersInfo.keySet()) {
            client.send(new TdApi.GetChatFolder(folderID)).whenCompleteAsync((folder, error) -> {
                if (error != null) {
                    helper.print("Ошибка при получении содержимого папок: " + error.getMessage(), true);
                } else {
                    chatsInFolders.put(folderID, folder.includedChatIds);
                }
            });
        }
    }

    @Override
    public void show() {
        stopLoadingNewChannels();
        StringBuilder builder = new StringBuilder();
        for (int folderID : foldersInfo.keySet()) {
            builder
                    .append(folderID)
                    .append(": ")
                    .append(foldersInfo.get(folderID))
                    .append(System.lineSeparator());
//            for (Long channelId : chatsInFolders.get(folderID)) {
//                builder
//                        .append("\t")
//                        .append(" - ")
//                        .append(chats.get(channelId).title)
//                        .append(System.lineSeparator());
//            }
        }
        helper.print(builder.toString(), true);
    }

    @Override
    public void loadChannelsHistory(String folderIDString, String dateFromString, String dateToString) {
        stopLoadingNewChannels();
        Integer folderID = NumbersParserUtil.parseInteger(folderIDString);
        Long dateFromUnix = NumbersParserUtil.parseUnixDateStartOfDay(dateFromString);
        Long dateToUnix = NumbersParserUtil.parseUnixDateEndOfDay(dateToString);
        Long dateNowUnix = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        if (dateFromUnix >= dateToUnix) {
            helper.print("Вторая дата не может быть больше первой", true);
            return;
        }
        if (dateFromUnix > dateNowUnix) {
            helper.print("Этот день ещё не наступил", true);
            return;
        }

        chatHistoryLoader.setDateFromUnix(dateFromUnix);
        chatHistoryLoader.setDateToUnix(dateToUnix);

        if (folderID == null) {
            helper.print("Начинаю загрузку со всех каналов", true);
            for (Long channelID : supergroups.keySet()) {
                loadChatHistory(channelID, dateFromUnix);
            }
        } else {
            if (chatsInFolders.containsKey(folderID)) {
                helper.print("Начинаю загрузку сообщений из папки " + foldersInfo.get(folderID), true);
                for (Long chatID : chatsInFolders.get(folderID)) {
                    if (supergroups.containsKey(chatID)) {
                        loadChatHistory(chatID, dateFromUnix);
                    }
                }
            } else {
                helper.print("Нет такой папки", true);
                return;
            }
        }
        chatHistoryLoader.removeSurplus();
        prepareNotes();
        helper.print("Всего загружено " + storage.getAllNotesQuantity() + " сообщ., соотв. заданным параметрам", true);
    }

    private void loadChatHistory(Long channelID, Long dateFromUnix) {
        int messagesLeft = PropertyHandler.getMessagesToDownload();
        int messagesToStop = PropertyHandler.getMessagesToStop();
        long fromMessageID = 0;
        while (true) {
            client.send(
                    new TdApi.GetChatHistory(channelID, fromMessageID, 0, 50, false),
                    chatHistoryLoader);
            try {
                Thread.sleep(Randomizer.giveNumber());
            } catch (InterruptedException e) {
                helper.print(e.getMessage(), true);
            }
            if (!chatHistoryLoader.isEmpty()) {
                fromMessageID = chatHistoryLoader.getLastMessageID();
                messagesLeft -= chatHistoryLoader.getCountArrived();
                messagesToStop -= chatHistoryLoader.getCountArrived();
            }
            helper.print(chatHistoryLoader.getAmountOfReceivedMsg() + " сообщ. предварительно загружено", false);
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
        helper.print("Загрузка сообщений из " + chats.get(channelID).title + " закончена", true);
        chatHistoryLoader.zeroCounter();
    }

    private Long transferChatID(Long chatID) {
        return -(1000000000000L + chatID);
    }

    @Override
    public void logout() {
        client.send(new TdApi.LogOut()).thenAccept(ok -> {
            helper.print("Вышел из аккаунта", true);
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

    private void stopLoadingNewChannels() {
        isReadyToLoadNewChannels = false;
    }

    private void prepareNotes() {
        while (!chatHistoryLoader.isEmpty()) {
            TdApi.Message message = chatHistoryLoader.takeMessage();
            String senderName = chats.get(message.chatId).title;
            storage.createNote(message, senderName);
        }
        storage.getNotesCommonPool().forEach(note -> {
            if (!note.hasLink()) {
                getMsgLink(note);
            }
        });
    }

    private void getMsgLink(Note note) {
        client.send(new TdApi.GetMessageLink(note.getSenderID(), note.getMessageID(), 0, true, true))
                .whenCompleteAsync((link, error) -> {
                    if (error != null) {
                        note.setMsgLink("Не удалось получить ссылку");
                        helper.print("Ошибка при запросе ссылки: " + error.getMessage(), true);
                    } else {
                        note.setMsgLink(link.link);
                    }
                });
    }
}