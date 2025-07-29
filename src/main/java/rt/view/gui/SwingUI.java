package rt.view.gui;

import rt.infrastructure.notifier.Notifier;
import rt.service_manager.ServiceManager;
import rt.view.View;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SwingUI implements View {

    private ServiceManager serviceManager;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final AuthWindow authWindow;
    private final MainWindow mainWindow;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss - ");

    public SwingUI() {
        authWindow = new AuthWindow();
        mainWindow = new MainWindow(this);
    }

    @Override
    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public void startInteractions() {
        mainWindow.setVisible(true);
        authWindow.dispose();
    }

    @Override
    public void startNotificationListener() {
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    print(Notifier.getInstance().getNotification());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    @Override
    public void stopNotificationListener() {
        executor.shutdownNow();
    }

    Map<Integer, String> getFoldersIDsAndNames() {
        return serviceManager.getFoldersIDsAndNames();
    }

    Map<Long, String> getChannelsIDsAndNames() {
        return serviceManager.getChannelsIDsAndNames();
    }

    void createLoadingWindow() {
        new LoadingWindow(this);
    }

    @Override
    public void load(String source, String dateFromString, String dateToString) {
        serviceManager.load(source, dateFromString, dateToString);
    }

    void createSearchWindow() {
        if (serviceManager.noAnyNotes()) {
            find(new String[]{});
        } else {
            new SearchWindow(this);
        }
    }

    @Override
    public void find(String[] args) {
        serviceManager.find(args);
    }

    @Override
    public void write(String value) {
        serviceManager.write(value);
    }

    @Override
    public void classify() {
        if (serviceManager.analyzerIsAvailable()) {
            serviceManager.classify();
        } else {
            print("Анализатор выключен");
        }
    }

    @Override
    public void clear() {
        serviceManager.clear();
    }

    @Override
    public void stopParser() {
        serviceManager.close();
    }

    @Override
    public void logout() {
        serviceManager.logout();
    }

    @Override
    public void print(String text) {
        mainWindow.getTextArea().append(LocalDateTime.now().format(dtf) + text + System.lineSeparator().repeat(2));
    }

    @Override
    public String askParameter(String who, String question) {
        return authWindow.askParameter(who, question);
    }

    @Override
    public void setMaxAmount(String arg) {
        serviceManager.setMessagesToDownload(arg);
    }
}