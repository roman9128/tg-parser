package rt.presenter.parser;

import it.tdlight.Init;
import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.client.APIToken;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.client.SimpleTelegramClientFactory;
import it.tdlight.client.TDLibSettings;
import rt.infrastructure.parser.PhoneAuthentication;
import rt.infrastructure.parser.TgParser;
import rt.infrastructure.config.PropertyHandler;
import rt.model.service.ParserService;
import rt.model.service.NoteStorageService;
import rt.presenter.Presenter;
import rt.view.View;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParserPresenter implements Presenter, ServiceHelper {

    private ParserService service;
    private final View view;
    private final NoteStorageService storage;

    public ParserPresenter(View view, NoteStorageService storage) {
        this.view = view;
        this.storage = storage;
    }

    public void initService() {
        try {
            Init.init();
            Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());
            try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
                APIToken apiToken = new APIToken(PropertyHandler.getApiID(), PropertyHandler.getApiHash());
                TDLibSettings settings = TDLibSettings.create(apiToken);
                Path sessionPath = Paths.get("session");
                settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
                SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);
                PhoneAuthentication authData = new PhoneAuthentication(this);
                try (TgParser tgParser = new TgParser(clientBuilder, authData, storage, this)) {
                    service = tgParser;
                    service.waitForExit();
                    Thread.sleep(100); // ожидание завершения соединения с TDLib
                } catch (Exception e) {
                    view.print("Исключение в главном потоке: " + e.getMessage(), true);
                } finally {
                    view.print("Завершаю работу...", true);
                }
            }
        } catch (Exception e) {
            view.print(e.getMessage(), true);
        }
    }

    @Override
    public void startInteractions() {
        CompletableFuture.runAsync(view::startInteractions).exceptionally(e -> {
            System.out.println("Ошибка в консольном потоке: " + e.getMessage());
            return null;
        });
    }

    public void show() {
        service.show();
    }

    public void load(String folderIDString, String dateFromString, String dateToString) {
        ExecutorService loader = Executors.newSingleThreadExecutor();
        loader.execute(() -> service.loadChannelsHistory(folderIDString, dateFromString, dateToString));
        loader.shutdown();
    }

    public void close() {
        service.close();
    }

    public void logout() {
        service.logout();
    }

    @Override
    public void print(String text, boolean needNextLine) {
        view.print(text, needNextLine);
    }

    @Override
    public String askParameter(String who, String question) {
        return view.askParameter(who, question);
    }
}