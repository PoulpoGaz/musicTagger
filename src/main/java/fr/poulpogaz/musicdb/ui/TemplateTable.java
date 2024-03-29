package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.model.Template;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TemplateTable extends JPanel {

    private final TemplateTableModel tableModel;

    private JScrollPane tableScrollPane;
    private JTable table;
    private TemplateTablePopupMenu tablePopupMenu;

    public TemplateTable(Template template) {
        this.tableModel = new TemplateTableModel(template);
        initComponents();
    }

    protected void initComponents() {
        table = createTable();
        tableScrollPane = new JScrollPane();

        tablePopupMenu = new TemplateTablePopupMenu();
        table.addMouseListener(new TablePopupMenuSupport(table, tablePopupMenu));

        setLayout(new BorderLayout());
        tableScrollPane.setViewportView(table);
        add(tableScrollPane, BorderLayout.CENTER);
    }

    protected JTable createTable() {
        JTable jTable = new JTable();
        jTable.setDefaultRenderer(Object.class, new CellRenderer());
        jTable.setColumnSelectionAllowed(true);
        jTable.setRowSelectionAllowed(true);
        jTable.setShowVerticalLines(true);
        jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTable.setFillsViewportHeight(true);
        jTable.setModel(tableModel);

        return jTable;
    }

    public void addMusicBelowSelection() {
        int row = table.getSelectionModel().getMaxSelectionIndex();
        if (row == -1) {
            tableModel.addRow(table.getRowCount());
        } else {
            tableModel.addRow(row + 1);
        }
    }

    public void deleteSelectedMusics() {
        tableModel.deleteSelectedRows(table.getSelectionModel());
    }

    public void unsetSelectedCell() {
        ListSelectionModel selectedRows = table.getSelectionModel();
        ListSelectionModel selectedColumns = table.getColumnModel().getSelectionModel();
        tableModel.setNullValues(selectedRows, selectedColumns);
    }

    public void downloadSelectedMusics() {
        ListSelectionModel selectedRows = table.getSelectionModel();

        int min = selectedRows.getMinSelectionIndex();
        int max = Math.min(selectedRows.getMaxSelectionIndex() + 1, table.getRowCount());

        for (int i = min; i <= max; i++) {
            if (selectedRows.isSelectedIndex(i)) {
                tableModel.download(i);
            }
        }
    }

    public TemplateTableModel getModel() {
        return tableModel;
    }


    private static class CellRenderer extends CellRendererBase {

        @Override
        protected int getStatusOfCell(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return DEFAULT;
        }
    }



    private class TemplateTablePopupMenu extends JPopupMenu {

        protected JMenuItem addMenuItem;
        protected JMenuItem removeMenuItem;
        protected JMenuItem unset;
        protected JMenuItem download;

        public TemplateTablePopupMenu() {
            initPopup();
        }

        protected void initPopup() {
            addMenuItem = add("Add music");
            removeMenuItem = add("Remove music");
            addSeparator();
            unset = add("Unset");
            download = add("Download music");

            addMenuItem.addActionListener((ActionEvent e) -> addMusicBelowSelection());
            removeMenuItem.addActionListener((ActionEvent e) -> deleteSelectedMusics());
            unset.addActionListener((ActionEvent e) -> unsetSelectedCell());
            download.addActionListener((ActionEvent e) -> downloadSelectedMusics());
        }
    }
}
