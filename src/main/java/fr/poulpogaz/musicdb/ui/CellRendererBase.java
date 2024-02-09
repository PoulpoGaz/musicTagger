package fr.poulpogaz.musicdb.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public abstract class CellRendererBase extends DefaultTableCellRenderer {

    protected static final int DEFAULT = 0;
    protected static final int CHANGED = 1;
    protected static final int NEW = 2;
    protected static final int DELETED = 3;
    protected static final int UNEDITABLE = 4;

    private static final Color COLOR_CHANGED    = new Color(44, 72, 86);
    private static final Color COLOR_NEW        = new Color(100, 113, 91);
    private static final Color COLOR_DELETED    = new Color(111, 81, 81);
    private static final Color COLOR_UNEDITABLE = new Color(60, 63, 63);

    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // before super call because of selection
        switch (getStatusOfCell(jTable, value, isSelected, hasFocus, row, column)) {
            case DEFAULT -> setBackground(null);
            case CHANGED -> setBackground(COLOR_CHANGED);
            case NEW -> setBackground(COLOR_NEW);
            case DELETED -> setBackground(COLOR_DELETED);
            case UNEDITABLE -> setBackground(COLOR_UNEDITABLE);
        }

        super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, column);

        if (value == null) {
            setText("Not set");
            setFont(getFont().deriveFont(Font.ITALIC));
        }

        return this;
    }

    protected abstract int getStatusOfCell(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column);
}
