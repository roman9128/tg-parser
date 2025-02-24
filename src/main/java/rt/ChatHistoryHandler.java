package rt;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.Result;
import it.tdlight.jni.TdApi;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatHistoryHandler implements GenericResultHandler<TdApi.Messages> {

    private final ConcurrentLinkedDeque<TdApi.Message> MESSAGES = new ConcurrentLinkedDeque<>();
    private final AtomicInteger countArrived = new AtomicInteger(0);

    @Override
    public void onResult(Result result) {
        try {
            TdApi.Messages messages = (TdApi.Messages) result.get();
            countArrived.set(messages.totalCount);
            for (TdApi.Message message : messages.messages) {
                MESSAGES.offer(message);
            }
            System.out.println(MESSAGES.size() + " сообщен. всего загружено");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Long getLastID() {
        return MESSAGES.peekLast().id;
    }

    public int getCountArrived() {
        return countArrived.get();
    }

    public void zeroCounter() {
        countArrived.set(0);
    }

    public TdApi.Message takeMessage() {
        return MESSAGES.pollFirst();
    }

    public boolean historyIsEmpty() {
        return MESSAGES.isEmpty();
    }

    public int getSize() {
        return MESSAGES.size();
    }
}