package rt.view.gui;

import rt.infrastructure.notifier.Notifier;
import rt.presenter.Presenter;
import rt.presenter.analyzer.AnalyzerPresenter;
import rt.presenter.parser.ParserPresenter;
import rt.presenter.recorder.RecorderPresenter;
import rt.presenter.storage.StoragePresenter;
import rt.view.View;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SwingUI implements View {

    private ParserPresenter parserPresenter;
    private StoragePresenter storagePresenter;
    private RecorderPresenter recorderPresenter;
    private AnalyzerPresenter analyzerPresenter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private AuthWindow authWindow;
    private MainWindow mainWindow;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss - ");

    public SwingUI() {
        authWindow = new AuthWindow();
        mainWindow = new MainWindow(this);
    }

    @Override
    public void setPresenters(Presenter presenter1, Presenter presenter2, Presenter presenter3, Presenter presenter4) {
        this.parserPresenter = (ParserPresenter) presenter1;
        this.storagePresenter = (StoragePresenter) presenter2;
        this.recorderPresenter = (RecorderPresenter) presenter3;
        if (presenter4 instanceof AnalyzerPresenter) {
            this.analyzerPresenter = (AnalyzerPresenter) presenter4;
        } else {
            this.analyzerPresenter = null;
        }
    }

    @Override
    public void startInteractions() {
        mainWindow.setVisible(true);
        authWindow.dispose();
    }

    @Override
    public void startNotificationListener() {
        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    print(Notifier.getInstance().getNotification());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    @Override
    public void stopNotificationListener() {
        executor.shutdownNow();
    }

    @Override
    public String showFolders() {
        return parserPresenter.show();
    }

    public void createLoadingWindow() {
        new LoadingWindow(this);
    }

    @Override
    public void load(String folderIDString, String dateFromString, String dateToString) {
        parserPresenter.load(folderIDString, dateFromString, dateToString);
    }

    @Override
    public void find(String[] args) {

    }

    @Override
    public void write(String value) {
        recorderPresenter.write(value);
    }

    @Override
    public void classify() {
        if (analyzerPresenter != null) {
            analyzerPresenter.classify();
        } else {
            print("Анализатор выключен");
        }
    }

    @Override
    public void clear() {
        storagePresenter.clear();
    }

    @Override
    public void stopParser() {
        parserPresenter.close();
    }

    @Override
    public void logout() {
        parserPresenter.logout();
    }

    @Override
    public void print(String text) {
        mainWindow.getTextArea().append(LocalDateTime.now().format(dtf) + text + System.lineSeparator().repeat(2));
    }

    @Override
    public String askParameter(String who, String question) {
        return authWindow.askParameter(who, question);
    }
}