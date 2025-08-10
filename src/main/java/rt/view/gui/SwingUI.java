package rt.view.gui;

import rt.model.preset.PresetDTO;
import rt.view.View;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class SwingUI extends View {

    private final AuthWindow authWindow;
    private MainWindow mainWindow;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss - ");

    public SwingUI() {
        authWindow = new AuthWindow();
    }

    @Override
    public void startInteractions() {
        mainWindow = new MainWindow(this);
        showPresets();
        authWindow.dispose();
    }

    Map<Integer, String> getFoldersIDsAndNames() {
        return serviceManager.getFoldersIDsAndNames();
    }

    Map<Long, String> getChannelsIDsAndNames() {
        return serviceManager.getChannelsIDsAndNames();
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
        if (mainWindow == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                print(e.getMessage());
            }
            print(text);
        } else if (text.equals("Загрузка и анализ сообщений закончены")) {
            mainWindow.showMessageDialog(text + System.lineSeparator() + "Теперь доступны функции поиска и записи сообщений");
        } else if (text.contains("Всего отобрано сообщений: ")) {
            mainWindow.showMessageDialog(text);
            mainWindow.print(LocalDateTime.now().format(dtf) + text + System.lineSeparator().repeat(2));
        } else {
            mainWindow.print(LocalDateTime.now().format(dtf) + text + System.lineSeparator().repeat(2));
        }
    }

    public void loadAnalyze(String source, String dateFromString, String dateToString) {
        serviceManager.loadAnalyze(source, dateFromString, dateToString);
    }

    @Override
    public void showPresets() {
        mainWindow.updatePresetsList();
    }

    public List<PresetDTO> getUpdatedPresets() {
        return serviceManager.getPresets();
    }

    @Override
    public String askParameter(String who, String question) {
        return authWindow.askParameter(who, question);
    }
}