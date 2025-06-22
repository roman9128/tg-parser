package rt.infrastructure.notifier;

import java.util.concurrent.LinkedBlockingQueue;

public class Notifier {
    private static volatile Notifier instance;
    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    private Notifier() {
    }

    public static synchronized Notifier getInstance() {
        if (instance == null) {
            instance = new Notifier();
        }
        return instance;
    }

    public void addNotification(String notification) {
        queue.offer(notification);
    }

    public String getNotification() throws InterruptedException {
        return queue.take();
    }
}