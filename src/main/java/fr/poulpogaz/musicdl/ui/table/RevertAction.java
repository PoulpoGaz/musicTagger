package fr.poulpogaz.musicdl.ui.table;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RevertAction extends AbstractAction {

    public static RevertAction create(MTable table) {
        RevertAction action = new RevertAction(table);
        action.putValue(Action.NAME, "Revert");
        action.putValue(Action.SHORT_DESCRIPTION,
                        "Revert selected cells to their previous values");

        return action;
    }


    private final MTable table;

    public RevertAction(MTable table) {
        this.table = table;
        table.addAction(this);
        setEnabled(isEnabled());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ListSelectionModel selectedRows = table.getSelectionModel();
        ListSelectionModel selectedColumns = table.getColumnModel().getSelectionModel();

        if (selectedRows.isSelectionEmpty() || selectedColumns.isSelectionEmpty()) {
            return;
        }

        MTableModel model = table.getModel();
        if (isSingleItemSelected(selectedRows) && isSingleItemSelected(selectedColumns)) {
            model.revert(selectedRows.getMinSelectionIndex(), selectedColumns.getMinSelectionIndex());
        } else {
            model.revert(selectedRows, selectedColumns);
        }
    }


    protected boolean isSingleItemSelected(ListSelectionModel model) {
        return model.getMaxSelectionIndex() == model.getMinSelectionIndex();
    }

    @Override
    public boolean isEnabled() {
        return !table.getSelectionModel().isSelectionEmpty()
                && !table.getColumnModel().getSelectionModel().isSelectionEmpty();
    }
}
