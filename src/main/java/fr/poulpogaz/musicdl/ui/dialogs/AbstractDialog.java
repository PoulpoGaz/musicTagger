package fr.poulpogaz.musicdl.ui.dialogs;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractDialog extends JDialog {

    public AbstractDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public AbstractDialog(Dialog owner, String title, boolean modal) {
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
        float sw = widthScaleFactor();
        float sh = heightScaleFactor();

        if (curr.width * sw < max.width && curr.height * sh < max.height) {
            setSize((int) (curr.width * sw), (int) (curr.height * sh));
        } else {
            setSize(curr);
        }
    }

    protected float widthScaleFactor() {
        return 2f;
    }

    protected float heightScaleFactor() {
        return 1.5f;
    }

    protected abstract void initComponents();
}
