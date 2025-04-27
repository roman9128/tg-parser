package rt.model.service;

import rt.model.storage.NoteStorageService;
import rt.presenter.recorder.TextPrinter;

public interface FileRecorderService {
    void write(TextPrinter printer, boolean writeAll);

    void setStorage(NoteStorageService storage);
}
