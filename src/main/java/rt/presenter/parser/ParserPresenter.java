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
import rt.model.service.InteractionStarter;
import rt.model.service.ParameterRequester;
import rt.model.service.ParserService;
import rt.model.service.NoteStorageService;
import rt.presenter.Presenter;
import rt.view.View;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ParserPresenter implements Presenter, ParameterRequester, InteractionStarter {

    private ParserService service;
    private final View view;
    private final NoteStorageService storage;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public ParserPresenter(View view, NoteStorageService storage) {
        this.view = view;
        this.storage = storage;
    }

    public void initService() {
        executor.execute(() -> {
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
                            try (TgParser tgParser = new TgParser(clientBuilder, authData, storage, this, this)) {
                                service = tgParser;
                                view.startNotificationListener();
                                view.print("Готов к работе");
                                service.waitForExit();
                                Thread.sleep(100); // ожидание завершения соединения с TDLib
                            } catch (Exception e) {
                                view.print("Исключение в главном потоке: " + e.getMessage());
                            } finally {
                                view.print("Завершаю работу...");
                                view.stopNotificationListener();
                                executor.shutdown();
                            }
                        }
                    } catch (Exception e) {
                        view.print(e.getMessage());
                    }
                }
        );
    }

    @Override
    public void startInteractions() {
        executor.execute(view::startInteractions);
    }

    public Map<Integer, String> getFoldersIDsAndNames() {
        return service.getFoldersIDsAndNames();
    }

    public Map<Long, String> getChannelsIDsAndNames() {
        return service.getChannelsIDsAndNames();
    }

    public void load(String userChoiceInput, String dateFromString, String dateToString) {
        Set<Long> channelsIDs = parseUserChoice(userChoiceInput);
        Long dateFromUnix = NumbersParserUtil.parseUnixDateStartOfDay(dateFromString);
        Long dateToUnix = NumbersParserUtil.parseUnixDateEndOfDay(dateToString);
        executor.execute(() -> service.loadChannelsHistory(channelsIDs, dateFromUnix, dateToUnix));
    }

    private Set<Long> parseUserChoice(String input) {
        Set<Long> result = new TreeSet<>();
        Stream.of(input.split(","))
                .map(NumbersParserUtil::parseLongOrGetZero)
                .forEach(n -> {
                    if (n < 0) {
                        result.add(n);
                    } else if (n > 0) {
                        result.addAll(service.getChatsInFolder(n.intValue()));
                    }
                });
        return result;
    }

    public void close() {
        service.close();
    }

    public void logout() {
        service.logout();
    }

    @Override
    public String askParameter(String who, String question) {
        return view.askParameter(who, question);
    }
}