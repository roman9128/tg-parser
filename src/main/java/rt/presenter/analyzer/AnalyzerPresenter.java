package rt.presenter.analyzer;

import rt.infrastructure.analyzer.AnalyzerImpl;
import rt.model.service.AnalyzerService;
import rt.model.service.NoteStorageService;
import rt.presenter.Presenter;
import rt.view.View;

public class AnalyzerPresenter implements Presenter {

    private final View view;
    private final AnalyzerService service;

    public AnalyzerPresenter(View view, AnalyzerService service) {
        this.view = view;
        this.service = service;
    }

    public void classify() {
        service.classify();
    }

}