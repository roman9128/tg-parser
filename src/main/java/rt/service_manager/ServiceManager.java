package rt.service_manager;

import it.tdlight.client.*;
import rt.infrastructure.analyzer.AnalyzerImpl;
import rt.infrastructure.parser.TgParser;
import rt.infrastructure.recorder.FileRecorder;
import rt.model.service.*;
import rt.nlp.NLPService;
import rt.service_manager.NumbersParserUtil;
import rt.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ServiceManager implements ParameterRequester, InteractionStarter {

    private ParserService parserService;
    private AnalyzerService analyzerService;
    private final View view;
    private final NoteStorageService storage;
    private final FileRecorderService recorderService;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory();

    public ServiceManager(View view, NoteStorageService storage) {
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

    public void find(String[] args) {
        if (storage.noAnyNotes()) {
            view.print("Сначала нужно загрузить сообщения");
            return;
        }
        String how = args[0].toLowerCase();
        String where = args[1].toLowerCase();
        String what = args[2].toLowerCase();
        if (how.isBlank() || where.isBlank() || what.isBlank()) {
            view.print("Не заданы параметры поиска");
            return;
        }

        if (!(how.equals("and") || how.equals("or") || how.equals("not"))) {
            view.print("Не введено логическое условие and, or или not");
            return;
        }

        if (where.equals("topic")) {
            findNotesByTopic(how, what.split("\\s+"));
        } else if (where.equals("text")) {
            findNotesByText(how, what.split("\\s+"));
        } else {
            view.print("Неверный параметр. Нужно topic или text");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Поиск завершён").append(System.lineSeparator());
        if (storage.noSuitableNotes()) {
            sb.append("Нет отобранных сообщений").append(System.lineSeparator());
        } else {
            sb.append("Всего отобрано сообщений: ")
                    .append(storage.getSuitableNotesQuantity())
                    .append(System.lineSeparator());
            if (where.equals("text")) {
                sb.append("Количество сообщений, содержащих слова для поиска")
                        .append(System.lineSeparator())
                        .append(storage.getWordsStat());
            }
        }
        view.print(sb.toString());
    }

    private void findNotesByTopic(String how, String[] what) {
        Map<String, Double> result = new HashMap<>();
        for (String s : what) {
            result.putAll(getParams(s));
        }
        storage.findNotesByTopic(how, result);
    }

    private Map<String, Double> getParams(String s) {
        String[] paramsPair = s.split("-", 2);
        Map<String, Double> result = new HashMap<>();
        try {
            result.put(paramsPair[0], Double.parseDouble(paramsPair[1]));
        } catch (Exception e) {
            result.put(paramsPair[0], 55.0); // default value
        }
        return result;
    }

    private void findNotesByText(String how, String[] what) {
        storage.findNotesByText(how, what);
    }

    public void clear() {
        storage.clearAll();
        view.print("Все загруженные сообщения удалены");
    }

    public boolean noAnyNotes() {
        return storage.noAnyNotes();
    }

    public boolean noSuitableNotes() {
        return storage.noSuitableNotes();
    }
}