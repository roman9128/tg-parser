package rt.model.core;

import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import rt.model.authentication.ClientInteractionImpl;
import rt.model.authentication.PhoneAuthentication;
import rt.model.auxillaries.*;
import rt.presenter.Printer;
import rt.presenter.ServiceHelper;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserBot implements AutoCloseable {

    private final SimpleTelegramClient client;
    private final Printer printer;
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
                   Printer printer) {
        this.printer = printer;
        this.chatHistoryHandler = new ChatHistoryHandler(printer);
        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        clientBuilder.addUpdateHandler(TdApi.UpdateNewChat.class, this::onUpdateChat);
        clientBuilder.addUpdateHandler(TdApi.UpdateSupergroup.class, this::onUpdateSuperGroup);
        clientBuilder.addUpdateHandler(TdApi.UpdateChatFolders.class, this::onUpdateFolder);
        this.client = clientBuilder.build(authenticationData);
        client.setClientInteraction(new ClientInteractionImpl(blockingExecutor, client));
    }

    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        TdApi.AuthorizationState authorizationState = update.authorizationState;
        if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            printer.print("Авторизован", true);
            Status.setReadyToInteract(true);
            printer.print("Начинаю загрузку чатов, каналов, папок", true);
            getChats();
            getChannels();
            getFoldersInfo();
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            printer.print("Соединение закрыто", true);
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            printer.print("Не авторизован", true);
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
                printer.print(e.getMessage(), true);
            }
            getChatsFromFolders();
        });
        executor.shutdown();
    }

    private void getChatsFromFolders() {
        for (int folderID : foldersInfo.keySet()) {
            client.send(new TdApi.GetChatFolder(folderID)).whenCompleteAsync((folder, error) -> {
                if (error != null) {
                    printer.print("Ошибка при получении содержимого папок: " + error.getMessage(), true);
                } else {
                    chatsInFolders.put(folderID, folder.includedChatIds);
                }
            });
        }
    }

    public void showFolders() {
        for (int folderID : foldersInfo.keySet()) {
            printer.print(folderID + ": " + foldersInfo.get(folderID), true);
        }
    }

    public void loadChannelsHistory(String folderIDString, String dateFromString, String dateToString) {
        Integer folderID = ParseMaster.parseInteger(folderIDString);
        Long dateFromUnix = ParseMaster.parseUnixDateStartOfDay(dateFromString);
        Long dateToUnix = ParseMaster.parseUnixDateEndOfDay(dateToString);

        if (dateFromUnix > dateToUnix) {
            printer.print("Вторая дата не может быть больше первой", true);
            return;
        }

        chatHistoryHandler.setDateFromUnix(dateFromUnix);
        chatHistoryHandler.setDateToUnix(dateToUnix);

        if (folderID == null) {
            printer.print("Начинаю загрузку со всех каналов", true);
            for (Long channelID : supergroups.keySet()) {
                loadChatHistory(channelID, dateFromUnix);
            }
            chatHistoryHandler.removeSurplus();
            printer.print("Сообщения загружены. Всего: " + chatHistoryHandler.getSize() + " сообщ., соотв. заданным параметрам", true);
        } else {
            if (chatsInFolders.containsKey(folderID)) {
                printer.print("Начинаю загрузку сообщений из папки " + foldersInfo.get(folderID), true);
                for (Long chatID : chatsInFolders.get(folderID)) {
                    if (supergroups.containsKey(chatID)) {
                        loadChatHistory(chatID, dateFromUnix);
                    }
                }
                chatHistoryHandler.removeSurplus();
                printer.print("Сообщения загружены. Всего: " + chatHistoryHandler.getSize() + " сообщ., соотв. заданным параметрам", true);
            } else {
                printer.print("Нет такой папки", true);
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
                printer.print(e.getMessage(), true);
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
        printer.print("Загрузка сообщений из " + chats.get(channelID).title + " закончена", true);
        chatHistoryHandler.zeroCounter();
    }

    public void clear() {
        notes.clear();
        chatHistoryHandler.clear();
        printer.print("Загружено: " + chatHistoryHandler.getSize() + " cообщ.", true);
    }

    public void writeHistory() {
        if (chatHistoryHandler.historyIsEmpty()) {
            printer.print("Нечего записывать. Сначала нужно загрузить сообщения", true);
            return;
        }
        printer.print("Начинаю запись в файл (" + chatHistoryHandler.getSize() + " сообщ. всего)", true);
        String channelName = "";
        while (!chatHistoryHandler.historyIsEmpty()) {
            printer.print(chatHistoryHandler.getSize() + " сообщен. осталось записать", false);
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
                    printer.print("Ошибка при записи в файл: " + e.getMessage(), true);
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
                            printer.print("Ошибка при запросе ссылки: " + error.getMessage(), true);
                        } else {
                            notes.get(msgID).setMsgLink(link.link);
                            try {
                                messageRecorder.writeToFile(notes.get(msgID).toString());
                            } catch (IOException e) {
                                printer.print("Ошибка при записи в файл: " + e.getMessage(), true);
                            }
                            notes.remove(msgID);
                        }
                    });
            try {
                Thread.sleep(Randomizer.giveNumber());
            } catch (InterruptedException e) {
                printer.print(e.getMessage(), true);
            }
            if (chatHistoryHandler.historyIsEmpty()) {
                break;
            }
        }
        printer.print("Запись сообщений в файл закончена", true);
    }

    public SimpleTelegramClient getClient() {
        return client;
    }

    private Long transferChatID(Long chatID) {
        return -(1000000000000L + chatID);
    }

    public void logout() {
        client.send(new TdApi.LogOut()).thenAccept(ok -> {
            printer.print("Вышел из аккаунта", true);
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