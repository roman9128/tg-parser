package rt.presenter.recorder;

import rt.infrastructure.recorder.FileRecorder;
import rt.model.service.FileRecorderService;
import rt.model.service.NoteStorageService;
import rt.presenter.Presenter;
import rt.view.View;

public class RecorderPresenter implements Presenter {

    private final View view;
    private final FileRecorderService service;

    public RecorderPresenter(View view, NoteStorageService storage) {
        this.view = view;
        this.service = new FileRecorder(storage);
    }

    public void write(String value) {
        service.write(value.isBlank());
    }
}