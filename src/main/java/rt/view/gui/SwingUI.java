package rt.view.gui;

import rt.infrastructure.notifier.Notifier;
import rt.presenter.Presenter;
import rt.presenter.analyzer.AnalyzerPresenter;
import rt.presenter.parser.ParserPresenter;
import rt.presenter.recorder.RecorderPresenter;
import rt.presenter.storage.StoragePresenter;
import rt.view.View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
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
    private LoadingWindow loadingWindow;
    private SearchWindow searchWindow;

//    private void initLoadingParamsWindow() {
//        JDialog loadingParamsWindow = new JDialog(this, "Параметры загрузки", true);
//        loadingParamsWindow.setSize(400, 200);
//        loadingParamsWindow.setResizable(false);
//        loadingParamsWindow.setLayout(new BorderLayout(5, 5));
//        loadingParamsWindow.getRootPane().setBorder(new EmptyBorder(5, 5, 5, 5));
//
//        JFormattedTextField startDateField = createDateField();
//        JFormattedTextField endDateField = createDateField();
//
//        addDigitFilter(startDateField);
//        addDigitFilter(endDateField);
//
//        String[] folders = parserPresenter.show().split(System.lineSeparator());
//        DefaultListModel<String> listModel = new DefaultListModel<>();
//        listModel.addElement("Все");
//        for (String folder : folders) {
//            folder = folder.split(": ")[1];
//            listModel.addElement(folder);
//        }
//
//        JList<String> itemList = new JList<>(listModel);
//        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        itemList.setFont(new Font("Arial", Font.PLAIN, 12));
//
//        JScrollPane scrollPane = new JScrollPane(itemList);
//        scrollPane.setBorder(BorderFactory.createTitledBorder("Папки"));
//        scrollPane.setPreferredSize(new Dimension(150, 0));
//
//        JPanel datePanel = new JPanel(new GridLayout(2, 2, 5, 5));
//        datePanel.setBorder(BorderFactory.createTitledBorder("Период"));
//
//        datePanel.add(new JLabel("С "));
//        datePanel.add(startDateField);
//        datePanel.add(new JLabel("По "));
//        datePanel.add(endDateField);
//
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
//        JButton cancelButton = createButton("Отмена", Color.WHITE);
//        JButton okButton = createButton("OK", Color.WHITE);
//
//        cancelButton.addActionListener(e -> loadingParamsWindow.dispose());
//
//        okButton.addActionListener(e -> {
//            System.out.println("Выбрано: " + itemList.getSelectedValue());
//            System.out.println("Период: " + startDateField.getText() + " - " + endDateField.getText());
//            loadingParamsWindow.setVisible(false);
//        });
//
//        buttonPanel.add(cancelButton);
//        buttonPanel.add(okButton);
//
//        loadingParamsWindow.add(scrollPane, BorderLayout.WEST);
//        loadingParamsWindow.add(datePanel, BorderLayout.CENTER);
//        loadingParamsWindow.add(buttonPanel, BorderLayout.SOUTH);
//
//        loadingParamsWindow.setLocationRelativeTo(this);
//        loadingParamsWindow.setVisible(true);
//    }

    public SwingUI() {
        mainWindow = new MainWindow(this);
    }

    private void addDigitFilter(JFormattedTextField field) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!(Character.isDigit(c) || c == '.' || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)) {
                    e.consume();
                }
            }
        });
    }

    private void setupDateField(JFormattedTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setColumns(10);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setToolTipText("Формат: ДД.ММ.ГГГГ (можно оставить пустым)");
        field.setValue("");
    }

    private JFormattedTextField createDateField() {
        try {
            MaskFormatter dateFormatter = new MaskFormatter("##.##.####");
            dateFormatter.setPlaceholderCharacter(' ');
            dateFormatter.setAllowsInvalid(true);
            dateFormatter.setOverwriteMode(false);

            JFormattedTextField field = new JFormattedTextField(dateFormatter);
            setupDateField(field);
            return field;
        } catch (ParseException e) {
            JFormattedTextField field = new JFormattedTextField();
            setupDateField(field);
            return field;
        }
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(80, 25));
        return button;
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
    public void showFolders() {

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

    private void closeAllWindows() {
        authWindow.dispose();
        mainWindow.dispose();
        loadingWindow.dispose();
        searchWindow.dispose();
    }

    @Override
    public void print(String text) {
        mainWindow.getTextArea().append(text + System.lineSeparator());
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
