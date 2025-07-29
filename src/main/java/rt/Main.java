package rt;

import rt.infrastructure.storage.NoteStorage;
import rt.model.service.NoteStorageService;
import rt.service_manager.ServiceManager;
import rt.view.View;
import rt.view.console.ConsoleUI;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        View view = new ConsoleUI();
        NoteStorageService storage = new NoteStorage();

        ServiceManager serviceManager = new ServiceManager(view, storage);
        view.setServiceManager(serviceManager);

        serviceManager.initService();
    }

    private static void countThreads() {
        Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
        System.out.println("Active Threads (" + threadMap.size() + "):");
        for (Map.Entry<Thread, StackTraceElement[]> entry : threadMap.entrySet()) {
            Thread thread = entry.getKey();
            System.out.println("  Thread: " + thread.getName() + " (State: " + thread.getState() + ")");
        }
    }
}