package rt.model.core;

import it.tdlight.Init;
import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.client.APIToken;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.client.SimpleTelegramClientFactory;
import it.tdlight.client.TDLibSettings;
import rt.model.authentication.PhoneAuthentication;
import rt.model.utils.PropertyHandler;
import rt.presenter.ServiceHelper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppService {

    private TgParser tgParserInstance;

    public void start(ServiceHelper helper) throws Exception {
        Init.init();
        Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());
        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            APIToken apiToken = new APIToken(PropertyHandler.getApiID(), PropertyHandler.getApiHash());
            TDLibSettings settings = TDLibSettings.create(apiToken);
            Path sessionPath = Paths.get("session");
            settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
            SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);
            PhoneAuthentication authData = new PhoneAuthentication(helper);
            try (TgParser tgParser = new TgParser(clientBuilder, authData, helper)) {
                tgParserInstance = tgParser;
                tgParserInstance.getClient().waitForExit();
                Thread.sleep(100); // ожидание завершения соединения с TDLib
            } catch (Exception e) {
                helper.print("Исключение в главном потоке: " + e.getMessage(), true);
            } finally {
                helper.print("Завершаю работу...", true);
            }
        }
    }

    public void show() {
        tgParserInstance.stopLoadingNewChannels();
        tgParserInstance.showFolders();
    }

    public void clear() {
        tgParserInstance.clear();
    }

    public void stop() {
        tgParserInstance.close();
    }

    public void logout() {
        tgParserInstance.logout();
    }

    public void writeHistoryToFile() {
        ExecutorService writer = Executors.newSingleThreadExecutor();
        writer.execute(tgParserInstance::writeHistory);
        writer.shutdown();
    }

    public void loadHistory(String folderIDString, String dateFromString, String dateToString) {
        tgParserInstance.stopLoadingNewChannels();
        ExecutorService loader = Executors.newSingleThreadExecutor();
        loader.execute(() -> tgParserInstance.loadChannelsHistory(folderIDString, dateFromString, dateToString));
        loader.shutdown();
    }
}