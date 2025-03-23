package rt.model.core;

import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import rt.model.authentication.ClientInteractionImpl;
import rt.model.authentication.PhoneAuthentication;
import rt.model.auxillaries.*;
import rt.presenter.PrinterScanner;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserBot implements AutoCloseable {

    private final SimpleTelegramClient client;
    private final PrinterScanner printerScanner;
    private final ChatHistoryHandler chatHistoryHandler;
    private final MessageRecorder messageRecorder = new MessageRecorder();
    private final ExecutorService blockingExecutor = Executors.newSingleThreadExecutor();
    private final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, String> foldersInfo = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, long[]> chatsInFolders = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Note> notes = new ConcurrentHashMap<>();

    public UserBot(SimpleTelegramClientBuilder clientBuilder,
                   PhoneAuthentication authenticationData,
                   PrinterScanner printerScanner) {
        this.printerScanner = printerScanner;
        this.chatHistoryHandler = new ChatHistoryHandler(printerScanner);
        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        clientBuilder.addUpdateHandler(TdApi.UpdateNewChat.class, this::onUpdateChat);
        clientBuilder.addUpdateHandler(TdApi.UpdateSupergroup.class, this::onUpdateSuperGroup);
        clientBuilder.addUpdateHandler(TdApi.UpdateChatFolders.class, this::onUpdateFolder);
        this.client = clientBuilder.build(authenticationData);
        client.setClientInteraction(new ClientInteractionImpl(blockingExecutor, client, printerScanner));
    }

    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        TdApi.AuthorizationState authorizationState = update.authorizationState;
        if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            printerScanner.print("Авторизован", true);
            Status.setReadyToInteract(true);
            printerScanner.print("Начинаю загрузку чатов, каналов, папок", true);
            getChats();
            getChannels();
            getFoldersInfo();
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            printerScanner.print("Соединение закрыто", true);
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            printerScanner.print("Не авторизован", true);
        }
    }

    private void onUpdateChat(TdApi.UpdateNewChat updateNewChat) {
        TdApi.Chat chat = updateNewChat.chat;
        chats.put(chat.id, chat);
    }

    private void onUpdateSuperGroup(TdApi.UpdateSupergroup updateSupergroup) {
        TdApi.Supergroup supergroup = updateSupergroup.supergroup;
        if (supergroup.isChannel) {
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
                printerScanner.print(e.getMessage(), true);
            }
            getChatsFromFolders();
        });
        executor.shutdown();
    }

    private void getChatsFromFolders() {
        for (int folderID : foldersInfo.keySet()) {
            client.send(new TdApi.GetChatFolder(folderID)).whenCompleteAsync((folder, error) -> {
                if (error != null) {
                    printerScanner.print("Ошибка при получении содержимого папок: " + error.getMessage(), true);
                } else {
                    chatsInFolders.put(folderID, folder.includedChatIds);
                }
            });
        }
    }

    public void showFolders() {
        for (int folderID : foldersInfo.keySet()) {
            printerScanner.print(folderID + ": " + foldersInfo.get(folderID), true);
        }
    }

    public void loadChannelsHistory(String folderIDString, String dateFromString, String dateToString) {
        Integer folderID = ParseMaster.parseInteger(folderIDString);
        Long dateFromUnix = ParseMaster.parseUnixDateStartOfDay(dateFromString);
        Long dateToUnix = ParseMaster.parseUnixDateEndOfDay(dateToString);
        Long dateNowUnix = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        if (dateFromUnix > dateToUnix) {
            printerScanner.print("Вторая дата не может быть больше первой", true);
            return;
        }

        if (dateFromUnix > dateNowUnix) {
            printerScanner.print("Этот день ещё не наступил", true);
            return;
        }

        chatHistoryHandler.setDateFromUnix(dateFromUnix);
        chatHistoryHandler.setDateToUnix(dateToUnix);

        if (folderID == null) {
            printerScanner.print("Начинаю загрузку со всех каналов", true);
            for (Long channelID : supergroups.keySet()) {
                loadChatHistory(channelID, dateFromUnix);
            }
            chatHistoryHandler.removeSurplus();
            printerScanner.print("Сообщения загружены. Всего: " + chatHistoryHandler.getSize() + " сообщ., соотв. заданным параметрам", true);
        } else {
            if (chatsInFolders.containsKey(folderID)) {
                printerScanner.print("Начинаю загрузку сообщений из папки " + foldersInfo.get(folderID), true);
                for (Long chatID : chatsInFolders.get(folderID)) {
                    if (supergroups.containsKey(chatID)) {
                        loadChatHistory(chatID, dateFromUnix);
                    }
                }
                chatHistoryHandler.removeSurplus();
                printerScanner.print("Сообщения загружены. Всего: " + chatHistoryHandler.getSize() + " сообщ., соотв. заданным параметрам", true);
            } else {
                printerScanner.print("Нет такой папки", true);
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
                printerScanner.print(e.getMessage(), true);
            }
            if (!chatHistoryHandler.historyIsEmpty()) {
                fromMessageID = chatHistoryHandler.getLastMessageID();
                messagesLeft = messagesLeft - chatHistoryHandler.getCountArrived();
                messagesToStop = messagesToStop - chatHistoryHandler.getCountArrived();
            }
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
        printerScanner.print("Загрузка сообщений из " + chats.get(channelID).title + " закончена", true);
        chatHistoryHandler.zeroCounter();
    }

    public void clear() {
        notes.clear();
        chatHistoryHandler.clear();
        printerScanner.print("Загружено: " + chatHistoryHandler.getSize() + " cообщ.", true);
    }

    public void writeHistory() {
        if (chatHistoryHandler.historyIsEmpty()) {
            printerScanner.print("Нечего записывать. Сначала нужно загрузить сообщения", true);
            return;
        }
        printerScanner.print("Начинаю запись в файл (" + chatHistoryHandler.getSize() + " сообщ. всего)", true);
        String channelName = "";
        while (!chatHistoryHandler.historyIsEmpty()) {
            printerScanner.print(chatHistoryHandler.getSize() + " сообщен. осталось записать", false);
            TdApi.Message message = chatHistoryHandler.takeMessage();
            Integer msgDate = message.date;
            Long senderID = message.chatId;
            Long msgID = message.id;
            String senderName = chats.get(senderID).title;
            String text = "";

            if (!channelName.equals(senderName)) {
                channelName = senderName;
                try {
                    messageRecorder.writeToFile(">>>>>>> Далее сообщения из канала " + channelName + System.lineSeparator());
                } catch (IOException e) {
                    printerScanner.print("Ошибка при записи в файл: " + e.getMessage(), true);
                }
            }

            TdApi.MessageContent messageContent = message.content;
            switch (messageContent) {
                case TdApi.MessageText mt -> {
                    text = mt.text.text;
                }
                case TdApi.MessagePhoto mp -> {
                    text = mp.caption.text;
                }
                case TdApi.MessageVideo mv -> {
                    text = mv.caption.text;
                }
                case TdApi.MessageDocument md -> {
                    text = md.caption.text;
                }
                default -> {
                    text = "Сообщение без текста" + System.lineSeparator();
                }
            }
            notes.put(msgID, new Note(msgDate, senderName, text));
            client.send(new TdApi.GetMessageLink(senderID, msgID, 0, true, true))
                    .whenCompleteAsync((link, error) -> {
                        if (error != null) {
                            printerScanner.print("Ошибка при запросе ссылки: " + error.getMessage(), true);
                        } else {
                            notes.get(msgID).setMsgLink(link.link);
                            try {
                                messageRecorder.writeToFile(notes.get(msgID).toString());
                            } catch (IOException e) {
                                printerScanner.print("Ошибка при записи в файл: " + e.getMessage(), true);
                            }
                            notes.remove(msgID);
                        }
                    });
            try {
                Thread.sleep(Randomizer.giveNumber());
            } catch (InterruptedException e) {
                printerScanner.print(e.getMessage(), true);
            }
            if (chatHistoryHandler.historyIsEmpty()) {
                break;
            }
        }
        printerScanner.print("Запись сообщений в файл закончена", true);
    }

    public SimpleTelegramClient getClient() {
        return client;
    }

    private Long transferChatID(Long chatID) {
        return -(1000000000000L + chatID);
    }

    public void logout() {
        client.send(new TdApi.LogOut()).thenAccept(ok -> {
            printerScanner.print("Вышел из аккаунта", true);
            Status.setReadyToInteract(false);
        });
        stopBlockingExecutor();
    }

    @Override
    public void close() {
        client.sendClose();
        Status.setReadyToInteract(false);
        stopBlockingExecutor();
    }

    private void stopBlockingExecutor() {
        blockingExecutor.shutdownNow();
    }
}