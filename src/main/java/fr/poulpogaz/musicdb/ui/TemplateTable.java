package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.model.Template;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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
        jTable.setColumnSelectionAllowed(false);
        jTable.setRowSelectionAllowed(true);
        jTable.setShowVerticalLines(true);
        jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable.setFillsViewportHeight(true);
        jTable.setModel(tableModel);

        return jTable;
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

        public TemplateTablePopupMenu() {
            initPopup();
        }

        protected void initPopup() {
            addMenuItem = add("Add music");
            removeMenuItem = add("Remove music");

            addMenuItem.addActionListener((ActionEvent e) -> {
                int row = table.getSelectedRow();
                tableModel.addRow(row);
            });

            removeMenuItem.addActionListener((ActionEvent e) -> {
                int row = table.getSelectedRow();
                tableModel.deleteRow(row);
            });
        }
    }
}
