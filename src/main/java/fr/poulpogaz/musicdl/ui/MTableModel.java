package fr.poulpogaz.musicdl.ui;

import javax.swing.table.TableModel;

public interface MTableModel extends TableModel {

    default boolean newRow() {
        return newRow(getRowCount());
    }

    boolean newRow(int index);

    boolean removeRow(int index);

    boolean swapRows(int rowI, int rowJ);

    default boolean moveUp(int row) {
        return swapRows(row - 1, row);
    }

    default boolean moveDown(int row) {
        return swapRows(row, row + 1);
    }
}
