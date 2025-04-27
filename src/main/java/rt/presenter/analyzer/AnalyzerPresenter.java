package rt.presenter.analyzer;

import rt.infrastructure.analyzer.AnalyzerImpl;
import rt.model.service.AnalyzerService;
import rt.model.service.NoteStorageService;
import rt.presenter.Presenter;
import rt.model.service.ResponsePrinter;
import rt.view.View;

public class AnalyzerPresenter implements Presenter, ResponsePrinter {

    private final View view;
    private final AnalyzerService service;

    public AnalyzerPresenter(View view, NoteStorageService storage) {
        this.view = view;
        this.service = new AnalyzerImpl(storage);
    }

    public void classify() {
        service.classify(this);
    }

    @Override
    public void printResponse(String text, boolean needNextLine) {
        view.print(text, needNextLine);
    }
}