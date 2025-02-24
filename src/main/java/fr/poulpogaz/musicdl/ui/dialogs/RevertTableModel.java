package fr.poulpogaz.musicdl.ui.dialogs;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class RevertTableModel<R extends RevertTableModel.Row> extends AbstractTableModel {

    protected final List<R> rows = new ArrayList<>();
    protected final List<R> removedRows = new ArrayList<>();

    public void newRow(R row) {
        row.table = this;
        row.index = rows.size();
        rows.add(row);
        fireTableRowsInserted(row.index, row.index);
    }

    public void removeRow(int row) {
        R r = rows.remove(row);

        if (!r.isNew() && isRestoreEnabled()) {
            r.removed = true;
            r.index = -1;
            removedRows.add(r);
        }
        resetIndex(row);
        fireTableDataChanged();
    }

    public void restoreRow(int row) {
        if (isRestoreEnabled()) {
            int relativeRow = row - rows.size();

            if (relativeRow >= 0 && relativeRow < removedRows.size()) {
                R r = removedRows.remove(relativeRow);
                r.index = rows.size();
                rows.add(r);
                r.removed = false;
                fireTableDataChanged();
            }
        }
    }

    public boolean swap(int rowI, int rowJ) {
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

    public boolean moveUp(int row) {
        return swap(row - 1, row);
    }

    public boolean moveDown(int row) {
        return swap(row, row + 1);
    }

    public void revertValue(int row, int column) {
        R r = getRow(row);

        if (r != null && !r.isNew() && r.canRevert(column)) {
            r.revert(column);
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

    public boolean isRestoreEnabled() {
        return true;
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

        protected RevertTableModel<?> table;
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
