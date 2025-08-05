package rt.view.gui;

import rt.infrastructure.notifier.Notifier;
import rt.service_manager.ServiceManager;
import rt.view.View;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class SwingUI extends View {

    private final AuthWindow authWindow;
    private final MainWindow mainWindow;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss - ");

    public SwingUI() {
        authWindow = new AuthWindow();
        mainWindow = new MainWindow(this);
    }

    @Override
    public void startInteractions() {
        mainWindow.setVisible(true);
        authWindow.dispose();
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

    void createSearchWindow() {
        if (serviceManager.noAnyNotes()) {
            find(new String[]{});
        } else {
            new SearchWindow(this);
        }
    }

    @Override
    public void print(String text) {
        mainWindow.getTextArea().append(LocalDateTime.now().format(dtf) + text + System.lineSeparator().repeat(2));
    }

    @Override
    public void showPresets() {

    }

    @Override
    public String askParameter(String who, String question) {
        return authWindow.askParameter(who, question);
    }
}