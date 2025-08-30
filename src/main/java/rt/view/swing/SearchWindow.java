package rt.view.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class SearchWindow extends JFrame {
    SearchWindow(SwingUI swingUI) {
        SwingUtilities.invokeLater(() -> {
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            initComponents(swingUI);
        });
    }

    private void initComponents(SwingUI swingUI) {
        JDialog paramsWindow = new JDialog(this, "Поиск", true);
        paramsWindow.setSize(350, 350);
        paramsWindow.setResizable(false);
        paramsWindow.setLayout(new BorderLayout(5, 5));
        paramsWindow.getRootPane().setBorder(new EmptyBorder(5, 5, 5, 5));
        paramsWindow.setLocationRelativeTo(this);
        paramsWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        paramsWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                SearchWindow.this.dispose();
            }
        });

        JPanel searchParamsPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        JPanel searchTypePanel = new JPanel(new GridLayout(3, 1, 0, 5));
        searchTypePanel.setBorder(BorderFactory.createTitledBorder("Тип поиска"));

        ButtonGroup searchTypeGroup = new ButtonGroup();
        JRadioButton andRadio = new JRadioButton("и");
        JRadioButton orRadio = new JRadioButton("или");
        JRadioButton notRadio = new JRadioButton("нет");

        searchTypeGroup.add(andRadio);
        searchTypeGroup.add(orRadio);
        searchTypeGroup.add(notRadio);
        orRadio.setSelected(true);

        searchTypePanel.add(andRadio);
        searchTypePanel.add(orRadio);
        searchTypePanel.add(notRadio);

        JPanel searchScopePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        searchScopePanel.setBorder(BorderFactory.createTitledBorder("Область поиска"));

        ButtonGroup searchScopeGroup = new ButtonGroup();
        JRadioButton textRadio = new JRadioButton("В тексте");
        JRadioButton topicRadio = new JRadioButton("По темам");

        searchScopeGroup.add(textRadio);
        searchScopeGroup.add(topicRadio);
        textRadio.setSelected(true);

        searchScopePanel.add(textRadio);
        searchScopePanel.add(topicRadio);

        searchParamsPanel.add(searchTypePanel);
        searchParamsPanel.add(searchScopePanel);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createTitledBorder("Текст для поиска"));

        JTextArea textArea = new JTextArea();
        textArea.setEditable(true);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        textPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> paramsWindow.dispose());

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            String how = andRadio.isSelected()
                    ? "and" : orRadio.isSelected()
                    ? "or" : "not";
            String where = textRadio.isSelected()
                    ? "text" : "topic";
            String what = textArea.getText();
            swingUI.find(new String[]{how, where, what});
            paramsWindow.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        paramsWindow.add(searchParamsPanel, BorderLayout.NORTH);
        paramsWindow.add(textPanel, BorderLayout.CENTER);
        paramsWindow.add(buttonPanel, BorderLayout.SOUTH);
        paramsWindow.setVisible(true);
    }
}