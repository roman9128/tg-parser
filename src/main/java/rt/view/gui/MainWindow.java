package rt.view.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

class MainWindow extends JFrame {

    private final Color black = new Color(30, 30, 30);
    private final Color darkGray = new Color(40, 40, 40);
    private final Color gray = new Color(66, 66, 66);
    private final Color white = new Color(200, 200, 200);

    private JTextArea textArea;
    private final SwingUI swingUI;

    MainWindow(SwingUI swingUI) {
        this.swingUI = swingUI;

        SwingUtilities.invokeLater(() -> {
            UIManager.put("OptionPane.yesButtonText", "Да");
            UIManager.put("OptionPane.noButtonText", "Нет");
            UIManager.put("OptionPane.cancelButtonText", "Отмена");
            UIManager.put("ToolTip.background", darkGray);
            UIManager.put("ToolTip.foreground", white);
            UIManager.put("ToolTip.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(white),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            UIManager.put("ToolTip.font", new Font("SansSerif", Font.PLAIN, 12));

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
            initComponents();
        });
    }

    private void initComponents() {
        JButton loadButton = ElementsBuilder.createMainMenuButton("Загрузить", "Начать загрузку сообщений из телеграм-каналов", darkGray, white, gray);
        JButton analyzeButton = ElementsBuilder.createMainMenuButton("Анализировать", "Запустить анализатор текста для определения тематик сообщений", darkGray, white, gray);
        JButton findButton = ElementsBuilder.createMainMenuButton("Найти", "Запустить поиск сообщений по задаваемым параметрам", darkGray, white, gray);
        JButton writeButton = ElementsBuilder.createMainMenuButton("Записать", "Записать все загруженные сообщения в текстовый файл", darkGray, white, gray);
        JButton clearButton = ElementsBuilder.createMainMenuButton("Очистить", "Удалить все загруженные сообщения", darkGray, white, gray);
        JButton logoutButton = ElementsBuilder.createMainMenuButton("Разлогиниться", "Выйти из программы и выйти из учётной записи (потребуется повторная авторизация при новом входе)", darkGray, white, gray);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(darkGray);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(150, 350));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        leftPanel.setBackground(black);

        JPanel mainButtonsPanel = new JPanel();
        mainButtonsPanel.setLayout(new BoxLayout(mainButtonsPanel, BoxLayout.Y_AXIS));
        mainButtonsPanel.setBackground(black);
        mainButtonsPanel.add(Box.createVerticalStrut(5));
        mainButtonsPanel.add(loadButton);
        mainButtonsPanel.add(Box.createVerticalStrut(5));
        mainButtonsPanel.add(analyzeButton);
        mainButtonsPanel.add(Box.createVerticalStrut(5));
        mainButtonsPanel.add(findButton);
        mainButtonsPanel.add(Box.createVerticalStrut(5));
        mainButtonsPanel.add(writeButton);
        mainButtonsPanel.add(Box.createVerticalStrut(5));
        mainButtonsPanel.add(clearButton);
        mainButtonsPanel.add(Box.createVerticalGlue());

        JPanel bottomButtonsPanel = new JPanel();
        bottomButtonsPanel.setLayout(new BoxLayout(bottomButtonsPanel, BoxLayout.Y_AXIS));
        bottomButtonsPanel.setBackground(black);
        bottomButtonsPanel.add(logoutButton);
        bottomButtonsPanel.add(Box.createVerticalStrut(5));

        leftPanel.add(mainButtonsPanel, BorderLayout.CENTER);
        leftPanel.add(bottomButtonsPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(350, 350));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(gray);
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        scrollPane.setBackground(gray);

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setOpaque(false);
        verticalScrollBar.setBackground(new Color(0, 0, 0, 0));
        verticalScrollBar.setUI(new BasicScrollBarUI() {
            private final Color THUMB_COLOR = white;
            private final Color TRACK_COLOR = gray;

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(THUMB_COLOR);
                g2.fillRoundRect(
                        thumbBounds.x + 2,
                        thumbBounds.y + 2,
                        thumbBounds.width - 4,
                        thumbBounds.height - 4,
                        5, 5
                );
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(TRACK_COLOR);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton();
            }

            private JButton createInvisibleButton() {
                JButton button = new JButton();
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.setBorder(BorderFactory.createEmptyBorder());
                return button;
            }
        });

        rightPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        loadButton.addActionListener(e -> loadButtonPressed());
        analyzeButton.addActionListener(e -> swingUI.classify());

        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.append("Нажата кнопка: Найти\n");
            }
        });

        writeButton.addActionListener(e -> swingUI.write(""));
        clearButton.addActionListener(e -> swingUI.clear());
        logoutButton.addActionListener(e -> confirmLogout());
        add(mainPanel);
    }

    private void loadButtonPressed() {
        swingUI.createLoadingWindow();
    }

    JTextArea getTextArea() {
        return textArea;
    }

    private void confirmClosure() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Вы уверены, что хотите выйти?",
                "Выход",
                JOptionPane.YES_NO_OPTION
        );
        if (option == JOptionPane.YES_OPTION) {
            swingUI.stopParser();
            dispose();
        }
    }

    private void confirmLogout() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Вы уверены, что хотите выйти из аккаунта?",
                "Выход",
                JOptionPane.YES_NO_OPTION
        );
        if (option == JOptionPane.YES_OPTION) {
            swingUI.logout();
            dispose();
        }
    }
}