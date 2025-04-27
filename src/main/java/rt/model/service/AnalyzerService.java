package rt.model.service;

import rt.model.storage.NoteStorageService;

public interface AnalyzerService {
    void classify();

    boolean storageIsEmpty();

    void setStorage(NoteStorageService storage);
}
