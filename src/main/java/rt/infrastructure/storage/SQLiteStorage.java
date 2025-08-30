package rt.infrastructure.storage;

import it.tdlight.jni.TdApi;
import rt.model.note.Note;
import rt.model.service.NoteStorageService;

import java.util.Map;

public class SQLiteStorage implements NoteStorageService {
    @Override
    public void createNote(TdApi.Message message, String senderName) {

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
    public void findNotesByTopic(String how, Map<String, Double> what) {

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
}
