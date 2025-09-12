package rt.view.swing;

import rt.model.preset.PresetDTO;
import rt.view.View;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class SwingUI extends View {

    private final AuthPasswordWindow authWindow;
    private final AuthQrCodeWindow qrCodeWindow;
    private MainWindow mainWindow;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss - ");

    public SwingUI() {
        authWindow = new AuthPasswordWindow();
        qrCodeWindow = new AuthQrCodeWindow();
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
        } else if (text.equals("Загрузка окончена")) {
            mainWindow.showMessageDialog(text);
        } else if (text.contains("Всего отобрано сообщений: ")) {
            mainWindow.showMessageDialog(text);
            mainWindow.print(LocalDateTime.now().format(dtf) + text + System.lineSeparator().repeat(2));
        } else {
            mainWindow.print(LocalDateTime.now().format(dtf) + text + System.lineSeparator().repeat(2));
        }
    }

    public void load(String source, String dateFromString, String dateToString) {
        serviceManager.load(source, dateFromString, dateToString);
    }

    @Override
    public void showPresets() {
        mainWindow.updatePresetsList();
    }

    public List<PresetDTO> getUpdatedPresets() {
        return serviceManager.getPresets();
    }

    @Override
    public String ask2FAPassword() {
        qrCodeWindow.dispose();
        return authWindow.ask2FAPassword();
    }

    @Override
    public void showErrorMessage(String errorText) {
        JOptionPane.showMessageDialog(
                null,
                errorText,
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
    public void showQrCode(String link) {
        qrCodeWindow.showQRCode(link);
    }
}