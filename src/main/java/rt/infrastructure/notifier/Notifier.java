package rt.infrastructure.notifier;

import rt.model.notification.Notification;

import java.util.concurrent.LinkedBlockingQueue;

public class Notifier {
    private static volatile Notifier instance;
    private final LinkedBlockingQueue<Notification> queue = new LinkedBlockingQueue<>();

    private Notifier() {
    }

    public static synchronized Notifier getInstance() {
        if (instance == null) {
            instance = new Notifier();
        }
        return instance;
    }

    public void addNotification(Notification notification) {
        queue.offer(notification);
    }

    public Notification getNotification() throws InterruptedException {
        return queue.take();
    }

    public boolean hasNotifications() {
        return !queue.isEmpty();
    }
}
