package rt.model.note;

import it.tdlight.jni.TdApi;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class NoteManager {

    private final ConcurrentLinkedDeque<Note> notes = new ConcurrentLinkedDeque<>();
    private Deque<Note> chosen_notes = new ArrayDeque<>();
    private HashMap<String, Integer> argsMap = new HashMap<>();
    private final TextMatchNoteFinder noteFinder = new TextMatchNoteFinder();
    private final NotesCounter notesCounter = new NotesCounter();

    public void createNote(TdApi.Message message, String senderName) {
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
        notes.offer(new Note(message.id, message.chatId, message.date, senderName, text));
    }

    public Note take() {
        return notes.pollFirst();
    }

    public int getSize() {
        return notes.size();
    }

    public boolean isEmpty() {
        return notes.isEmpty();
    }

    public boolean noSuitableNotes() {
        return chosen_notes.isEmpty();
    }

    public void clearAll() {
        notes.clear();
        chosen_notes.clear();
        argsMap.clear();
    }

    public void clearChosen() {
        chosen_notes.clear();
        argsMap.clear();
    }

    public void findNotes(String[] args) {
        Arrays.stream(args).forEach(arg -> argsMap.put(arg, 0));
        addSuitableNoteWithOneOfArgs(args);
        removeCopies();
        countNotesWithArgs();
    }

    public Note takeChosenNote() {
        return chosen_notes.pollFirst();
    }

    public int getSuitableNotesQuantity() {
        return chosen_notes.size();
    }

    private void addSuitableNoteWithOneOfArgs(String[] args) {
        for (Note note : notes) {
            if (noteFinder.noteContainsOneOfArgs(note, args)) {
                chosen_notes.addLast(note);
            }
        }
    }

    private void removeCopies() {
        chosen_notes = new ArrayDeque<>(new LinkedHashSet<>(chosen_notes));
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

    public String getStat() {
        Map<String, Integer> sortedMap = argsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return sortedMap.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(System.lineSeparator()));
    }
}