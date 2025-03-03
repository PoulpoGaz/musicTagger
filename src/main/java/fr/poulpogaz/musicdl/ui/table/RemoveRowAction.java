package fr.poulpogaz.musicdl.ui.table;

import fr.poulpogaz.musicdl.ui.Icons;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RemoveRowAction extends AbstractMAction {

    public static RemoveRowAction create(MTable table, String rowName) {
        RemoveRowAction action = new RemoveRowAction(table);
        action.putValue(Action.SMALL_ICON, Icons.get("delete.svg"));
        action.putValue(Action.NAME, "Remove " + rowName);
        action.putValue(Action.SHORT_DESCRIPTION,
                        "Remove selected " + rowName);

        return action;
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
            int newSelection = Math.min(firstRow, table.getRowCount() - 1);
            if (newSelection >= 0) {
                table.setRowSelectionInterval(newSelection, newSelection);

                if (!table.getColumnSelectionAllowed()) {
                    table.setColumnSelectionInterval(col, col);
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return table != null &&
                table.getSelectedRow() >= 0 &&
                (table.getColumnSelectionAllowed() && table.getSelectedColumn() >= 0 || !table.getColumnSelectionAllowed());
    }
}
