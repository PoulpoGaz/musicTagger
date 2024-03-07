package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.model.Template;
import fr.poulpogaz.musicdb.model.TemplateKeyListListener;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemplateTableModel extends AbstractTableModel {

    private final Template template;
    private final List<String[]> rows = new ArrayList<>();

    public TemplateTableModel(Template template) {
        this.template = template;

        template.addTemplateKeyListListener(this::updateTable);
    }

    private void updateTable(int eventType, int index0, int index1) {
        // offset because first column is download url which is not a key
        index0++;
        index1++;

        switch (eventType) {
            case TemplateKeyListListener.KEYS_ADDED -> {
                for (int i = 0; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    int addLength = index1 - index0 + 1;

                    row = Arrays.copyOf(row, row.length + addLength);
                    System.arraycopy(row, index0,
                                     row, index1 + 1,
                                     addLength);
                    Arrays.fill(row, index0, index1, null);

                    rows.set(i, row);
                }
            }
            case TemplateKeyListListener.KEYS_REMOVED -> {
                for (int i = 0; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    int removeLength = index1 - index0 + 1;

                    System.arraycopy(row, index1 + 1,
                                     row, index0,
                                     row.length - index1 - 1);
                    row = Arrays.copyOf(row, row.length - removeLength);

                    rows.set(i, row);
                }
            }
            case TemplateKeyListListener.KEYS_SWAPPED -> {
                for (String[] row : rows) {
                    String temp = row[index0];
                    row[index0] = row[index1];
                    row[index1] = temp;
                }
            }
        }
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Download URL";
        } else {
            return template.getKeyName(column - 1);
        }
    }

    @Override
    public int getColumnCount() {
        return 1 + template.keyCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex)[columnIndex];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (isCellEditable(rowIndex, columnIndex)) {
            rows.get(rowIndex)[columnIndex] = (String) aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return rowIndex >= 0 && rowIndex < getRowCount() &&
                columnIndex >= 0 && columnIndex < getColumnCount();

    }

    public void addRow() {
        addRow(rows.size());
    }

    public void addRow(int index) {
        if (index < 0 || index > rows.size()) {
            index = rows.size();
        }

        rows.add(index, new String[getColumnCount()]);
        fireTableRowsInserted(index, index);
    }

    public void deleteRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            rows.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public Template getTemplate() {
        return template;
    }
}
