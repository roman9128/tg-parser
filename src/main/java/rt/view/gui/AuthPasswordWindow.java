package rt.view.gui;

import rt.infrastructure.parser.SessionEraser;

import javax.swing.*;
import java.awt.*;

class AuthPasswordWindow extends JFrame {

    String ask2FAPassword() {
        JPanel authPanel = new JPanel(new BorderLayout(10, 10));
        authPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JPanel textPanel = new JPanel(new GridLayout(1, 1, 0, 5));
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        textPanel.add(new JLabel("Введите облачный пароль"));
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
                    "Авторизация",
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
                SessionEraser.deleteSession();
                System.exit(0);
            }
        }
    }
}
