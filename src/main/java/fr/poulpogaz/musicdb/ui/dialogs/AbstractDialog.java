package fr.poulpogaz.musicdb.ui.dialogs;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractDialog extends JDialog {

    public AbstractDialog(JFrame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    protected void init() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();

        setBestSize();
        setLocationRelativeTo(null);
    }

    protected void setBestSize() {
        Dimension curr = getPreferredSize();
        Rectangle max = getGraphicsConfiguration().getBounds();
        if (curr.width * 2 < max.width && curr.height * 1.5f < max.height) {
            setSize(curr.width * 2, (int) (curr.height * 1.5f));
        } else {
            setSize(curr);
        }
    }

    protected abstract void initComponents();
}
