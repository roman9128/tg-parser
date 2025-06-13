package rt.view.gui;

import rt.presenter.Presenter;
import rt.presenter.analyzer.AnalyzerPresenter;
import rt.presenter.parser.ParserPresenter;
import rt.presenter.recorder.RecorderPresenter;
import rt.presenter.storage.StoragePresenter;
import rt.view.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class SwingUI extends JFrame implements View {

    private ParserPresenter parserPresenter;
    private StoragePresenter storagePresenter;
    private RecorderPresenter recorderPresenter;
    private AnalyzerPresenter analyzerPresenter;

    private JTextArea textArea;

    public SwingUI() {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("OptionPane.yesButtonText", "Да");
            UIManager.put("OptionPane.noButtonText", "Нет");
            UIManager.put("OptionPane.cancelButtonText", "Отмена");

            setVisible(false);
            setTitle("Tg-Parser 2.0");
            setSize(500, 350);
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
        JButton loadButton = new JButton("Загрузить");
        JButton analyzeButton = new JButton("Анализировать");
        JButton findButton = new JButton("Найти");
        JButton writeButton = new JButton("Записать");
        JButton clearButton = new JButton("Очистить");
        JButton closeButton = new JButton("Закрыть программу");
        JButton logoutButton = new JButton("Выйти из аккаунта");

        Dimension buttonSize = new Dimension(150, 30);
        loadButton.setPreferredSize(buttonSize);
        analyzeButton.setPreferredSize(buttonSize);
        findButton.setPreferredSize(buttonSize);
        writeButton.setPreferredSize(buttonSize);
        clearButton.setPreferredSize(buttonSize);
        closeButton.setPreferredSize(buttonSize);
        logoutButton.setPreferredSize(buttonSize);
        loadButton.setMaximumSize(buttonSize);
        analyzeButton.setMaximumSize(buttonSize);
        findButton.setMaximumSize(buttonSize);
        writeButton.setMaximumSize(buttonSize);
        clearButton.setMaximumSize(buttonSize);
        closeButton.setMaximumSize(buttonSize);
        logoutButton.setMaximumSize(buttonSize);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(150, 350));

        JPanel mainButtonsPanel = new JPanel();
        mainButtonsPanel.setLayout(new BoxLayout(mainButtonsPanel, BoxLayout.Y_AXIS));
        mainButtonsPanel.add(Box.createVerticalStrut(10));
        mainButtonsPanel.add(loadButton);
        mainButtonsPanel.add(Box.createVerticalStrut(10));
        mainButtonsPanel.add(analyzeButton);
        mainButtonsPanel.add(Box.createVerticalStrut(10));
        mainButtonsPanel.add(findButton);
        mainButtonsPanel.add(Box.createVerticalStrut(10));
        mainButtonsPanel.add(writeButton);
        mainButtonsPanel.add(Box.createVerticalStrut(10));
        mainButtonsPanel.add(clearButton);
        mainButtonsPanel.add(Box.createVerticalGlue());

        JPanel bottomButtonsPanel = new JPanel();
        bottomButtonsPanel.setLayout(new BoxLayout(bottomButtonsPanel, BoxLayout.Y_AXIS));
        bottomButtonsPanel.add(logoutButton);
        bottomButtonsPanel.add(Box.createVerticalStrut(10));
        bottomButtonsPanel.add(closeButton);
        bottomButtonsPanel.add(Box.createVerticalStrut(10));

        leftPanel.add(mainButtonsPanel, BorderLayout.CENTER);
        leftPanel.add(bottomButtonsPanel, BorderLayout.SOUTH);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(350, 350));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.append("Нажата кнопка: Загрузить\n");
            }
        });

        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.append("Нажата кнопка: Анализировать\n");
            }
        });

        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.append("Нажата кнопка: Найти\n");
            }
        });

        writeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.append("Нажата кнопка: Записать\n");
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.append("Нажата кнопка: Очистить\n");
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.append("Нажата кнопка: Выйти из аккаунта\n");
            }
        });
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
        if (analyzerPresenter != null) {
            analyzerPresenter.classify();
        } else {
            print("Анализатор выключен", true);
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
