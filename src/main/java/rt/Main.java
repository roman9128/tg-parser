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
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        View view = new ConsoleUI();
        NoteStorageService storage = new NoteStorage();
        ParserPresenter parserPresenter = new ParserPresenter(view, storage);
        StoragePresenter storagePresenter = new StoragePresenter(view, storage);
        RecorderPresenter recorderPresenter = new RecorderPresenter(view, storage);
        AnalyzerPresenter analyzerPresenter = new AnalyzerPresenter(view, storage);
        view.setPresenters(List.of(parserPresenter, storagePresenter, recorderPresenter, analyzerPresenter));

        view.startNotificationListener();
        parserPresenter.initService();
        view.stopNotificationListener();
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