package fr.poulpogaz.musicdl.ui.table;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static fr.poulpogaz.musicdl.ui.table.ActionUtils.isSingleItemSelected;

public class RevertAction extends AbstractMAction {

    public static RevertAction create(MTable table) {
        RevertAction action = new RevertAction(table);
        action.putValue(Action.NAME, "Revert");
        action.putValue(Action.SHORT_DESCRIPTION,
                        "Revert selected cells to their previous values");

        return action;
    }

    public RevertAction(MTable table) {
        super(table);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (table == null) {
            return;
        }

        ListSelectionModel selectedRows = table.getSelectionModel();
        ListSelectionModel selectedColumns = table.getColumnModel().getSelectionModel();

        if (selectedRows.isSelectionEmpty() || selectedColumns.isSelectionEmpty()) {
            return;
        }

        MTableModel model = table.getModel();
        if (model instanceof RevertTableModel revertModel) {
            if (isSingleItemSelected(selectedRows) && isSingleItemSelected(selectedColumns)) {
                revertModel.revert(selectedRows.getMinSelectionIndex(), selectedColumns.getMinSelectionIndex());
            } else {
                revertModel.revert(selectedRows, selectedColumns);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return table != null
                && !table.getSelectionModel().isSelectionEmpty()
                && !table.getColumnModel().getSelectionModel().isSelectionEmpty();
    }
}
