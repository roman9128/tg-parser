package rt.view.gui;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class LoadingWindow extends JFrame {
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    LoadingWindow(SwingUI swingUI) {
        SwingUtilities.invokeLater(() -> {
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            initComponents(swingUI);
        });
    }

    private void initComponents(SwingUI swingUI) {
        JDialog loadingParamsWindow = new JDialog(this, "Параметры загрузки", true);
        loadingParamsWindow.setSize(350, 350);
        loadingParamsWindow.setResizable(false);
        loadingParamsWindow.setLayout(new BorderLayout(5, 5));
        loadingParamsWindow.getRootPane().setBorder(new EmptyBorder(5, 5, 5, 5));
        loadingParamsWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        loadingParamsWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                LoadingWindow.this.dispose();
            }
        });

        JXDatePicker startDatePicker = ElementsBuilder.createDatePicker();
        JXDatePicker endDatePicker = ElementsBuilder.createDatePicker();

        Map<String, String> commonMap = Stream.concat(
                        swingUI.getFoldersIDsAndNames().entrySet().stream(),
                        swingUI.getChannelsIDsAndNames().entrySet().stream()
                )
                .collect(Collectors.toMap(
                        e -> e.getValue().replaceAll("[^\\p{L}\\p{N} ]", ""),
                        e -> String.valueOf(e.getKey()),
                        (oldID, newID) -> newID
                ));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("Все");
        for (String key : commonMap.keySet()) {
            listModel.addElement(key);
        }

        JList<String> itemList = new JList<>(listModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.setFont(new Font("Arial", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(itemList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Папки"));
        scrollPane.setPreferredSize(new Dimension(0, 190));

        JPanel datePanel = new JPanel(new FlowLayout());
        datePanel.setBorder(BorderFactory.createTitledBorder("Период"));

        datePanel.add(new JLabel("с "));
        datePanel.add(startDatePicker);
        datePanel.add(new JLabel("по "));
        datePanel.add(endDatePicker);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton cancelButton = ElementsBuilder.createRegularButton("Отмена", Color.WHITE, Color.BLACK);
        JButton okButton = ElementsBuilder.createRegularButton("OK", Color.WHITE, Color.BLACK);

        cancelButton.addActionListener(e -> dispose());
        okButton.addActionListener(e -> {
            String selectedValue = itemList.getSelectedValue();
            String source = "all";
            String dateFromString = "";
            String dateToString = "";
            if (selectedValue != null) {
                if (commonMap.containsKey(selectedValue)) {
                    source = commonMap.get(selectedValue);
                }
            }
            if (startDatePicker.getDate() != null) {
                dateFromString = sdf.format(startDatePicker.getDate());
                if (endDatePicker.getDate() != null) {
                    dateToString = sdf.format(endDatePicker.getDate());
                }
            }
            swingUI.load(source, dateFromString, dateToString);
            dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        loadingParamsWindow.add(scrollPane, BorderLayout.NORTH);
        loadingParamsWindow.add(datePanel, BorderLayout.CENTER);
        loadingParamsWindow.add(buttonPanel, BorderLayout.SOUTH);
        loadingParamsWindow.setLocationRelativeTo(this);
        loadingParamsWindow.setVisible(true);
    }
}