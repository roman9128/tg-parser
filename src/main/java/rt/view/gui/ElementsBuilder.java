package rt.view.gui;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

class ElementsBuilder {

    static JButton createRegularButton(String text) {
        Dimension buttonSize = new Dimension(100, 25);

        JButton button = new JButton(text);
        button.setFont(Fonts.F12);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setPreferredSize(buttonSize);
        button.setMaximumSize(buttonSize);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(Colors.LIGHT_GRAY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    static JButton createItemListButton(String title) {
        JButton button = new JButton(title);
        button.setFont(Fonts.S);
        button.setPreferredSize(new Dimension(30, 30));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(Colors.LIGHT_GRAY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    static JButton createInvisibleButton() {
        JButton button = new JButton();
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        return button;
    }

    static JXDatePicker createDatePicker() {
        JXDatePicker datePicker = new JXDatePicker();
        datePicker.setFormats("dd.MM.yyyy");
        datePicker.getMonthView().setUpperBound(new Date());
        return datePicker;
    }

    static JPanel createColumn(String title) {
        JPanel column = new JPanel(new BorderLayout());
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBackground(Color.WHITE);
        column.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel leftTitle = new JLabel(title);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftTitle.setFont(Fonts.F16BI);
        leftTitle.setForeground(Color.BLACK);
        leftTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        column.add(leftTitle, BorderLayout.NORTH);

        return column;
    }

    static JLabel createLabel(String title) {
        JLabel label = new JLabel(title);
        label.setFont(Fonts.F12B);
        return label;
    }

    static JLabel createSmallLabel(String title){
        JLabel label = new JLabel(title);
        label.setFont(Fonts.F12);
        return label;
    }

    static void customizeScrollBar(JScrollPane scrollPane) {
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setOpaque(false);
        verticalScrollBar.setBackground(new Color(0, 0, 0, 0));
        verticalScrollBar.setUI(new BasicScrollBarUI() {
            private final Color THUMB_COLOR = Colors.LIGHT_GRAY;
            private final Color TRACK_COLOR = Color.WHITE;

            @Override
            protected Dimension getMinimumThumbSize() {
                return new Dimension(super.getMinimumThumbSize().width / 2, super.getMinimumThumbSize().height);
            }

            @Override
            protected Dimension getMaximumThumbSize() {
                return new Dimension(super.getMaximumThumbSize().width / 2, super.getMaximumThumbSize().height);
            }

            @Override
            public Dimension getPreferredSize(JComponent c) {
                return new Dimension(super.getPreferredSize(c).width / 2, super.getPreferredSize(c).height);
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(THUMB_COLOR);
                g2.fillRoundRect(
                        thumbBounds.x + 1,
                        thumbBounds.y + 2,
                        thumbBounds.width - 2,
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
                return ElementsBuilder.createInvisibleButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return ElementsBuilder.createInvisibleButton();
            }
        });
    }
}
