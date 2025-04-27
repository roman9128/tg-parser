package rt.infrastructure.parser;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.Result;
import it.tdlight.jni.TdApi;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class ChatHistoryLoader implements GenericResultHandler<TdApi.Messages> {

    private final ConcurrentLinkedDeque<TdApi.Message> RECEIVED_MESSAGES = new ConcurrentLinkedDeque<>();
    private final AtomicInteger countArrived = new AtomicInteger(0);
    private final AtomicLong lastMessageDate = new AtomicLong(0);
    private Long dateFromUnix = 0L;
    private Long dateToUnix = Long.MAX_VALUE;

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
        } catch (Exception _) {
            // just keep working
        }
    }

    Long getLastMessageID() {
        if (RECEIVED_MESSAGES.peekLast() != null) {
            return RECEIVED_MESSAGES.peekLast().id;
        } else return 0L;
    }

    Long getLastMessageDate() {
        return lastMessageDate.get();
    }

    Long getLastMessageChatID() {
        if (RECEIVED_MESSAGES.peekLast() != null) {
            return RECEIVED_MESSAGES.peekLast().chatId;
        } else return 0L;
    }

    int getCountArrived() {
        return countArrived.get();
    }

    void zeroCounter() {
        countArrived.set(0);
    }

    TdApi.Message takeMessage() {
        return RECEIVED_MESSAGES.pollFirst();
    }

    boolean isEmpty() {
        return RECEIVED_MESSAGES.isEmpty();
    }

    int getAmountOfReceivedMsg() {
        return RECEIVED_MESSAGES.size();
    }

    void setDateFromUnix(Long dateFromUnix) {
        this.dateFromUnix = dateFromUnix;
    }

    void setDateToUnix(Long dateToUnix) {
        this.dateToUnix = dateToUnix;
    }

    void removeSurplus() {
        RECEIVED_MESSAGES.removeIf(message -> message.date < dateFromUnix || message.date > dateToUnix);
    }
}