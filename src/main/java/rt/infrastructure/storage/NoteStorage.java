package rt.infrastructure.storage;

import it.tdlight.jni.TdApi;
import rt.infrastructure.analyzer.Analyzer;
import rt.model.entity.Entity;
import rt.model.note.Note;
import rt.model.service.NoteStorageService;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class NoteStorage implements NoteStorageService {

    private ConcurrentLinkedDeque<Note> notes = new ConcurrentLinkedDeque<>();
    private Deque<Note> chosen_notes = new ArrayDeque<>();
    private final HashMap<String, Integer> argsMap = new HashMap<>();
    private final NoteFinder noteFinder = new NoteFinder();
    private final NotesCounter notesCounter = new NotesCounter();
    private final Analyzer analyzer = new Analyzer();

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
        notes.offer(new Note(message.id, message.chatId, message.date, senderName, text, link, topic, ner));
    }

    @Override
    public Note takeNoteFromCommonPool() {
        return notes.pollFirst();
    }

    @Override
    public Note takeNoteFromChosenNotesPool() {
        return chosen_notes.pollFirst();
    }

    @Override
    public int getAllNotesQuantity() {
        return notes.size();
    }

    @Override
    public int getSuitableNotesQuantity() {
        return chosen_notes.size();
    }

    @Override
    public boolean noAnyNotes() {
        return notes.isEmpty();
    }

    @Override
    public boolean noSuitableNotes() {
        return chosen_notes.isEmpty();
    }

    @Override
    public void clearAll() {
        notes.clear();
        chosen_notes.clear();
        argsMap.clear();
    }

    @Override
    public void clearChosen() {
        chosen_notes.clear();
        argsMap.clear();
    }

    @Override
    public void findNotesByText(String how, String[] what) {
        Arrays.stream(what).forEach(arg -> argsMap.put(arg, 0));
        for (Note note : notes) {
            if (noteFinder.textMeetsConditions(note, how, what)) {
                chosen_notes.addLast(note);
            }
        }
        removeCopies();
        countNotesWithArgs();
    }

    @Override
    public void findNotesByTopic(String how, String[] what) {
        for (Note note : notes) {
            if (noteFinder.topicsMeetConditions(note, how, what)) {
                chosen_notes.addLast(note);
            }
        }
        removeCopies();
    }

    @Override
    public String getWordsStat() {
        Map<String, Integer> sortedMap = argsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return sortedMap.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @Override
    public ConcurrentLinkedDeque<Note> getNotesCommonPool() {
        return notes;
    }

    @Override
    public Deque<Note> getChosenNotesPool() {
        return chosen_notes;
    }

    @Override
    public void removeCopies() {
        notes = notes.stream().distinct().collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
        if (!noSuitableNotes()) {
            chosen_notes = new ArrayDeque<>(new LinkedHashSet<>(chosen_notes));
        }
    }

    private void countNotesWithArgs() {
        for (Note chosenNote : chosen_notes) {
            for (String arg : argsMap.keySet()) {
                notesCounter.count(chosenNote.getText(), arg);
            }
        }
        argsMap.putAll(notesCounter.getArgsMap());
        notesCounter.clear();
    }
}