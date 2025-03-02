package fr.poulpogaz.musicdl.ui.text;

import javax.swing.*;
import java.awt.*;

public class TextUtils {

    public static JComponent titledSeparator(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.weighty = 1;

        panel.add(new JLabel(title), c);
        c.gridx = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(2, 6, 0, 0);
        panel.add(new JSeparator(), c);

        return panel;
    }
}
