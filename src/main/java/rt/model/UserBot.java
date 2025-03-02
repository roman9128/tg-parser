package rt.model;

import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import rt.auxillaries.MessageRecorder;
import rt.auxillaries.Note;
import rt.auxillaries.ParseMaster;
import rt.auxillaries.PropertyHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserBot implements AutoCloseable {

    private final SimpleTelegramClient client;
    private final AtomicBoolean isLoggedIn = new AtomicBoolean(false);
    private final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, String> foldersInfo = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, long[]> chatsInFolders = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Note> notes = new ConcurrentHashMap<>();
    private final ChatHistoryHandler chatHistoryHandler = new ChatHistoryHandler();
    private final MessageRecorder messageRecorder = new MessageRecorder();

    public UserBot(SimpleTelegramClientBuilder clientBuilder,
                   ConsoleInteractiveAuthenticationData authenticationData) {
        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        clientBuilder.addUpdateHandler(TdApi.UpdateNewChat.class, this::onUpdateChat);
        clientBuilder.addUpdateHandler(TdApi.UpdateSupergroup.class, this::onUpdateSuperGroup);
        clientBuilder.addUpdateHandler(TdApi.UpdateChatFolders.class, this::onUpdateFolder);
        this.client = clientBuilder.build(authenticationData);
    }

    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        TdApi.AuthorizationState authorizationState = update.authorizationState;
        if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            System.out.println("Вошёл в аккаунт");
            isLoggedIn.set(true);
            getChats();
            getChannels();
            getFoldersInfo();
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
            System.out.println("Закрытие соединения...");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            System.out.println("Соединение закрыто");
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            System.out.println("Вышел из аккаунта");
            isLoggedIn.set(false);
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
                System.out.println(e.getMessage());
            }
            getFoldersChats();
        });
        executor.shutdown();
    }

    private void getFoldersChats() {
        for (int folderID : foldersInfo.keySet()) {
            client.send(new TdApi.GetChatFolder(folderID)).whenCompleteAsync((folder, error) -> {
                if (error != null) {
                    System.out.println(error.getMessage());
                } else {
                    chatsInFolders.put(folderID, folder.includedChatIds);
                }
            });
        }
    }

    public void showFolders() {
        for (int folderID : foldersInfo.keySet()) {
            System.out.println(folderID + ": " + foldersInfo.get(folderID));
        }
    }

    public void loadChannelsHistory(String folderIDString, String dateString) {
        Integer folderID = ParseMaster.parseInteger(folderIDString);
        Long dateUnix = ParseMaster.parseUnixTime(dateString);

        if (dateUnix != null) {
            chatHistoryHandler.setDateUnix(dateUnix);
        }

        if (folderID == null) {
            System.out.println("Начинаю загрузку со всех каналов");
            for (Long channelID : supergroups.keySet()) {
                loadChatHistory(channelID, dateUnix);
            }
            System.out.println("Сообщения загружены. Всего: " + chatHistoryHandler.getSize() + " сообщ.");
        } else {
            if (chatsInFolders.containsKey(folderID)) {
                System.out.println("Начинаю загрузку сообщений из папки " + foldersInfo.get(folderID));
                for (Long chatID : chatsInFolders.get(folderID)) {
                    if (supergroups.containsKey(chatID)) {
                        loadChatHistory(chatID, dateUnix);
                    }
                }
                System.out.println("Сообщения загружены. Всего: " + chatHistoryHandler.getSize() + " сообщ.");
            } else {
                System.out.println("Нет такой папки");
            }
        }
        chatHistoryHandler.setDateUnix(0L);
    }

    private void loadChatHistory(Long channelID, Long dateUnix) {
        int messagesLeft = PropertyHandler.getMessagesToDownload();
        int messagesToStop = PropertyHandler.getMessagesToStop();
        long fromMessageID = 0;
        while (true) {
            client.send(
                    new TdApi.GetChatHistory(
                            channelID, fromMessageID, 0, 50, false),
                    chatHistoryHandler);
            try {
                Thread.sleep(333);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            if (!chatHistoryHandler.historyIsEmpty()) {
                fromMessageID = chatHistoryHandler.getLastMessageID();
                messagesLeft = messagesLeft - chatHistoryHandler.getCountArrived();
                messagesToStop = messagesToStop - chatHistoryHandler.getCountArrived();
            }
            try {
                Thread.sleep(333);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            if (chatHistoryHandler.getCountArrived() == 0) {
                break;
            }
            if (messagesToStop < 1) {
                break;
            }
            if (dateUnix != null) {
                if (channelID.equals(chatHistoryHandler.getLastMessageChatID())
                        && chatHistoryHandler.getLastMessageDate() < dateUnix) {
                    break;
                }
            } else {
                if (messagesLeft < 1) {
                    break;
                }
            }
        }
        System.out.println("Загрузка сообщений из " + chats.get(channelID).title + " закончена");
        chatHistoryHandler.zeroCounter();
    }

    public void clear() {
        notes.clear();
        chatHistoryHandler.clear();
        System.out.println("Загружено: " + chatHistoryHandler.getSize() + " cообщ.");
    }

    public void writeHistory() {
        if (chatHistoryHandler.historyIsEmpty()) {
            System.out.println("Нечего записывать. Сначала нужно загрузить сообщения");
            return;
        }
        System.out.println("Начинаю запись в файл (" + chatHistoryHandler.getSize() + " сообщ. всего)");
        while (!chatHistoryHandler.historyIsEmpty()) {
            System.out.print(chatHistoryHandler.getSize() + " сообщен. осталось записать" + "\r");
            TdApi.Message message = chatHistoryHandler.takeMessage();
            Integer msgDate = message.date;
            Long senderID = message.chatId;
            Long msgID = message.id;
            String senderName = chats.get(senderID).title;
            String text = "";

            TdApi.MessageContent messageContent = message.content;
            switch (messageContent) {
                case TdApi.MessageText mt -> {
                    text = mt.text.text;
                }
                case TdApi.MessagePhoto mp -> {
                    text = "Фото" + System.lineSeparator() + mp.caption.text;
                }
                case TdApi.MessageVideo mv -> {
                    text = "Видео" + System.lineSeparator() + mv.caption.text;
                }
                case TdApi.MessageDocument md -> {
                    text = "Документ" + System.lineSeparator() + md.caption.text;
                }
                default -> {
                }
            }
            notes.put(msgID, new Note(msgDate, senderName, text));
            client.send(new TdApi.GetMessageLink(senderID, msgID, 0, true, true))
                    .whenCompleteAsync((link, error) -> {
                        if (error != null) {
                            System.out.println(error.getMessage());
                        } else {
                            notes.get(msgID).setMsgLink(link.link);
                            messageRecorder.writeToFile(notes.get(msgID).toString());
                            notes.remove(msgID);
                        }
                    });
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            if (chatHistoryHandler.historyIsEmpty()) {
                break;
            }
        }
        System.out.println("Запись сообщений в файл закончена");
    }

    public SimpleTelegramClient getClient() {
        return client;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    private Long transferChatID(Long chatID) {
        return -(1000000000000L + chatID);
    }

    public boolean isLoggedIn() {
        return isLoggedIn.get();
    }
}