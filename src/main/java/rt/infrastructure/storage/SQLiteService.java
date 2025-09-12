package rt.infrastructure.storage;

import it.tdlight.jni.TdApi;
import rt.infrastructure.analyzer.Analyzer;
import rt.model.entity.Entity;
import rt.model.note.Note;
import rt.model.service.NoteStorageService;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SQLiteService implements NoteStorageService {
    private final SQLiteConnector connector = new SQLiteConnector();
    private final Analyzer analyzer = new Analyzer();

    public SQLiteService() {
        connector.createTables();
    }

    @Override
    public void createNote(TdApi.Message message, String senderName, String link) {
        String text = "";
        TdApi.MessageContent messageContent = message.content;
        switch (messageContent) {
            case TdApi.MessageText mt -> {
                text = mt.text.text;
            }
            case TdApi.MessagePhoto mp -> {
                text = "Фото. " + mp.caption.text;
            }
            case TdApi.MessageVideo mv -> {
                text = "Видео. " + mv.caption.text;
            }
            case TdApi.MessageDocument md -> {
                text = "Документ. " + md.caption.text;
            }
            default -> {
                text = "Сообщение без текста" + System.lineSeparator();
            }
        }

        Set<String> topic = analyzer.classify(text)
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 55)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<Entity> ner = analyzer.recognizeNE(text);
        connector.addNote(new Note(message.id, message.chatId, message.date, senderName, text, link, topic, ner));
    }

    @Override
    public int getAllNotesQuantity() {
        return 0;
    }

    @Override
    public int getSuitableNotesQuantity() {
        return 0;
    }

    @Override
    public boolean noAnyNotes() {
        return false;
    }

    @Override
    public boolean noSuitableNotes() {
        return false;
    }

    @Override
    public void findNotesByText(String how, String[] what) {

    }

    @Override
    public void findNotesByTopic(String how, String[] what) {

    }

    @Override
    public String getWordsStat() {
        return "";
    }

    @Override
    public Iterable<Note> getNotesCommonPool() {
        return null;
    }

    @Override
    public Iterable<Note> getChosenNotesPool() {
        return null;
    }

    @Override
    public Note takeNoteFromCommonPool() {
        return null;
    }

    @Override
    public Note takeNoteFromChosenNotesPool() {
        return null;
    }

    @Override
    public void clearAll() {

    }

    @Override
    public void clearChosen() {

    }

    @Override
    public void removeCopies() {

    }

    private String[] removeDuplicates(String[] array) {
        return Arrays.stream(array)
                .map(String::toLowerCase)
                .distinct()
                .toArray(String[]::new);
    }
}
