package rt.model.core;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.Result;
import it.tdlight.jni.TdApi;
import rt.presenter.PrinterScanner;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ChatHistoryHandler implements GenericResultHandler<TdApi.Messages> {

    private final ConcurrentLinkedDeque<TdApi.Message> RECEIVED_MESSAGES = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<TdApi.Message> READY_TO_SEND_MESSAGES = new ConcurrentLinkedDeque<>();
    private final AtomicInteger countArrived = new AtomicInteger(0);
    private final AtomicLong lastMessageDate = new AtomicLong(0);
    private final PrinterScanner printerScanner;
    private Long dateFromUnix = 0L;
    private Long dateToUnix = Long.MAX_VALUE;

    public ChatHistoryHandler(PrinterScanner printerScanner) {
        this.printerScanner = printerScanner;
    }

    @Override
    public void onResult(Result result) {
        try {
            TdApi.Messages messages = (TdApi.Messages) result.get();
            if (messages.messages.length == 0) {
                return;
            }
            countArrived.set(messages.totalCount);
            lastMessageDate.set(messages.messages[messages.messages.length - 1].date);
            for (TdApi.Message message : messages.messages) {
                RECEIVED_MESSAGES.offer(message);
            }
        } catch (Exception e) {
            printerScanner.print("Ошибка при получении сообщений с сервера: " + e.getMessage(), true);
        }
    }

    protected Long getLastMessageID() {
        assert RECEIVED_MESSAGES.peekLast() != null;
        return RECEIVED_MESSAGES.peekLast().id;
    }

    protected Long getLastMessageDate() {
        return lastMessageDate.get();
    }

    protected Long getLastMessageChatID() {
        try {
            return RECEIVED_MESSAGES.peekLast().chatId;
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
        return READY_TO_SEND_MESSAGES.pollFirst();
    }

    protected boolean noReceivedMsgs() {
        return RECEIVED_MESSAGES.isEmpty();
    }

    protected boolean noReadyToSendMsgs() {
        return READY_TO_SEND_MESSAGES.isEmpty();
    }

    protected int getAmountOfReadyToSendMsg() {
        return READY_TO_SEND_MESSAGES.size();
    }

    protected int getAmountOfReceivedMsg(){
        return RECEIVED_MESSAGES.size();
    }

    protected void setDateFromUnix(Long dateFromUnix) {
        this.dateFromUnix = dateFromUnix;
    }

    protected void setDateToUnix(Long dateToUnix) {
        this.dateToUnix = dateToUnix;
    }

    protected void clear() {
        READY_TO_SEND_MESSAGES.clear();
    }

    protected void removeSurplus() {
        RECEIVED_MESSAGES.removeIf(message -> message.date < dateFromUnix || message.date > dateToUnix);
        READY_TO_SEND_MESSAGES.addAll(RECEIVED_MESSAGES);
        RECEIVED_MESSAGES.clear();
        printerScanner.print("Всего загружено " + getAmountOfReadyToSendMsg() + " сообщ., соотв. заданным параметрам", true);
    }
}