package rt.model.storage;

import it.tdlight.jni.TdApi;
import rt.model.entity.Note;

public interface NoteStorageService {
    void createNote(TdApi.Message message, String senderName);

    int getAllNotesQuantity();

    int getSuitableNotesQuantity();

    boolean noAnyNotes();

    boolean noSuitableNotes();

    void findNotes(String[] args);

    String getStat();

    Iterable<Note> getNotesCommonPool();

    Iterable<Note> getChosenNotesPool();

    Note takeNoteFromCommonPool();

    Note takeNoteFromChosenNotesPool();

    void clearAll();

    void clearChosen();
}
