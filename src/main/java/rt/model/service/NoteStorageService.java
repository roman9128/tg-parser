package rt.model.service;

import it.tdlight.jni.TdApi;
import rt.model.note.Note;

import java.util.Map;

public interface NoteStorageService {
    void createNote(TdApi.Message message, String senderName);

    int getAllNotesQuantity();

    int getSuitableNotesQuantity();

    boolean noAnyNotes();

    boolean noSuitableNotes();

    void findNotesByText(String how, String[] what);

    void findNotesByTopic(String how, Map<String, Double> what);

    String getWordsStat();

    Iterable<Note> getNotesCommonPool();

    Iterable<Note> getChosenNotesPool();

    Note takeNoteFromCommonPool();

    Note takeNoteFromChosenNotesPool();

    void clearAll();

    void clearChosen();
}