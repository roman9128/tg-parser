package rt;

import rt.infrastructure.analyzer.AnalyzerImpl;
import rt.infrastructure.storage.NoteStorage;
import rt.model.service.AnalyzerService;
import rt.model.service.NoteStorageService;
import rt.nlp.NLPService;
import rt.presenter.analyzer.AnalyzerPresenter;
import rt.presenter.parser.ParserPresenter;
import rt.presenter.recorder.RecorderPresenter;
import rt.presenter.storage.StoragePresenter;
import rt.view.View;
import rt.view.console.ConsoleUI;

import java.util.Map;

public class ConsoleMain {

    public static void main(String[] args) {
        View view = new ConsoleUI();
        NoteStorageService storage = new NoteStorage();

        AnalyzerPresenter analyzerPresenter;
        try {
            AnalyzerService analyzerService = new AnalyzerImpl(storage, new NLPService());
            analyzerPresenter = new AnalyzerPresenter(view, analyzerService);
        } catch (Exception e) {
            view.print("Ошибка в анализаторе: " + e.getMessage(), true);
            analyzerPresenter = null;
        }

        ParserPresenter parserPresenter = new ParserPresenter(view, storage);
        StoragePresenter storagePresenter = new StoragePresenter(view, storage);
        RecorderPresenter recorderPresenter = new RecorderPresenter(view, storage);
        view.setPresenters(parserPresenter, storagePresenter, recorderPresenter, analyzerPresenter);

        view.startNotificationListener();
        parserPresenter.initService();
    }

    private static void countThreads() {
        Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
        System.out.println("Active Threads (" + threadMap.size() + "):");
        for (Map.Entry<Thread, StackTraceElement[]> entry : threadMap.entrySet()) {
            Thread thread = entry.getKey();
            System.out.println("  Thread: " + thread.getName() + " (State: " + thread.getState() + ")");
        }
    }
}