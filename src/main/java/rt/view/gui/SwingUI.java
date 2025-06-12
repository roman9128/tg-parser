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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class SwingUI extends JFrame implements View {

    private ParserPresenter parserPresenter;
    private StoragePresenter storagePresenter;
    private RecorderPresenter recorderPresenter;
    private AnalyzerPresenter analyzerPresenter;

    private JPanel buttonPanel;
    private JTextArea outputArea;

    public SwingUI() {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("OptionPane.yesButtonText", "Да");
            UIManager.put("OptionPane.noButtonText", "Нет");
            UIManager.put("OptionPane.cancelButtonText", "Отмена");

            setVisible(false);
            setTitle("Tg-Parser 2.0");
            setSize(600, 600);
            setResizable(false);
            setLocationRelativeTo(null);

            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/logo.png")));
            setIconImage(icon.getImage());

            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    confirmClosure();
                }
            });
        });

        initComponents();
    }

    private void confirmClosure() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Вы уверены, что хотите выйти?",
                "Выход",
                JOptionPane.YES_NO_OPTION
        );
        if (option == JOptionPane.YES_OPTION) {
            stopParser();
            dispose();
        }
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
        setVisible(true);
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
        parserPresenter.close();
    }

    @Override
    public void logout() {

    }

    @Override
    public void print(String text, boolean needNextLine) {

    }

    @Override
    public String askParameter(String who, String question) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        String[] lines = question.split(System.lineSeparator());
        JPanel textPanel = new JPanel(new GridLayout(lines.length, 1, 0, 5));
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        for (String line : lines) {
            textPanel.add(new JLabel(line));
        }
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField field = new JTextField(15);
        field.setMargin(new Insets(5, 5, 5, 5));
        inputPanel.add(field, BorderLayout.SOUTH);
        inputPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
        mainPanel.add(textPanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.EAST);
        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    null,
                    mainPanel,
                    who,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (result == JOptionPane.OK_OPTION) {
                String input = field.getText();
                if (input.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Поле не может быть пустым!",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                return input;
            } else {
                System.exit(0);
            }
        }
    }
}
