package fr.poulpogaz.musicdl.ui;

import fr.poulpogaz.musicdl.model.Music;
import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.model.Templates;
import fr.poulpogaz.musicdl.ui.dialogs.MetadataDialog;
import fr.poulpogaz.musicdl.ui.dialogs.SingleMetadataEditorDialog;
import fr.poulpogaz.musicdl.ui.table.*;

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
    private JMenu changeTemplate;

    private Action editMetadataAction;
    private Action revertAction;
    private Action setNullAction;

    private Action downloadAction;
    private Action showMetadataAction;


    private JPopupMenu tablePopupMenu;

    public TemplateTable(Template template) {
        this.tableModel = new TemplateTableModel(template);
        initComponents();
    }

    protected void initComponents() {
        table = createTable();
        createActions();

        tablePopupMenu = createPopupMenu();
        TablePopupMenuSupport.install(table, tablePopupMenu);

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

                    if (col != 0 && m.hasMultipleValues(col - 1)) {
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

        editMetadataAction = createEditTagAction();
        revertAction = RevertAction.create(table);
        setNullAction = createSetNullAction();

        downloadAction = createDownloadAction();
        showMetadataAction = createShowMetadataAction();
    }

    protected Action createEditTagAction() {
        Action action = new AbstractMAction("Edit tag", Icons.get("edit.svg"), table) {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSingleMetadataEditor(table.getSelectedRow(), table.getSelectedColumn());
            }

            @Override
            public boolean isEnabled() {
                return tableModel.canOpenTagEditor(table.getSelectedRow(), table.getSelectedColumn());
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, "Edit selected tag in another window (for multi-value editing)");
        return action;
    }

    protected Action createSetNullAction() {
        Action action = new AbstractMAction("Unset", table) {
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
        action.putValue(Action.SHORT_DESCRIPTION, "Remove values for every selected cells");
        return action;
    }

    protected Action createDownloadAction() {
        Action action = new AbstractMAction("Download", Icons.get("download.svg"), table) {
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
        action.putValue(Action.SHORT_DESCRIPTION, "Download selected musics");
        return action;
    }

    protected Action createShowMetadataAction() {
        Action action = new AbstractMAction("Show all metadata", table) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();

                if (row >= 0) {
                    Music m = tableModel.getMusic(row);
                    if (!m.isDownloading()) {
                        MetadataDialog.showDialog(MusicdlFrame.getInstance(), m);
                    }
                }
            }

            @Override
            public boolean isEnabled() {
                return table.getSelectedRow() >= 0 && !tableModel.getMusic(table.getSelectedRow()).isDownloading();
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, "Show all metadata of selected musics");
        return action;
    }

    protected JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        changeTemplate = new JMenu("Change template");

        menu.add(newMusicAction);
        menu.add(removeMusicsAction);
        menu.add(changeTemplate);
        menu.addSeparator();

        menu.add(editMetadataAction);
        menu.add(revertAction);
        menu.add(setNullAction);
        menu.addSeparator();

        menu.add(downloadAction);
        menu.add(showMetadataAction);

        menu.addPopupMenuListener(createPopupMenuListener());

        return menu;
    }

    protected PopupMenuListener createPopupMenuListener() {
        return new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (Templates.templateCount() <= 1
                        || table.getSelectionModel().isSelectionEmpty()
                        || table.getColumnModel().getSelectionModel().isSelectionEmpty()) {
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
                            item.addActionListener(TemplateTable.this::transferTemplate);
                        }
                    }

                    int expectedSize = panel.getTemplateTableCount() - 1;
                    while (changeTemplate.getItemCount() > expectedSize) {
                        changeTemplate.remove(expectedSize);
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        };
    }

    private void transferTemplate(ActionEvent actionEvent) {
        JMenuItem item = (JMenuItem) actionEvent.getSource();
        String templateName = item.getText();

        Template dest = Templates.getTemplate(templateName);
        transferSelectionTo(dest);
    }

    private void openSingleMetadataEditor(int row, int column) {
        if (tableModel.canOpenTagEditor(row, column)) {
            Music m = tableModel.getMusic(row);
            String key = tableModel.getMetadataField(column);

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

    public void populateToolbar(JToolBar toolBar) {
        toolBar.add(newMusicAction);
        toolBar.add(removeMusicsAction);
        toolBar.add(revertAction);
        toolBar.add(downloadAction);
    }

    private static class CellRenderer extends CellRendererBase {

        @Override
        protected int getStatusOfCell(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TemplateTableModel model = (TemplateTableModel) jTable.getModel();
            int mRow = jTable.convertRowIndexToModel(row);
            int mCol = jTable.convertColumnIndexToModel(column);

            if (model.hasChanged(mRow, mCol)) {
                return CHANGED;
            } else if (model.isCellEditable(row, column) || model.canOpenTagEditor(mRow, mCol)) {
                return DEFAULT;
            } else {
                return UNEDITABLE;
            }
        }
    }
}
