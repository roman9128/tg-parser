package rt.model;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.Result;
import it.tdlight.jni.TdApi;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatHistoryHandler implements GenericResultHandler<TdApi.Messages> {

    private final ConcurrentLinkedDeque<TdApi.Message> MESSAGES = new ConcurrentLinkedDeque<>();
    private final AtomicInteger countArrived = new AtomicInteger(0);
    private Long dateUnix = 0L;

    @Override
    public void onResult(Result result) {
        try {
            TdApi.Messages messages = (TdApi.Messages) result.get();
            countArrived.set(messages.totalCount);
            for (TdApi.Message message : messages.messages) {
                if (message.date > dateUnix) {
                    MESSAGES.offer(message);
                }
            }
            System.out.print(MESSAGES.size() + " сообщен. всего загружено" + "\r");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    protected Long getLastMessageID() {
        assert MESSAGES.peekLast() != null;
        return MESSAGES.peekLast().id;
    }

    protected Long getLastMessageDate() {
        assert MESSAGES.peekLast() != null;
        return Long.valueOf(MESSAGES.peekLast().date);
    }

    protected Long getLastMessageChatID() {
        assert MESSAGES.peekLast() != null;
        return MESSAGES.peekLast().chatId;
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

    protected void setDateUnix(Long dateUnix) {
        this.dateUnix = dateUnix;
    }

    protected void clear() {
        MESSAGES.clear();
    }
}