package rt.presenter.parser;

import it.tdlight.client.*;
import rt.infrastructure.analyzer.AnalyzerImpl;
import rt.infrastructure.parser.TgParser;
import rt.infrastructure.recorder.FileRecorder;
import rt.model.service.*;
import rt.nlp.NLPService;
import rt.presenter.Presenter;
import rt.view.View;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ParserPresenter implements Presenter, ParameterRequester, InteractionStarter {

    private ParserService parserService;
    private AnalyzerService analyzerService;
    private final View view;
    private final NoteStorageService storage;
    private final FileRecorderService recorderService;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory();

    public ParserPresenter(View view, NoteStorageService storage) {
        this.view = view;
        this.storage = storage;
        this.recorderService = new FileRecorder(storage);
        try {
            analyzerService = new AnalyzerImpl(storage, new NLPService());
        } catch (Exception e) {
            analyzerService = null;
        }
    }

    public void initService() {
        executor.execute(() -> {
                    try {
                        parserService = new TgParser(clientFactory, storage, this, this);
                        view.startNotificationListener();
                        view.print("Готов к работе");
                        parserService.waitForExit();
                        Thread.sleep(100); // ожидание завершения соединения с TDLib
                    } catch (Exception e) {
                        view.print("Исключение в главном потоке: " + e.getMessage());
                    } finally {
                        view.print("Завершаю работу...");
                        view.stopNotificationListener();
                        clientFactory.close();
                        executor.shutdown();
                    }
                }
        );
    }

    @Override
    public void startInteractions() {
        executor.execute(view::startInteractions);
    }

    public Map<Integer, String> getFoldersIDsAndNames() {
        return parserService.getFoldersIDsAndNames();
    }

    public Map<Long, String> getChannelsIDsAndNames() {
        return parserService.getChannelsIDsAndNames();
    }

    public void load(String userChoiceInput, String dateFromString, String dateToString) {
        Set<Long> channelsIDs = parseUserChoice(userChoiceInput);
        Long dateFromUnix = NumbersParserUtil.parseUnixDateStartOfDay(dateFromString);
        Long dateToUnix = NumbersParserUtil.parseUnixDateEndOfDay(dateToString);
        parserService.loadChannelsHistory(channelsIDs, dateFromUnix, dateToUnix);
    }

    private Set<Long> parseUserChoice(String input) {
        Set<Long> result = new TreeSet<>();
        Stream.of(input.split(","))
                .map(NumbersParserUtil::parseLongOrGetZero)
                .forEach(n -> {
                    if (n < 0) {
                        result.add(n);
                    } else if (n > 0) {
                        result.addAll(parserService.getChatsInFolder(n.intValue()));
                    }
                });
        return result;
    }

    public void close() {
        parserService.close();
    }

    public void logout() {
        parserService.logout();
    }

    @Override
    public String askParameter(String who, String question) {
        return view.askParameter(who, question);
    }

    public void setMessagesToDownload(String value) {
        parserService.setMessagesToDownload(NumbersParserUtil.parseIntegerOrGetZero(value));
    }

    public void write(String value) {
        recorderService.write(value.isBlank());
    }

    public boolean analyzerIsAvailable() {
        return analyzerService != null;
    }

    public void classify() {
        analyzerService.classify();
    }
}