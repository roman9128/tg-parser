package rt;

import rt.infrastructure.storage.NoteStorage;
import rt.model.service.NoteStorageService;
import rt.presenter.analyzer.AnalyzerPresenter;
import rt.presenter.parser.ParserPresenter;
import rt.presenter.recorder.RecorderPresenter;
import rt.presenter.storage.StoragePresenter;
import rt.view.View;
import rt.view.console.ConsoleUI;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        View view = new ConsoleUI();
        NoteStorageService storage = new NoteStorage();

        ParserPresenter parserPresenter = new ParserPresenter(view, storage);
        StoragePresenter storagePresenter = new StoragePresenter(view, storage);
        RecorderPresenter recorderPresenter = new RecorderPresenter(view, storage);
        AnalyzerPresenter analyzerPresenter = new AnalyzerPresenter(view, storage);

        view.setPresenters(List.of(parserPresenter, storagePresenter, recorderPresenter));

        parserPresenter.initService();
    }
}