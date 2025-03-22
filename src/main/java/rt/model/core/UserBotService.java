package rt.model.core;

import it.tdlight.Init;
import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.client.APIToken;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.client.SimpleTelegramClientFactory;
import it.tdlight.client.TDLibSettings;
import rt.model.authentication.PhoneAuthentication;
import rt.model.auxillaries.PropertyHandler;
import rt.presenter.Printer;
import rt.presenter.ServiceHelper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserBotService {

    private UserBot userBotInstance;

    public void start(ServiceHelper helper) throws Exception {
        Init.init();
        Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());
        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            APIToken apiToken = new APIToken(PropertyHandler.getApiID(), PropertyHandler.getApiHash());
            TDLibSettings settings = TDLibSettings.create(apiToken);
            Path sessionPath = Paths.get("session");
            settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
            SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);
            try (UserBot userBot = new UserBot(clientBuilder, new PhoneAuthentication(), helper)) {
                userBotInstance = userBot;
                helper.startInteractions();
                userBotInstance.getClient().waitForExit();
                Thread.sleep(100); // ожидание завершения соединения с TDLib
            } catch (Exception e) {
                helper.print("Исключение в главном потоке: " + e.getMessage(), true);
            } finally {
                helper.print("Завершаю работу...", true);
                showEndingBar(helper);
            }
        }
    }

    public void show() {
        userBotInstance.showFolders();
    }

    public void clear() {
        userBotInstance.clear();
    }

    public void stop() {
        userBotInstance.close();
    }

    public void logout() {
        userBotInstance.logout();
    }

    public void writeHistoryToFile() {
        ExecutorService writer = Executors.newSingleThreadExecutor();
        writer.execute(userBotInstance::writeHistory);
        writer.shutdown();
    }

    public void loadHistory(String folderIDString, String dateFromString, String dateToString) {
        ExecutorService loader = Executors.newSingleThreadExecutor();
        loader.execute(() -> userBotInstance.loadChannelsHistory(folderIDString, dateFromString, dateToString));
        loader.shutdown();
    }

    private void showEndingBar(Printer printer) {
        CompletableFuture.runAsync(() -> {
            for (int i = 10; i > 0; i--) {
                printer.print("*".repeat(i), false);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
