package fr.poulpogaz.musicdl.ui.table;

import fr.poulpogaz.musicdl.ui.Icons;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RemoveRowAction extends AbstractAction {

    public static RemoveRowAction create(MTable table, String rowName) {
        RemoveRowAction action = new RemoveRowAction(table);
        action.putValue(Action.SMALL_ICON, Icons.get("delete.svg"));
        action.putValue(Action.NAME, "Remove " + rowName);
        action.putValue(Action.SHORT_DESCRIPTION,
                        "Remove selected " + rowName);

        return action;
    }


    private final MTable table;

    public RemoveRowAction(MTable table) {
        this.table = table;
        table.addAction(this);
        setEnabled(isEnabled());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();

        if (row != -1 && table.getModel().removeRow(row)) {
            int newSelection = Math.min(row, table.getRowCount() - 1);
            if (newSelection >= 0) {
                table.setRowSelectionInterval(newSelection, newSelection);

                if (col >= 0 && col < table.getColumnCount()) {
                    table.setColumnSelectionInterval(col, col);
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return table.getSelectedRow() >= 0;
    }
}
