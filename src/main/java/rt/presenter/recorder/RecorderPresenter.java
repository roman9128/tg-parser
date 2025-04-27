package rt.presenter.recorder;

import rt.infrastructure.recorder.FileRecorder;
import rt.model.service.FileRecorderService;
import rt.model.service.NoteStorageService;
import rt.presenter.Presenter;
import rt.model.service.ResponsePrinter;
import rt.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecorderPresenter implements Presenter, ResponsePrinter {

    private final View view;
    private final FileRecorderService service;

    public RecorderPresenter(View view, NoteStorageService storage) {
        this.view = view;
        this.service = new FileRecorder(storage);
    }

    public void write(String value) {
        ExecutorService writer = Executors.newSingleThreadExecutor();
        writer.execute(() -> service.write(this, value.isEmpty() || value.isBlank()));
        writer.shutdown();
    }

    @Override
    public void printResponse(String text, boolean needNextLine) {
        view.print(text, needNextLine);
    }
}
