package fr.poulpogaz.musicdb.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class TablePopupMenuSupport extends MouseAdapter {

    protected final JTable table;
    protected final JPopupMenu popupMenu;

    public TablePopupMenuSupport(JTable table, JPopupMenu popupMenu) {
        this.table = Objects.requireNonNull(table);
        this.popupMenu = Objects.requireNonNull(popupMenu);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), table);

            int row = table.rowAtPoint(p);
            int col = table.columnAtPoint(p);

            if (isVisible(e, row, col)) {
                popupMenu.show(table, e.getX(), e.getY());
            }
        }
    }

    protected boolean isVisible(MouseEvent event, int row, int column) {
        if (row >= 0 && column >= 0) {
            table.setRowSelectionInterval(row, row);
            table.setColumnSelectionInterval(column, column);
        } else {
            table.clearSelection();
        }

        return true;
    }
}
