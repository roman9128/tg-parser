package rt.view.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

class AuthWindow extends JFrame {

    String askParameter(String who, String question){
        String[] lines = question.split(System.lineSeparator());
        JPanel authPanel = new JPanel(new BorderLayout(10, 10));
        authPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
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
        authPanel.add(textPanel, BorderLayout.CENTER);
        authPanel.add(inputPanel, BorderLayout.EAST);
        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    null,
                    authPanel,
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
                File folder = new File("./session");
                deleteSession(folder);
                System.exit(0);
            }
        }
    }

    private void deleteSession(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteSession(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
