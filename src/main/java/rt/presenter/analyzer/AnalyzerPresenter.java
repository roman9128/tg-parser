package rt.presenter.analyzer;

import rt.infrastructure.analyzer.AnalyzerImpl;
import rt.model.service.AnalyzerService;
import rt.model.storage.NoteStorageService;
import rt.presenter.Presenter;
import rt.view.View;

public class AnalyzerPresenter implements Presenter {

    private final View view;
    private final AnalyzerService service;

    public AnalyzerPresenter(View view, NoteStorageService storage) {
        this.view = view;
        this.service = new AnalyzerImpl();
        service.setStorage(storage);
    }

    public void classify() {
        if (service.storageIsEmpty()) {
            view.print("Нечего анализировать", true);
            return;
        }
        service.classify();
    }
}
