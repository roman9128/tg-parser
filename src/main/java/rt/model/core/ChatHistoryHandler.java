package rt.model.core;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.Result;
import it.tdlight.jni.TdApi;
import rt.presenter.Printer;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ChatHistoryHandler implements GenericResultHandler<TdApi.Messages> {

    private final ConcurrentLinkedDeque<TdApi.Message> MESSAGES = new ConcurrentLinkedDeque<>();
    private final AtomicInteger countArrived = new AtomicInteger(0);
    private final AtomicLong lastMessageDate = new AtomicLong(0);
    private final Printer printer;
    private Long dateFromUnix = 0L;
    private Long dateToUnix = Long.MAX_VALUE;

    public ChatHistoryHandler(Printer printer) {
        this.printer = printer;
    }

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
            printer.print("Предварительно загружено " + MESSAGES.size() + " сообщен.", false);
        } catch (Exception e) {
            printer.print("Ошибка при получении сообщений с сервера: " + e.getMessage(), true);
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
        printer.print("Проверка на наличие неподходящих по дате сообщений выполнена", true);
    }
}