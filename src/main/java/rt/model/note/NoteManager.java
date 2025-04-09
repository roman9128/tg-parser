package rt.model.note;

import it.tdlight.jni.TdApi;

import java.util.concurrent.ConcurrentLinkedDeque;

public class NoteManager {
    private final ConcurrentLinkedDeque<Note> notes = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Note> chosen_notes = new ConcurrentLinkedDeque<>();
    private final NoteAnalyzer noteAnalyzer = new NoteAnalyzer();

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

    public void clear() {
        notes.clear();
        chosen_notes.clear();
    }

    public void findNotes(String[] args) {
        chosen_notes.addAll(notes);
        chosen_notes.removeIf(note -> !noteAnalyzer.noteIsSuitable(note, args));
    }

    public Note takeChosenNote() {
        return chosen_notes.pollFirst();
    }

    public int getSuitableNotesQuantity() {
        return chosen_notes.size();
    }
}