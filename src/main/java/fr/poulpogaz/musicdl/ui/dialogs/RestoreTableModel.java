package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.ui.MTableModel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class RestoreTableModel<R extends RestoreTableModel.Row> extends AbstractTableModel implements MTableModel {

    protected final List<R> rows = new ArrayList<>();
    protected final List<R> removedRows = new ArrayList<>();

    protected abstract R createRow();

    public void newRow(R row) {
        newRow(row, rows.size());
    }

    public void newRow(R row, int index) {
        row.table = this;
        row.index = Math.min(index, rows.size());
        rows.add(row);
        fireTableRowsInserted(row.index, row.index);
    }

    @Override
    public boolean newRow(int index) {
        newRow(createRow(), index);

        return true;
    }

    public boolean removeRow(int row) {
        if (row >= 0 && row < rows.size()) {
            R r = rows.remove(row);

            if (!r.isNew()) {
                r.removed = true;
                r.index = -1;
                removedRows.add(r);
            }
            resetIndex(row);
            fireTableDataChanged();
            return true;
        }

        return false;
    }

    @Override
    public boolean swapRows(int rowI, int rowJ) {
        if (rowI < 0 || rowI >= rows.size() || rowJ < 0 || rowJ >= rows.size() || rowI == rowJ) {
            return false; // Swapping is only allowed between keys that aren't deleted
        }

        R rI = rows.get(rowI);
        R rJ = rows.get(rowJ);
        rows.set(rowI, rJ);
        rows.set(rowJ, rI);
        rI.index = rowJ;
        rJ.index = rowI;

        if (rowI < rowJ) {
            fireTableRowsUpdated(rowI, rowJ);
        } else {
            fireTableRowsUpdated(rowJ, rowI);
        }

        return true;
    }

    @Override
    public boolean revert(int row, int column) {
        R r = getRow(row);

        if (r != null && !r.isNew() && r.canRevert(column)) {
            r.revert(column);
            return true;
        }
        return false;
    }

    @Override
    public boolean revert(ListSelectionModel selectedRows, ListSelectionModel selectedColumns) {
        return false;
    }

    public void restoreRow(int row) {
        int relativeRow = row - rows.size();

        if (relativeRow >= 0 && relativeRow < removedRows.size()) {
            R r = removedRows.remove(relativeRow);
            r.index = rows.size();
            rows.add(r);
            r.removed = false;
            fireTableDataChanged();
        }
    }

    private void resetIndex(int startInclusive) {
        for (int i = startInclusive; i < rows.size(); i++) {
            rows.get(i).index = i;
        }
    }

    public R getRow(int row) {
        if (row < 0 || row >= getRowCount()) {
            return null;
        } else if (row < rows.size()) {
            return rows.get(row);
        } else {
            return removedRows.get(row - rows.size());
        }
    }

    @Override
    public int getRowCount() {
        return rows.size() + removedRows.size();
    }

    @Override
    public abstract int getColumnCount();

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (0 <= columnIndex && columnIndex < getColumnCount()) {
            R row = getRow(rowIndex);
            if (row == null) {
                return null;
            }

            return row.getValue(columnIndex);
        }

        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (0 <= columnIndex && columnIndex < getColumnCount()) {
            R row = getRow(rowIndex);

            if (row != null) {
                row.setValue(aValue, columnIndex);
            }
        }
    }

    public int notRemovedRowCount() {
        return rows.size();
    }

    public int removedRowCount() {
        return removedRows.size();
    }

    public List<R> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public List<R> getRemovedRows() {
        return Collections.unmodifiableList(removedRows);
    }

    public abstract static class Row {

        protected RestoreTableModel<?> table;
        int index;
        boolean removed;

        public abstract Object getValue(int column);

        public abstract void setValue(Object value, int column);

        public abstract void revert(int column);

        public boolean canRevert(int column) {
            return !isNew() && hasChanged(column);
        }

        public abstract boolean isNew();

        public boolean isRemoved() {
            return removed;
        }

        public abstract boolean hasChanged(int column);
    }
}
