package fr.poulpogaz.musicdl.ui;

import fr.poulpogaz.musicdl.model.Music;
import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.model.Templates;
import fr.poulpogaz.musicdl.ui.dialogs.MetadataDialog;
import fr.poulpogaz.musicdl.ui.dialogs.SingleMetadataEditorDialog;
import fr.poulpogaz.musicdl.ui.table.AbstractMAction;
import fr.poulpogaz.musicdl.ui.table.MTable;
import fr.poulpogaz.musicdl.ui.table.NewRowAction;
import fr.poulpogaz.musicdl.ui.table.RemoveRowAction;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TemplateTable extends JPanel {

    private final TemplateTableModel tableModel;

    private MTable table;

    private Action newMusicAction;
    private Action removeMusicsAction;
    private Action transferAction; // TODO

    private Action editTagAction;
    private Action setNullAction;

    private Action downloadAction;
    private Action showMetadataAction;


    private JToolBar toolBar;
    private TemplateTablePopupMenu tablePopupMenu;

    public TemplateTable(Template template) {
        this.tableModel = new TemplateTableModel(template);
        initComponents();
    }

    protected void initComponents() {
        table = createTable();
        createActions();

        toolBar = createToolBar();

        tablePopupMenu = new TemplateTablePopupMenu();
        table.addMouseListener(new TablePopupMenuSupport(table, tablePopupMenu));

        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    protected MTable createTable() {
        MTable table = new MTable(tableModel);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setFillsViewportHeight(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int row = TemplateTable.this.table.rowAtPoint(point);
                int col = TemplateTable.this.table.columnAtPoint(point);
                if (e.getClickCount() == 2 && row != -1 && col != -1) {
                    int modelRow = TemplateTable.this.table.convertRowIndexToModel(row);
                    int modelCol = TemplateTable.this.table.convertColumnIndexToModel(col);

                    Music m = tableModel.getMusic(modelRow);

                    if (m.hasMultipleValues(col - 1)) {
                        openSingleMetadataEditor(modelRow, modelCol);
                    }
                }
            }
        });

        return table;
    }

    protected void createActions() {
        newMusicAction = NewRowAction.create(table, "music");
        removeMusicsAction = RemoveRowAction.create(table, "music");

        editTagAction = createEditTagAction();
        setNullAction = createSetNullAction();

        downloadAction = createDownloadAction();
        showMetadataAction = createShowMetadataAction();
    }

    protected Action createEditTagAction() {
        return new AbstractMAction("Edit tag", table) {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSingleMetadataEditor(table.getSelectedRow(), table.getSelectedColumn());
            }

            @Override
            public boolean isEnabled() {
                return tableModel.canOpenTagEditor(table.getSelectedRow(), table.getSelectedColumn());
            }
        };
    }

    protected Action createSetNullAction() {
        return new AbstractMAction("Unset", table) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ListSelectionModel selectedRows = table.getSelectionModel();
                ListSelectionModel selectedColumns = table.getColumnModel().getSelectionModel();
                tableModel.setNullValues(selectedRows, selectedColumns);
            }

            @Override
            public boolean isEnabled() {
                return !table.getSelectionModel().isSelectionEmpty()
                        && !table.getColumnModel().getSelectionModel().isSelectionEmpty();
            }
        };
    }

    protected Action createDownloadAction() {
        return new AbstractMAction("Download", table) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ListSelectionModel selectedRows = table.getSelectionModel();
                tableModel.downloadSelected(selectedRows);
            }

            @Override
            public boolean isEnabled() {
                return table.getSelectedRow() >= 0;
            }
        };
    }

    protected Action createShowMetadataAction() {
        return new AbstractMAction("Show all metadata", table) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();

                if (row >= 0) {
                    Music m = tableModel.getTemplate().getData().getMusic(row);

                    if (m.isDownloaded()) {
                        MetadataDialog.showDialog(MusicdlFrame.getInstance(), m);
                    }
                }
            }

            @Override
            public boolean isEnabled() {
                return table.getSelectedRow() >= 0 &&
                        tableModel.getTemplate().getData().getMusic(table.getSelectedRow())
                                  .isDownloaded();
            }
        };
    }

    protected JToolBar createToolBar() {
        JToolBar bar = new JToolBar(SwingConstants.VERTICAL);
        bar.add(newMusicAction);
        bar.add(removeMusicsAction);
        return bar;
    }

    private void openSingleMetadataEditor(int row, int column) {
        if (tableModel.canOpenTagEditor(row, column)) {
            Music m = tableModel.getMusic(row);
            String key = tableModel.getMetadataKey(column);

            SingleMetadataEditorDialog.showDialog(MusicdlFrame.getInstance(), m, key);
        }
    }

    public void transferSelectionTo(Template template) {
        tableModel.transferSelectionTo(table.getSelectionModel(), template);
    }

    public TemplateTableModel getModel() {
        return tableModel;
    }

    public Template getTemplate() {
        return tableModel.getTemplate();
    }

    public ListSelectionModel getSelectedRows() {
        return table.getSelectionModel();
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    private static class CellRenderer extends CellRendererBase {

        @Override
        protected int getStatusOfCell(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TemplateTableModel model = (TemplateTableModel) jTable.getModel();
            int mRow = jTable.convertRowIndexToModel(row);
            int mCol = jTable.convertColumnIndexToModel(column);

            if (model.isCellEditable(row, column) || model.canOpenTagEditor(mRow, mCol)) {
                return DEFAULT;
            } else {
                return UNEDITABLE;
            }
        }
    }



    private class TemplateTablePopupMenu extends JPopupMenu implements PopupMenuListener {

        protected JMenu changeTemplate;

        public TemplateTablePopupMenu() {
            initPopup();
            // addPopupMenuListener(this);
        }

        protected void initPopup() {
            add(newMusicAction);
            add(removeMusicsAction);
            // TODO: change template
            addSeparator();

            add(editTagAction);
            add(setNullAction);
            addSeparator();

            add(downloadAction);
            add(showMetadataAction);
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
                    Template template = panel.getTemplateTable(i).getTemplate();

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
