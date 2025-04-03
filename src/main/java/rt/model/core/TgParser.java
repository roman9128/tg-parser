package rt.model.core;

import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import rt.model.authentication.ClientInteractionImpl;
import rt.model.authentication.PhoneAuthentication;
import rt.model.note.*;
import rt.model.utils.FileRecorder;
import rt.model.utils.ParseMaster;
import rt.model.utils.PropertyHandler;
import rt.model.utils.Randomizer;
import rt.presenter.ServiceHelper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TgParser implements AutoCloseable {

    private final SimpleTelegramClient client;
    private final ServiceHelper helper;
    private final ChatHistoryHandler chatHistoryHandler;
    private final ExecutorService blockingExecutor = Executors.newSingleThreadExecutor();
    private final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, String> foldersInfo = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, long[]> chatsInFolders = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Note> notes = new ConcurrentHashMap<>();
    private volatile boolean isReadyToLoadNewChannels = true;

    protected TgParser(SimpleTelegramClientBuilder clientBuilder,
                       PhoneAuthentication authenticationData,
                       ServiceHelper helper) {
        this.helper = helper;
        this.chatHistoryHandler = new ChatHistoryHandler(helper);
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

    protected void showFolders() {
        StringBuilder builder = new StringBuilder();
        for (int folderID : foldersInfo.keySet()) {
            builder
                    .append(folderID)
                    .append(": ")
                    .append(foldersInfo.get(folderID))
                    .append(System.lineSeparator());
            for (Long channelId : chatsInFolders.get(folderID)) {
                builder
                        .append("\t")
                        .append(" - ")
                        .append(chats.get(channelId).title)
                        .append(System.lineSeparator());
            }
        }
        helper.print(builder.toString(), true);
    }

    protected void loadChannelsHistory(String folderIDString, String dateFromString, String dateToString) {
        Integer folderID = ParseMaster.parseInteger(folderIDString);
        Long dateFromUnix = ParseMaster.parseUnixDateStartOfDay(dateFromString);
        Long dateToUnix = ParseMaster.parseUnixDateEndOfDay(dateToString);
        Long dateNowUnix = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        if (dateFromUnix > dateToUnix) {
            helper.print("Вторая дата не может быть больше первой", true);
            return;
        }

        if (dateFromUnix > dateNowUnix) {
            helper.print("Этот день ещё не наступил", true);
            return;
        }

        chatHistoryHandler.setDateFromUnix(dateFromUnix);
        chatHistoryHandler.setDateToUnix(dateToUnix);

        if (folderID == null) {
            helper.print("Начинаю загрузку со всех каналов", true);
            for (Long channelID : supergroups.keySet()) {
                loadChatHistory(channelID, dateFromUnix);
            }
            chatHistoryHandler.removeSurplus();
        } else {
            if (chatsInFolders.containsKey(folderID)) {
                helper.print("Начинаю загрузку сообщений из папки " + foldersInfo.get(folderID), true);
                for (Long chatID : chatsInFolders.get(folderID)) {
                    if (supergroups.containsKey(chatID)) {
                        loadChatHistory(chatID, dateFromUnix);
                    }
                }
                chatHistoryHandler.removeSurplus();
            } else {
                helper.print("Нет такой папки", true);
            }
        }
        chatHistoryHandler.setDateFromUnix(0L); // возврат значений по умолчанию
        chatHistoryHandler.setDateToUnix(Long.MAX_VALUE);
    }

    private void loadChatHistory(Long channelID, Long dateFromUnix) {
        int messagesLeft = PropertyHandler.getMessagesToDownload();
        int messagesToStop = PropertyHandler.getMessagesToStop();
        long fromMessageID = 0;
        while (true) {
            client.send(
                    new TdApi.GetChatHistory(channelID, fromMessageID, 0, 50, false),
                    chatHistoryHandler);
            try {
                Thread.sleep(Randomizer.giveNumber());
            } catch (InterruptedException e) {
                helper.print(e.getMessage(), true);
            }
            if (!chatHistoryHandler.noReceivedMsgs()) {
                fromMessageID = chatHistoryHandler.getLastMessageID();
                messagesLeft -= chatHistoryHandler.getCountArrived();
                messagesToStop -= chatHistoryHandler.getCountArrived();
            }
            helper.print(chatHistoryHandler.getAmountOfReceivedMsg() + " сообщ. предварительно загружено", false);
            if (chatHistoryHandler.getCountArrived() == 0) {
                break;
            }
            if (messagesToStop < 1) {
                break;
            }
            if (dateFromUnix != 0L) {
                if (channelID.equals(chatHistoryHandler.getLastMessageChatID())
                        && chatHistoryHandler.getLastMessageDate() <= dateFromUnix) {
                    break;
                }
            } else {
                if (messagesLeft < 1) {
                    break;
                }
            }
        }
        helper.print("Загрузка сообщений из " + chats.get(channelID).title + " закончена", true);
        chatHistoryHandler.zeroCounter();
    }

    protected void clear() {
        notes.clear();
        chatHistoryHandler.clear();
        helper.print("Загруженные сообщения удалены", true);
    }

    protected void writeHistory() {
        if (chatHistoryHandler.noReadyToSendMsgs()) {
            helper.print("Нечего записывать. Сначала нужно загрузить сообщения", true);
            return;
        }
        helper.print("Начинаю запись в файл (" + chatHistoryHandler.getAmountOfReadyToSendMsg() + " сообщ. всего)", true);
        String channelName = "";
        while (!chatHistoryHandler.noReadyToSendMsgs()) {
            helper.print(chatHistoryHandler.getAmountOfReadyToSendMsg() + " сообщен. осталось записать", false);
            TdApi.Message message = chatHistoryHandler.takeMessage();
            Integer msgDate = message.date;
            Long senderID = message.chatId;
            Long msgID = message.id;
            String senderName = chats.get(senderID).title;
            String text = "";

            if (!channelName.equals(senderName)) {
                channelName = senderName;
                try {
                    FileRecorder.writeToFile(PropertyHandler.getFilePath(), ">>>>>>> Далее сообщения из канала " + channelName + System.lineSeparator());
                } catch (IOException e) {
                    helper.print("Ошибка при записи в файл: " + e.getMessage(), true);
                }
            }

            TdApi.MessageContent messageContent = message.content;
            switch (messageContent) {
                case TdApi.MessageText mt -> {
                    text = mt.text.text;
                }
                case TdApi.MessagePhoto mp -> {
                    text = "Фото. " + mp.caption.text;
                }
                case TdApi.MessageVideo mv -> {
                    text = "Видео. " + mv.caption.text;
                }
                case TdApi.MessageDocument md -> {
                    text = "Документ. " + md.caption.text;
                }
                default -> {
                    text = "Сообщение без текста" + System.lineSeparator();
                }
            }
            notes.put(msgID, new Note(msgDate, senderName, text));
            client.send(new TdApi.GetMessageLink(senderID, msgID, 0, true, true))
                    .whenCompleteAsync((link, error) -> {
                        if (error != null) {
                            notes.get(msgID).setMsgLink("Ошибка при запросе ссылки");
                            helper.print("Ошибка при запросе ссылки: " + error.getMessage(), true);
                        } else {
                            notes.get(msgID).setMsgLink(link.link);
                        }
                    });
            try {
                Thread.sleep(Randomizer.giveNumber());
            } catch (InterruptedException e) {
                helper.print(e.getMessage(), true);
            }
            writeMsgToFile(notes.get(msgID));
            notes.remove(msgID);
            if (chatHistoryHandler.noReadyToSendMsgs()) {
                break;
            }
        }
        helper.print("Запись сообщений в файл закончена", true);
        notes.clear();
    }

    private void writeMsgToFile(Note note) {
        try {
            FileRecorder.writeToFile(PropertyHandler.getFilePath(), note.toString());
        } catch (IOException e) {
            helper.print("Ошибка при записи в файл: " + e.getMessage(), true);
        }
    }

    protected SimpleTelegramClient getClient() {
        return client;
    }

    private Long transferChatID(Long chatID) {
        return -(1000000000000L + chatID);
    }

    protected void logout() {
        client.send(new TdApi.LogOut()).thenAccept(ok -> {
            helper.print("Вышел из аккаунта", true);
        });
        stopBlockingExecutor();
    }

    @Override
    public void close() {
        client.sendClose();
        stopBlockingExecutor();
    }

    private void stopBlockingExecutor() {
        blockingExecutor.shutdownNow();
    }

    protected void stopLoadingNewChannels() {
        isReadyToLoadNewChannels = false;
    }
}