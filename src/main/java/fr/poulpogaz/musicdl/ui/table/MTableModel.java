package fr.poulpogaz.musicdl.ui.table;

import javax.swing.*;
import javax.swing.table.TableModel;

public interface MTableModel extends TableModel {

    // RETURNS VALUES ARE NOT USED FOR FIRING AN EVENT
    // THEY ARE USED FOR ANYTHING (MOVING SELECTION, ...)
    // EXCEPT MODIFYING MODEL

    default boolean newRow() {
        return newRow(getRowCount());
    }

    boolean newRow(int index);

    boolean removeRow(int index);

    default boolean removeRows(ListSelectionModel selectedRows) {
        return false;
    }

    boolean swapRows(int rowI, int rowJ);

    default boolean moveUp(int row) {
        return swapRows(row - 1, row);
    }

    default boolean moveDown(int row) {
        return swapRows(row, row + 1);
    }
}
