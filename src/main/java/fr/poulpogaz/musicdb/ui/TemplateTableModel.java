package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.model.Template;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TemplateTableModel extends AbstractTableModel {

    private final Template template;
    private final List<String[]> rows = new ArrayList<>();

    public TemplateTableModel(Template template) {
        this.template = template;
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
