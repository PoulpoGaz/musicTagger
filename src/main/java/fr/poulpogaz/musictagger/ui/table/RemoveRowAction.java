package fr.poulpogaz.musictagger.ui.table;

import fr.poulpogaz.musictagger.ui.Icons;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RemoveRowAction extends AbstractMAction {

    public static RemoveRowAction create(MTable table, String rowName) {
        RemoveRowAction action = new RemoveRowAction(table);
        putValues(action, rowName);
        return action;
    }

    public static void putValues(Action action, String rowName) {
        action.putValue(Action.SMALL_ICON, Icons.get("delete.svg"));
        action.putValue(Action.NAME, "Remove " + rowName);
        action.putValue(Action.SHORT_DESCRIPTION,
                        "Remove selected " + rowName);
    }

    public RemoveRowAction(MTable table) {
        super(table);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (table == null) {
            return;
        }

        ListSelectionModel rows = table.getSelectionModel();
        int firstRow = table.getSelectedRow();
        int col = Math.max(table.getSelectedColumn(), 0);

        boolean moveSelection = false;
        if (ActionUtils.isSingleItemSelected(rows)) {
            moveSelection = table.getModel().removeRow(firstRow);
        } else if (firstRow >= 0) {
            moveSelection = table.getModel().removeRows(rows);
        }


        if (moveSelection) {
            int newSelection = newSelectionIndex(table, firstRow);
            if (newSelection >= 0) {
                table.setRowSelectionInterval(newSelection, newSelection);

                if (!table.getColumnSelectionAllowed()) {
                    table.setColumnSelectionInterval(col, col);
                }
            }
        }
    }

    protected int newSelectionIndex(MTable table, int firstRemovedRow) {
        return Math.min(firstRemovedRow, table.getRowCount() - 1);
    }

    @Override
    public boolean isEnabled() {
        return table != null &&
                table.getSelectedRow() >= 0 &&
                (table.getColumnSelectionAllowed() && table.getSelectedColumn() >= 0 || !table.getColumnSelectionAllowed());
    }
}
