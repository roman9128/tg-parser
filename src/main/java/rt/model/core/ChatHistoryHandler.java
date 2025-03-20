package rt.model.core;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.Result;
import it.tdlight.jni.TdApi;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ChatHistoryHandler implements GenericResultHandler<TdApi.Messages> {

    private final ConcurrentLinkedDeque<TdApi.Message> MESSAGES = new ConcurrentLinkedDeque<>();
    private final AtomicInteger countArrived = new AtomicInteger(0);
    private final AtomicLong lastMessageDate = new AtomicLong(0);
    private Long dateFromUnix = 0L;
    private Long dateToUnix = Long.MAX_VALUE;

    @Override
    public void onResult(Result result) {
        try {
            TdApi.Messages messages = (TdApi.Messages) result.get();
            countArrived.set(messages.totalCount);
            lastMessageDate.set(messages.messages[messages.messages.length - 1].date);
            for (TdApi.Message message : messages.messages) {
                if (dateFromUnix <= message.date) {
                    MESSAGES.offer(message);
                }
            }
            System.out.print("Предварительно загружено " + MESSAGES.size() + " сообщен." + "\r");
        } catch (Exception e) {
            System.out.println("Ошибка при получении сообщений с сервера: " + e.getMessage());
        }
    }

    protected Long getLastMessageID() {
        assert MESSAGES.peekLast() != null;
        return MESSAGES.peekLast().id;
    }

    protected Long getLastMessageDate() {
        return lastMessageDate.get();
    }

    protected Long getLastMessageChatID() {
        try {
            return MESSAGES.peekLast().chatId;
        } catch (NullPointerException e) {
            return 0L;
        }
    }

    protected int getCountArrived() {
        return countArrived.get();
    }

    protected void zeroCounter() {
        countArrived.set(0);
    }

    protected TdApi.Message takeMessage() {
        return MESSAGES.pollFirst();
    }

    protected boolean historyIsEmpty() {
        return MESSAGES.isEmpty();
    }

    protected int getSize() {
        return MESSAGES.size();
    }

    protected void setDateFromUnix(Long dateFromUnix) {
        this.dateFromUnix = dateFromUnix;
    }

    protected void setDateToUnix(Long dateToUnix) {
        this.dateToUnix = dateToUnix;
    }

    protected void clear() {
        MESSAGES.clear();
    }

    protected void removeSurplus() {
        MESSAGES.removeIf(message -> message.date > dateToUnix);
        System.out.println("Проверка на наличие неподходящих по дате сообщений выполнена");
    }
}