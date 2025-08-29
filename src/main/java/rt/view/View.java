package rt.view;

import rt.infrastructure.notifier.Notifier;
import rt.service_manager.ServiceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class View {

    protected final ExecutorService executor = Executors.newSingleThreadExecutor();
    protected ServiceManager serviceManager;

    public abstract void startInteractions();

    public abstract void print(String text);

    public abstract void showPresets();

    public abstract String ask2FAPassword();

    public abstract void showErrorMessage(String errorText);

    public abstract void showQrCode(String link);

    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public void startNotificationListener() {
        executor.execute(() -> {
            try {
                Thread.sleep(200);
                while (!Thread.interrupted()) {
                    print(Notifier.getInstance().getNotification());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void stopNotificationListener() {
        executor.shutdownNow();
    }

    public void load(String source, String dateFromString, String dateToString) {
        serviceManager.load(source, dateFromString, dateToString);
    }

    public void find(String[] args) {
        serviceManager.find(args);
    }

    public void classify() {
        if (serviceManager.analyzerIsAvailable()) {
            serviceManager.classify();
        } else {
            print("Анализатор выключен");
        }
    }

    public void write(String value) {
        serviceManager.write(value);
    }

    public void clear() {
        serviceManager.clear();
    }

    public void stopParser() {
        serviceManager.close();
    }

    public void logout() {
        serviceManager.logout();
    }

    public void setMaxAmount(String arg) {
        serviceManager.setMessagesToDownload(arg);
    }

    public void usePreset(String name) {
        serviceManager.usePresetByName(name);
    }

    public void renamePreset(String oldName, String newName) {
        serviceManager.renamePresetByName(oldName, newName);
    }

    public void removePresetByName(String name) {
        serviceManager.removePresetByName(name);
    }
}