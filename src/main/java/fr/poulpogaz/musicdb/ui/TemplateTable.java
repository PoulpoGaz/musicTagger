package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.model.Template;
import fr.poulpogaz.musicdb.model.Templates;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
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
        tableModel.addRow(row + 1);
    }

    public void deleteSelectedMusics() {
        tableModel.deleteSelectedRows(table.getSelectionModel());
    }

    public void unsetSelectedCell() {
        ListSelectionModel selectedRows = table.getSelectionModel();
        ListSelectionModel selectedColumns = table.getColumnModel().getSelectionModel();
        tableModel.setNullValues(selectedRows, selectedColumns);
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

        public TemplateTablePopupMenu() {
            initPopup();
        }

        protected void initPopup() {
            addMenuItem = add("Add music");
            removeMenuItem = add("Remove music");
            addSeparator();
            unset = add("Unset");

            addMenuItem.addActionListener((ActionEvent e) -> addMusicBelowSelection());
            removeMenuItem.addActionListener((ActionEvent e) -> deleteSelectedMusics());
            unset.addActionListener((ActionEvent e) -> unsetSelectedCell());
        }
    }
}
