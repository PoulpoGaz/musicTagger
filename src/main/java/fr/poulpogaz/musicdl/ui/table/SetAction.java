package fr.poulpogaz.musicdl.ui.table;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SetAction extends AbstractMAction {

    public static SetAction setNull(MTable table) {
        SetAction action = new SetAction(table, null);
        action.putValue(Action.NAME, "Set null");
        action.putValue(Action.SHORT_DESCRIPTION,
                        "Replace selected cell values with null values");

        return action;
    }

    private final Object value;

    public SetAction(MTable table, Object value) {
        super(table);
        this.value = value;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (table == null) {
            return;
        }

        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();

        if (row != -1 && col != -1) {
            table.getModel().setValueAt(value, row, col);
        }
    }

    @Override
    public boolean isEnabled() {
        return table != null &&
                table.getSelectedRow() != -1 && table.getSelectedColumn() != -1;
    }
}
