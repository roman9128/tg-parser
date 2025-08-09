package rt.view.gui;

import org.jdesktop.swingx.JXDatePicker;
import rt.infrastructure.notifier.Notifier;
import rt.model.preset.PresetDTO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MainWindow extends JFrame {

    private JPanel listPanel;
    private JTextArea textArea;
    private List<PresetDTO> presets;
    private final SwingUI swingUI;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    MainWindow(SwingUI swingUI) {
        this.swingUI = swingUI;

        SwingUtilities.invokeLater(() -> {
            UIManager.put("OptionPane.yesButtonText", "Да");
            UIManager.put("OptionPane.noButtonText", "Нет");
            UIManager.put("OptionPane.sound", null);
            UIManager.put("Button.background", Color.WHITE);
            UIManager.put("Button.foreground", Color.BLACK);
            UIManager.put("Button.focus", new Color(0, 0, 0, 0));

            setVisible(true);
            setTitle("Tg-Parser 2.0");
            setSize(1100, 600);
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

    public void setPresets(List<PresetDTO> presets) {
        this.presets = presets;
        updatePresetsList(presets);
    }

    public void updatePresetsList(List<PresetDTO> updatedPresets) {
        SwingUtilities.invokeLater(() -> {
            this.presets = updatedPresets;
            listPanel.removeAll();

            for (PresetDTO preset : updatedPresets) {
                listPanel.add(createPresetListItem(preset));
                listPanel.add(Box.createVerticalStrut(5));
            }

            listPanel.revalidate();
            listPanel.repaint();
        });
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        columnsPanel.setBackground(Color.WHITE);

        JPanel leftColumn = ElementsBuilder.createColumn("проекты");

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        if (presets != null) {
            for (PresetDTO preset : presets) {
                listPanel.add(createPresetListItem(preset));
                listPanel.add(Box.createVerticalStrut(10));
            }
        }

        JScrollPane scrollPaneList = new JScrollPane(listPanel);
        scrollPaneList.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(),
                BorderFactory.createMatteBorder(0, 0, 0, 1, Colors.DARK_GRAY)
        ));
        scrollPaneList.getVerticalScrollBar().setUnitIncrement(16);
        ElementsBuilder.customizeScrollBar(scrollPaneList);

        leftColumn.add(scrollPaneList, BorderLayout.CENTER);

        JPanel rightColumn = ElementsBuilder.createColumn("новый поиск");

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JXDatePicker startDatePicker = ElementsBuilder.createDatePicker();
        JXDatePicker endDatePicker = ElementsBuilder.createDatePicker();
        datePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        datePanel.setBackground(Color.WHITE);
        datePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        datePanel.add(ElementsBuilder.createLabel("дата: с "));
        datePanel.add(startDatePicker);
        datePanel.add(Box.createHorizontalStrut(15));
        datePanel.add(ElementsBuilder.createLabel("по "));
        datePanel.add(endDatePicker);

        rightColumn.add(datePanel);

        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        listsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        listsPanel.setBackground(Color.WHITE);

        Map<String, String> foldersMap = swingUI.getFoldersIDsAndNames().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getValue(),
                        e -> String.valueOf(e.getKey())
                ));

        Map<String, String> channelsMap = swingUI.getChannelsIDsAndNames().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getValue().replaceAll("[^\\p{L}\\p{N} ]", ""),
                        e -> String.valueOf(e.getKey())
                ));

        DefaultListModel<ItemWrapper> foldersModel = new DefaultListModel<>();
        foldersMap.keySet().forEach(name -> foldersModel.addElement(new ItemWrapper(name, false)));

        JList<ItemWrapper> foldersList = new JList<>(foldersModel);
        foldersList.setCellRenderer(new CheckboxListCellRenderer());
        foldersList.setSelectionModel(new NoSelectionModel());
        foldersList.setBackground(Color.WHITE);
        foldersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = foldersList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    ItemWrapper item = foldersModel.getElementAt(index);
                    item.setSelected(!item.isSelected());
                    foldersList.repaint();
                }
            }
        });

        JScrollPane foldersScroll = new JScrollPane(foldersList);
        foldersScroll.setBackground(Color.WHITE);
        ElementsBuilder.customizeScrollBar(foldersScroll);
        foldersScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        "папки",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        Fonts.F12B,
                        Color.BLACK
                ),
                BorderFactory.createMatteBorder(0, 0, 0, 1, Colors.DARK_GRAY)
        ));

        DefaultListModel<ItemWrapper> channelsModel = new DefaultListModel<>();
        channelsMap.keySet().forEach(name -> channelsModel.addElement(new ItemWrapper(name, false)));

        JList<ItemWrapper> channelsList = new JList<>(channelsModel);
        channelsList.setCellRenderer(new CheckboxListCellRenderer());
        channelsList.setSelectionModel(new NoSelectionModel());
        channelsList.setBackground(Color.WHITE);
        channelsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = channelsList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    ItemWrapper item = channelsModel.getElementAt(index);
                    item.setSelected(!item.isSelected());
                    channelsList.repaint();
                }
            }
        });

        JScrollPane channelsScroll = new JScrollPane(channelsList);
        channelsScroll.setBackground(Color.WHITE);
        ElementsBuilder.customizeScrollBar(channelsScroll);
        channelsScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "каналы",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        Fonts.F12B,
                        Color.BLACK
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        listsPanel.add(foldersScroll);
        listsPanel.add(channelsScroll);
        rightColumn.add(listsPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setBackground(Color.WHITE);

        JButton loadButton = ElementsBuilder.createRegularButton("Загрузить");
        JButton findButton = ElementsBuilder.createRegularButton("Найти");
        JButton writeButton = ElementsBuilder.createRegularButton("Записать");
        JButton writeXButton = ElementsBuilder.createRegularButton("Записать избранное");
        JButton clearButton = ElementsBuilder.createRegularButton("Очистить");

        loadButton.addActionListener(e -> {
            List<String> selectedFolders = new ArrayList<>();
            for (int i = 0; i < foldersModel.getSize(); i++) {
                ItemWrapper item = foldersModel.getElementAt(i);
                if (item.isSelected()) {
                    String id = foldersMap.get(item.getName());
                    if (id != null) selectedFolders.add(id);
                }
            }

            List<String> selectedChannels = new ArrayList<>();
            for (int i = 0; i < channelsModel.getSize(); i++) {
                ItemWrapper item = channelsModel.getElementAt(i);
                if (item.isSelected()) {
                    String id = channelsMap.get(item.getName());
                    if (id != null) selectedChannels.add(id);
                }
            }

            String sources;
            if (selectedFolders.isEmpty() && selectedChannels.isEmpty()) {
                sources = "all";
            } else {
                sources = Stream.concat(selectedFolders.stream(), selectedChannels.stream())
                        .collect(Collectors.joining(","));
            }

            String dateFromString = "";
            String dateToString = "";
            if (startDatePicker.getDate() != null) {
                dateFromString = sdf.format(startDatePicker.getDate());
                if (endDatePicker.getDate() != null) {
                    dateToString = sdf.format(endDatePicker.getDate());
                }
            }
            swingUI.loadAnalyze(sources, dateFromString, dateToString);
            updatePresetsList(swingUI.getUpdatedPresets());
        });
        findButton.addActionListener(e -> findButtonPressed());
        writeButton.addActionListener(e -> writeButtonPressed());
        writeXButton.addActionListener(e -> writeXButtonPressed());
        clearButton.addActionListener(e -> clearButtonPressed());

        buttonPanel.add(loadButton);
        buttonPanel.add(findButton);
        buttonPanel.add(writeButton);
        buttonPanel.add(writeXButton);
        buttonPanel.add(clearButton);

        rightColumn.add(Box.createVerticalStrut(10));
        rightColumn.add(buttonPanel);
        rightColumn.add(Box.createVerticalGlue());

        columnsPanel.add(leftColumn);
        columnsPanel.add(rightColumn);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(Color.BLACK);
        textArea.setCaretColor(Color.BLACK);
        textArea.setFont(Fonts.F10);

        JScrollPane scrollPaneText = new JScrollPane(textArea);
        scrollPaneText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                BorderFactory.createMatteBorder(1, 0, 0, 0, Colors.DARK_GRAY)
        ));
        scrollPaneText.setBackground(Color.WHITE);
        scrollPaneText.setPreferredSize(new Dimension(0, 100));
        ElementsBuilder.customizeScrollBar(scrollPaneText);

        mainPanel.add(columnsPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPaneText, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void findButtonPressed() {
        swingUI.createSearchWindow();
    }

    private void writeButtonPressed() {
        swingUI.write("");
    }

    private void writeXButtonPressed() {
        swingUI.write("x");
    }

    private void clearButtonPressed() {
        swingUI.clear();
    }

    public void print(String text) {
        if (textArea != null) {
            textArea.append(text);
        } else {
            try {
                Thread.sleep(300);
                print(text);
            } catch (InterruptedException e) {
                Notifier.getInstance().addNotification(e.getMessage());
            }
        }
    }

    private void confirmClosure() {
        Object[] options = {"Да, выйти", "Нет, остаться", "Разлогиниться"};
        int option = JOptionPane.showOptionDialog(
                this,
                "Вы уверены, что хотите выйти?",
                "Выход",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        if (option == JOptionPane.YES_OPTION) {
            swingUI.stopParser();
            dispose();
        } else if (option == JOptionPane.NO_OPTION) {
        } else if (option == 2) {
            confirmLogout();
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

    public void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Уведомление",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private JPanel createPresetListItem(PresetDTO preset) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.X_AXIS));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel name = new JLabel(truncateText(preset.name(), 20));
        name.setFont(Fonts.F12B);
        name.setPreferredSize(new Dimension(120, 20));
        item.add(name);

        item.add(Box.createHorizontalStrut(5));

        JPanel data = new JPanel();
        data.setLayout(new BoxLayout(data, BoxLayout.Y_AXIS));
        data.setBackground(Color.WHITE);
        String date = preset.end().isBlank()
                ? preset.start()
                : preset.start() + " - " + preset.end();

        data.add(ElementsBuilder.createSmallLabel(date));
        data.add(ElementsBuilder.createSmallLabel(truncateText("Папки: " + String.join(", ", preset.folders()), 50)));
        data.add(ElementsBuilder.createSmallLabel(truncateText("Каналы: " + String.join(", ", preset.channels()), 50)));
        item.add(data);

        item.add(Box.createHorizontalGlue());

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(Color.WHITE);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 5, 0));
        buttons.setBackground(Color.WHITE);

        JButton selectBtn = ElementsBuilder.createItemListButton("✓");
        JButton renameBtn = ElementsBuilder.createItemListButton("↻");
        JButton deleteBtn = ElementsBuilder.createItemListButton("✕");

//        selectBtn.addActionListener(e -> swingUI.loadPreset(preset.getId()));
//        renameBtn.addActionListener(e -> renamePreset(preset));
//        deleteBtn.addActionListener(e -> deletePreset(preset));

        buttons.add(selectBtn);
        buttons.add(renameBtn);
        buttons.add(deleteBtn);

        buttonPanel.add(buttons, BorderLayout.EAST);

        item.add(buttonPanel, BorderLayout.EAST);

        return item;
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    class ItemWrapper {
        private final String name;
        private boolean selected;

        public ItemWrapper(String name, boolean selected) {
            this.name = name;
            this.selected = selected;
        }

        public String getName() {
            return name;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    class CheckboxListCellRenderer implements ListCellRenderer<ItemWrapper> {
        private final JCheckBox checkBox = new JCheckBox();

        {
            checkBox.setIcon(new CircleIcon(false));
            checkBox.setSelectedIcon(new CircleIcon(true));
            checkBox.setBackground(Color.WHITE);
            checkBox.setFont(Fonts.F12);
            checkBox.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2)); // Отступы
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ItemWrapper> list, ItemWrapper value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            checkBox.setText(value.getName());
            checkBox.setSelected(value.isSelected());
            return checkBox;
        }
    }

    class CircleIcon implements Icon {
        private final boolean selected;
        private final int size;
        private final Color circleColor;
        private final Color checkColor;
        private final float strokeWidth;
        private final float checkPadding;
        private final float checkMiddleX;

        public CircleIcon(boolean selected) {
            this.selected = selected;
            this.size = 16;
            this.circleColor = Color.BLACK;
            this.checkColor = Color.WHITE;
            this.strokeWidth = 2.0f;
            this.checkPadding = 0.25f;
            this.checkMiddleX = 0.45f;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(c.getBackground());
            g2d.fillOval(x, y, size, size);

            g2d.setColor(circleColor);
            g2d.drawOval(x, y, size, size);

            if (selected) {
                g2d.setColor(circleColor);
                g2d.fillOval(x, y, size, size);

                g2d.setColor(checkColor);
                g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                float startX = x + size * checkPadding;
                float startY = y + size * 0.5f;
                float middleX = x + size * checkMiddleX;
                float middleY = y + size * (1 - checkPadding);
                float endX = x + size * (1 - checkPadding);
                float endY = y + size * checkPadding;

                Path2D check = new Path2D.Float();
                check.moveTo(startX, startY);
                check.lineTo(middleX, middleY);
                check.lineTo(endX, endY);

                g2d.draw(check);
            }
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    class NoSelectionModel extends DefaultListSelectionModel {
        @Override
        public void setSelectionInterval(int index0, int index1) {
            super.setSelectionInterval(-1, -1);
        }
    }
}