package rt.view.gui;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

public class ElementsBuilder {

    static JButton createMainMenuButton(String text, String tooltip, Color back, Color fore, Color point) {
        Dimension buttonSize = new Dimension(140, 30);
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setPreferredSize(buttonSize);
        button.setMaximumSize(buttonSize);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(back);
        button.setForeground(fore);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(point);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(back);
            }
        });
        return button;
    }

    static JButton createRegularButton(String text, Color back, Color fore) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(back);
        button.setForeground(fore);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(80, 25));
        return button;
    }

    static JXDatePicker createDatePicker() {
        JXDatePicker datePicker = new JXDatePicker();
        datePicker.setFormats("dd.MM.yyyy");
        datePicker.getMonthView().setUpperBound(new Date());
        return datePicker;
    }
}
