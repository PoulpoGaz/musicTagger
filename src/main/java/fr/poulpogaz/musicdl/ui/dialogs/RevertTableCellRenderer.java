package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.ui.CellRendererBase;

import javax.swing.*;

public class RevertTableCellRenderer extends CellRendererBase {

    @Override
    protected int getStatusOfCell(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        RestoreTableModel<?> model = (RestoreTableModel<?>) jTable.getModel();
        RestoreTableModel.Row rowO = model.getRow(row);

        if (rowO.isNew()) {
            return NEW;
        } else if (rowO.isRemoved()) {
            return DELETED;
        } else if (model.isCellEditable(row, column)
                && rowO.hasChanged(column)) {
            return CHANGED;
        } else {
            return DEFAULT;
        }
    }
}