package rt.model.core;

import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import rt.model.authentication.ClientInteractionImpl;
import rt.model.authentication.PhoneAuthentication;
import rt.model.auxillaries.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserBot implements AutoCloseable {

    private final SimpleTelegramClient client;
    private final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, String> foldersInfo = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, long[]> chatsInFolders = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Note> notes = new ConcurrentHashMap<>();
    private final ChatHistoryHandler chatHistoryHandler = new ChatHistoryHandler();
    private final MessageRecorder messageRecorder = new MessageRecorder();
    private final ExecutorService blockingExecutor = Executors.newSingleThreadExecutor();

    public UserBot(SimpleTelegramClientBuilder clientBuilder,
                   PhoneAuthentication authenticationData) {
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
            System.out.println("Авторизован");
            Status.setReadyToInteract(true);
            System.out.println("Начинаю загрузку чатов, каналов, папок");
            getChats();
            getChannels();
            getFoldersInfo();
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
            System.out.println("Закрытие соединения...");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            System.out.println("Соединение закрыто");
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            System.out.println("Не авторизован");
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
            getChatsFromFolders();
        });
        executor.shutdown();
    }

    private void getChatsFromFolders() {
        for (int folderID : foldersInfo.keySet()) {
            client.send(new TdApi.GetChatFolder(folderID)).whenCompleteAsync((folder, error) -> {
                if (error != null) {
                    System.out.println("Ошибка при получении содержимого папок: " + error.getMessage());
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

    public void loadChannelsHistory(String folderIDString, String dateFromString, String dateToString) {
        Integer folderID = ParseMaster.parseInteger(folderIDString);
        Long dateFromUnix = ParseMaster.parseUnixDateStartOfDay(dateFromString);
        Long dateToUnix = ParseMaster.parseUnixDateEndOfDay(dateToString);

        if (dateFromUnix > dateToUnix) {
            System.out.println("Вторая дата не может быть больше первой");
            return;
        }

        chatHistoryHandler.setDateFromUnix(dateFromUnix);
        chatHistoryHandler.setDateToUnix(dateToUnix);

        if (folderID == null) {
            System.out.println("Начинаю загрузку со всех каналов");
            for (Long channelID : supergroups.keySet()) {
                loadChatHistory(channelID, dateFromUnix);
            }
            chatHistoryHandler.removeSurplus();
            System.out.println("Сообщения загружены. Всего: " + chatHistoryHandler.getSize() + " сообщ., соотв. заданным параметрам");
        } else {
            if (chatsInFolders.containsKey(folderID)) {
                System.out.println("Начинаю загрузку сообщений из папки " + foldersInfo.get(folderID));
                for (Long chatID : chatsInFolders.get(folderID)) {
                    if (supergroups.containsKey(chatID)) {
                        loadChatHistory(chatID, dateFromUnix);
                    }
                }
                chatHistoryHandler.removeSurplus();
                System.out.println("Сообщения загружены. Всего: " + chatHistoryHandler.getSize() + " сообщ., соотв. заданным параметрам");
            } else {
                System.out.println("Нет такой папки");
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
                System.out.println(e.getMessage());
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
        String channelName = "";
        while (!chatHistoryHandler.historyIsEmpty()) {
            System.out.print(chatHistoryHandler.getSize() + " сообщен. осталось записать" + "\r");
            TdApi.Message message = chatHistoryHandler.takeMessage();
            Integer msgDate = message.date;
            Long senderID = message.chatId;
            Long msgID = message.id;
            String senderName = chats.get(senderID).title;
            String text = "";

            if (!channelName.equals(senderName)) {
                channelName = senderName;
                messageRecorder.writeToFile(">>>>>>> Далее сообщения из канала " + channelName + System.lineSeparator());
            }

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
                    text = "Сообщение иного типа" + System.lineSeparator();
                }
            }
            notes.put(msgID, new Note(msgDate, senderName, text));
            client.send(new TdApi.GetMessageLink(senderID, msgID, 0, true, true))
                    .whenCompleteAsync((link, error) -> {
                        if (error != null) {
                            System.out.println("Ошибка при запросе ссылки: " + error.getMessage());
                        } else {
                            notes.get(msgID).setMsgLink(link.link);
                            messageRecorder.writeToFile(notes.get(msgID).toString());
                            notes.remove(msgID);
                        }
                    });
            try {
                Thread.sleep(Randomizer.giveNumber());
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

    private Long transferChatID(Long chatID) {
        return -(1000000000000L + chatID);
    }

    public void logout() {
        client.send(new TdApi.LogOut()).thenAccept(ok -> {
            System.out.println("Вышел из аккаунта");
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