package fr.poulpogaz.musicdl.ui.table;

import fr.poulpogaz.musicdl.ui.Icons;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MoveAction extends AbstractAction {

    public static MoveAction moveUp(MTable table, String rowName) {
        MoveAction action = new MoveAction(table, -1);
        action.putValue(Action.SMALL_ICON, Icons.get("move_up.svg"));
        action.putValue(Action.NAME, "Move up");
        action.putValue(Action.SHORT_DESCRIPTION,
                        "Move up selected " + rowName);

        return action;
    }

    public static MoveAction moveDown(MTable table, String rowName) {
        MoveAction action = new MoveAction(table, 1);
        action.putValue(Action.SMALL_ICON, Icons.get("move_down.svg"));
        action.putValue(Action.NAME, "Move down");
        action.putValue(Action.SHORT_DESCRIPTION,
                        "Move down selected " + rowName);

        return action;
    }


    private final MTable table;
    private final int offset;

    public MoveAction(MTable table, int offset) {
        if (offset == 0) {
            throw new IllegalArgumentException("zero offset");
        }

        this.table = table;
        this.offset = offset;
        table.addAction(this);
        setEnabled(isEnabled());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int row = table.getSelectedRow();
        int dest = row + offset;
        if (table.getModel().swapRows(row, dest)) {
            table.setRowSelectionInterval(dest, dest);
        }
    }

    @Override
    public boolean isEnabled() {
        if (table.getSelectedColumn() != -1) {
            int dest = table.getSelectedRow() + offset;
            return dest >= 0 && dest < table.getRowCount();
        }
        return false;
    }
}
