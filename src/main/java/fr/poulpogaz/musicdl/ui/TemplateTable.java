package fr.poulpogaz.musicdl.ui;

import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.model.Templates;
import fr.poulpogaz.musicdl.ui.dialogs.MetadataDialog;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
            tableModel.addRow(Math.min(row + 1, table.getRowCount()));
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

    public void transferSelectionTo(Template template) {
        tableModel.transferSelectionTo(table.getSelectionModel(), template);
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



    private class TemplateTablePopupMenu extends JPopupMenu implements PopupMenuListener {

        protected JMenuItem addMenuItem;
        protected JMenuItem removeMenuItem;
        protected JMenu changeTemplate;
        protected JMenuItem showMetadata;
        protected JMenuItem unset;
        protected JMenuItem download;

        public TemplateTablePopupMenu() {
            initPopup();
            addPopupMenuListener(this);
        }

        protected void initPopup() {
            addMenuItem = add("Add music");
            removeMenuItem = add("Remove music");
            changeTemplate = new JMenu("Change template");
            add(changeTemplate);
            showMetadata = add("Show metadata");
            addSeparator();
            unset = add("Unset");
            download = add("Download music");

            addMenuItem.addActionListener(_ -> addMusicBelowSelection());
            removeMenuItem.addActionListener(_ -> deleteSelectedMusics());
            showMetadata.addActionListener(_ -> {
                int row = table.getSelectionModel().getMinSelectionIndex();

                if (row >= 0) {
                    MetadataDialog.showDialog(MusicdlFrame.getInstance(),
                                              tableModel.getTemplate().getData().getMusic(row));
                }
            });
            unset.addActionListener(_ -> unsetSelectedCell());
            download.addActionListener(_ -> downloadSelectedMusics());
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            if (Templates.templateCount() <= 1) {
                changeTemplate.setEnabled(false);
            } else {
                changeTemplate.setEnabled(true);
                TemplatesPanel panel = MusicdlFrame.getInstance().getTemplatesPanel();

                int i;
                for (i = 0; i < panel.getTemplateTableCount(); i++) {
                    Template template = panel.getTemplateTable(i).getModel().getTemplate();

                    if (template == panel.getSelectedTemplate()) {
                        continue;
                    }

                    if (i < changeTemplate.getItemCount()) {
                        changeTemplate.setName(template.getName());
                    } else {
                        JMenuItem item = changeTemplate.add(template.getName());
                        item.addActionListener(this::transferTemplate);
                    }
                }

                int expectedSize = panel.getTemplateTableCount() - 1;
                while (changeTemplate.getItemCount() > expectedSize) {
                    changeTemplate.remove(expectedSize);
                }
            }
        }

        private void transferTemplate(ActionEvent actionEvent) {
            JMenuItem item = (JMenuItem) actionEvent.getSource();
            String templateName = item.getText();

            Template dest = Templates.getTemplate(templateName);
            transferSelectionTo(dest);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {

        }
    }
}
