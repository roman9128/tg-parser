package rt.model.service;

import it.tdlight.jni.TdApi;
import rt.model.note.Note;

public interface NoteStorageService {
    void createNote(TdApi.Message message, String senderName, String link);

    int getAllNotesQuantity();

    int getSuitableNotesQuantity();

    boolean noAnyNotes();

    boolean noSuitableNotes();

    void findNotesByText(String how, String[] what);

    void findNotesByTopic(String how, String[] what);

    String getWordsStat();

    Iterable<Note> getNotesCommonPool();

    Iterable<Note> getChosenNotesPool();

    Note takeNoteFromCommonPool();

    Note takeNoteFromChosenNotesPool();

    void clearAll();

    void clearChosen();

    void removeCopies();
}