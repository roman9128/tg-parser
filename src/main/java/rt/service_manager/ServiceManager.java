package rt.service_manager;

import it.tdlight.client.SimpleTelegramClientFactory;
import rt.infrastructure.notifier.Notifier;
import rt.infrastructure.parser.TgParser;
import rt.infrastructure.preset.Presetter;
import rt.infrastructure.recorder.FileRecorder;
import rt.infrastructure.storage.NoteStorage;
import rt.model.preset.Preset;
import rt.model.preset.PresetDTO;
import rt.model.service.FileRecorderService;
import rt.model.service.NoteStorageService;
import rt.model.service.ParserService;
import rt.model.service.PresetService;
import rt.utils.NumberUtils;
import rt.utils.DateTimeUtils;
import rt.view.View;
import rt.view.swing.SwingUI;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceManager implements InteractionStarter, ErrorInformer {

    private ParserService parserService;
    private final View view;
    private final NoteStorageService storage;
    private final FileRecorderService recorderService;
    private final PresetService presetService;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory();

    public ServiceManager() {
        this.view = new SwingUI();
        view.setServiceManager(this);
        this.storage = new NoteStorage();
        this.recorderService = new FileRecorder(storage);
        this.presetService = new Presetter();
    }

    public void init() {
        executor.execute(() -> {
                    try {
                        startParser();
                    } catch (Exception e) {
                        view.print("Исключение в главном потоке: " + e.getMessage());
                    } finally {
                        closeApp();
                    }
                }
        );
    }

    private void startParser() throws InterruptedException {
        parserService = new TgParser(
                clientFactory,
                storage,
                this,
                this);
        view.startNotificationListener();
        Notifier.getInstance().addNotification("Готов к работе");
        parserService.waitForExit();
        Thread.sleep(100); // ожидание завершения соединения с TDLib
    }

    @Override
    public void startInteractions() {
        executor.execute(view::startInteractions);
    }

    @Override
    public void showQrCode(String link) {
        view.showQrCode(link);
    }

    public Map<Integer, String> getFoldersIDsAndNames() {
        return parserService.getFoldersIDsAndNames();
    }

    public Map<Long, String> getChannelsIDsAndNames() {
        return parserService.getChannelsIDsAndNames();
    }

    public void load(String userChoiceInput, String dateFromString, String dateToString) {
        Set<Long> channelsIDs = parseUserChoice(userChoiceInput);
        Long dateFromUnix = DateTimeUtils.parseUnixDateStartOfDay(dateFromString);
        Long dateToUnix = DateTimeUtils.parseUnixDateEndOfDay(dateToString);
        executor.execute(() -> {
            for (Long channelID : channelsIDs) {
                parserService.loadChannelsHistory(channelID, dateFromUnix, dateToUnix);
            }
            Notifier.getInstance().addNotification("Загрузка окончена");
        });
        createPreset(userChoiceInput, dateFromString, dateToString);
    }

    private Set<Long> parseUserChoice(String input) {
        Set<Long> result = new TreeSet<>();
        Stream.of(input.split(","))
                .map(NumberUtils::parseLongOrGetZero)
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
    public String ask2FAPassword() {
        return view.ask2FAPassword();
    }

    public void setMessagesToDownload(String value) {
        parserService.setMessagesToDownload(NumberUtils.parseIntegerOrGetZero(value));
    }

    public void write(String value) {
        recorderService.write(value.isBlank());
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
        storage.findNotesByTopic(how, what);
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

    private void createPreset(String source, String dateFromString, String dateToString) {
        LocalDate start = DateTimeUtils.parseStringToLocalDateOrGetNull(dateFromString);
        LocalDate end = DateTimeUtils.parseStringToLocalDateOrGetNull(dateToString);
        presetService.createPreset(source, start, end);
    }

    public void usePresetByName(String name) {
        Preset preset = presetService.getPresetByName(name.trim());
        if (preset == null) {
            Notifier.getInstance().addNotification("Нет такого запроса");
            return;
        }
        load(preset.getSource(), DateTimeUtils.calculateDate(preset.getStart()), DateTimeUtils.calculateDate(preset.getEnd()));
    }

    public void renamePresetByName(String oldName, String newName) {
        presetService.renamePreset(oldName, newName);
    }

    public void removePresetByName(String name) {
        presetService.removePresetByName(name);
    }

    public List<PresetDTO> getPresets() {
        List<PresetDTO> presetDTOList = new ArrayList<>();
        for (Preset preset : presetService.getAllPresets().values()) {
            String name = preset.getName();
            Set<String> folders = extractFoldersNames(preset.getSource());
            Set<String> channels = extractChannelsNames(preset.getSource());
            String start = DateTimeUtils.calculateDate(preset.getStart());
            String end = DateTimeUtils.calculateDate(preset.getEnd());
            presetDTOList.add(new PresetDTO(name, folders, channels, start, end));
        }
        return presetDTOList;
    }

    private Set<String> extractFoldersNames(String source) {
        return Stream.of(source.split(","))
                .map(NumberUtils::parseLongOrGetZero)
                .filter(n -> n > 0)
                .map(Long::intValue)
                .map(getFoldersIDsAndNames()::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<String> extractChannelsNames(String source) {
        return Stream.of(source.split(","))
                .map(NumberUtils::parseLongOrGetZero)
                .filter(n -> n < 0)
                .map(getChannelsIDsAndNames()::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public void informAboutError(String errorText) {
        view.showErrorMessage(errorText);
    }

    public void closeApp() {
        view.print("Завершаю работу...");
        view.stopNotificationListener();
        clientFactory.close();
        executor.shutdown();
    }
}