package fr.poulpogaz.musictagger.ui.table;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public abstract class AbstractRevertTableModel extends AbstractTableModel implements RevertTableModel {

    @Override
    public boolean revert(ListSelectionModel selectedRows, ListSelectionModel selectedColumns) {
        int minR = selectedRows.getMinSelectionIndex();
        int maxR = selectedRows.getMaxSelectionIndex();
        int minC = selectedColumns.getMinSelectionIndex();
        int maxC = selectedColumns.getMaxSelectionIndex();

        int updateMinR = Integer.MAX_VALUE;
        int updateMaxR = -1;

        for (int row = minR; row <= maxR; row++) {
            if (selectedRows.isSelectedIndex(row)) {
                boolean update = false;
                for (int col = minC; col <= maxC; col++) {
                    if (selectedColumns.isSelectedIndex(col)) {
                        update |= doRevert(row, col);
                    }
                }

                if (update) {
                    updateMaxR = Math.max(row, updateMaxR);
                    updateMinR = Math.min(row, updateMinR);
                }
            }
        }

        if (updateMaxR != -1) {
            fireTableRowsUpdated(updateMinR, updateMaxR);
            return true;
        }
        return false;
    }

    @Override
    public boolean revert(int row, int column) {
        if (doRevert(row, column)) {
            fireTableCellUpdated(row, column);
            return true;
        }
        return false;
    }

    protected abstract boolean doRevert(int row, int column);
}
