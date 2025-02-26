package fr.poulpogaz.musicdl.ui.table;

import fr.poulpogaz.musicdl.ui.Icons;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class NewRowAction extends AbstractAction {

    public static NewRowAction create(MTable table, String rowName) {
        NewRowAction action = new NewRowAction(table);
        action.putValue(Action.SMALL_ICON, Icons.get("add.svg"));
        action.putValue(Action.NAME, "New " + rowName);
        action.putValue(Action.SHORT_DESCRIPTION,
                        "Create a new " + rowName + " below selection");

        return action;
    }


    private final MTable table;

    public NewRowAction(MTable table) {
        this.table = table;
        table.addAction(this);
        setEnabled(isEnabled());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int row = table.getSelectionModel().getMaxSelectionIndex();
        if (row == -1) {
            row = table.getRowCount();
        } else {
            row = Math.min(row + 1, table.getRowCount());
        }

        if (table.getModel().newRow(row)) {
            table.getSelectionModel().setSelectionInterval(row, row);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
