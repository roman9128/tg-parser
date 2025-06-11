package rt.view.gui;

import rt.infrastructure.analyzer.AnalyzerImpl;
import rt.infrastructure.storage.NoteStorage;
import rt.model.service.AnalyzerService;
import rt.model.service.NoteStorageService;
import rt.nlp.NLPService;
import rt.presenter.Presenter;
import rt.presenter.analyzer.AnalyzerPresenter;
import rt.presenter.parser.ParserPresenter;
import rt.presenter.recorder.RecorderPresenter;
import rt.presenter.storage.StoragePresenter;
import rt.view.View;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements View {

    private ParserPresenter parserPresenter;
    private StoragePresenter storagePresenter;
    private RecorderPresenter recorderPresenter;
    private AnalyzerPresenter analyzerPresenter;

    private JTextArea outputArea;

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.initializeApplication();
            frame.setVisible(true);
        });
    }

    private MainFrame() {
        super("Tg-Parser 2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        JToolBar toolBar = new JToolBar();
        toolBar.add(new JButton("Парсер"));
        toolBar.add(new JButton("Анализатор"));
        toolBar.add(new JButton("Хранилище"));
        toolBar.add(new JButton("Запись"));

        mainPanel.add(toolBar, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void initializeApplication() {
        NoteStorageService storage = new NoteStorage();

        AnalyzerPresenter analyzerPresenter;
        try {
            AnalyzerService analyzerService = new AnalyzerImpl(storage, new NLPService());
            analyzerPresenter = new AnalyzerPresenter(this, analyzerService);
        } catch (Exception e) {
            print("Ошибка в анализаторе: " + e.getMessage(), true);
            analyzerPresenter = null;
        }

        ParserPresenter parserPresenter = new ParserPresenter(this, storage);
        StoragePresenter storagePresenter = new StoragePresenter(this, storage);
        RecorderPresenter recorderPresenter = new RecorderPresenter(this, storage);
        this.setPresenters(parserPresenter, storagePresenter, recorderPresenter, analyzerPresenter);

        startNotificationListener();
        parserPresenter.initService();
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

    }

    @Override
    public void startNotificationListener() {

    }

    @Override
    public void stopNotificationListener() {

    }

    @Override
    public void load(String folderIDString, String dateFromString, String dateToString) {

    }

    @Override
    public void find(String[] args) {

    }

    @Override
    public void write(String value) {

    }

    @Override
    public void classify() {

    }

    @Override
    public void clear() {

    }

    @Override
    public void stopParser() {

    }

    @Override
    public void logout() {

    }

    @Override
    public void print(String text, boolean needNextLine) {

    }

    @Override
    public String askParameter(String who, String question) {
        return "";
    }
}
