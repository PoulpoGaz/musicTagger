package fr.poulpogaz.musictagger.ui.dialogs;

import fr.poulpogaz.musictagger.model.Music;
import fr.poulpogaz.musictagger.ui.layout.HCOrientation;
import fr.poulpogaz.musictagger.ui.layout.HorizontalConstraint;
import fr.poulpogaz.musictagger.ui.layout.HorizontalLayout;
import fr.poulpogaz.musictagger.ui.table.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SingleMetadataEditorDialog extends AbstractDialog {

    public static void showDialog(Frame frame, Music music, String key) {
        new SingleMetadataEditorDialog(frame, music, key).setVisible(true);
    }

    private final Music music;
    private final String key;

    private JScrollPane scrollPane;
    private JList<String> oldValuesList;
    private MTable table;
    private TableModel tableModel;

    private JToolBar toolbar;

    private JButton done;
    private JButton cancel;

    public SingleMetadataEditorDialog(Frame owner, Music music, String key) {
        super(owner, "Editing: " + key, true);
        this.music = music;
        this.key = key;
        init();
    }

    @Override
    protected void setBestSize() {
        setSize(512, 384);
    }

    @Override
    protected void initComponents() {
        List<String> values = music.getMetadata(key);

        ArrayListModel oldValuesModel = new ArrayListModel(values);
        oldValuesList = new JList<>(oldValuesModel);


        tableModel = new TableModel(oldValuesModel);
        table = new Table(tableModel);
        table.setShowVerticalLines(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDragEnabled(false);
        table.setShowHorizontalLines(true);

        List<Action> actions = List.of(
                NewRowAction.create(table, "value"),
                RemoveRowAction.create(table, "value"),
                MoveAction.moveUp(table, "value"),
                MoveAction.moveDown(table, "value")
        );


        toolbar = ActionUtils.toolBarFromActions(actions);
        toolbar.setOrientation(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        JPopupMenu popup = ActionUtils.popupMenuFromActions(actions);
        TablePopupMenuSupport.install(table, popup);


        done = new JButton("Done");
        done.addActionListener(this::done);
        cancel = new JButton("Cancel");
        cancel.addActionListener(this::cancel);

        getRootPane().setDefaultButton(done);


        JSplitPane pane = new JSplitPane();
        pane.setLeftComponent(oldValuesList);
        pane.setRightComponent(table);
        pane.setResizeWeight(0.5);
        scrollPane = new JScrollPane(pane);


        JPanel south = new JPanel();
        south.setLayout(new HorizontalLayout(4));
        HorizontalConstraint hc = new HorizontalConstraint();
        hc.orientation = HCOrientation.RIGHT;
        south.add(cancel, hc);
        south.add(done, hc);


        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(toolbar, BorderLayout.WEST);
        add(south, BorderLayout.SOUTH);
    }

    private void done(ActionEvent e) {
        List<String> metadata = music.getMetadata(key);
        if (metadata != null) {
            metadata.clear();
            for (String v : tableModel.values) {
                if (v != null && !v.isEmpty()) {
                    metadata.add(v);
                }
            }
            music.notifyChanges();
        }

        dispose();
    }

    private void cancel(ActionEvent e) {
        dispose();
    }

    private class Table extends MTable {

        public Table(MTableModel model) {
            super(model);
        }

        @Override
        public void tableChanged(TableModelEvent e) {
            super.tableChanged(e);
            if (scrollPane != null) {
                scrollPane.revalidate();
            }
        }
    }

    private static class TableModel extends AbstractTableModel implements MTableModel {

        private final List<String> values;

        public TableModel(ArrayListModel originalValues) {
            values = new ArrayList<>(originalValues.values);
        }

        @Override
        public boolean newRow(int index) {
            index = Math.min(index, values.size());
            values.add(index, null);
            fireTableRowsInserted(index, index);
            return true;
        }

        @Override
        public boolean removeRow(int index) {
            if (index >= 0 && index < values.size()) {
                values.remove(index);
                fireTableRowsDeleted(index, index);
                return true;
            }
            return false;
        }

        @Override
        public boolean swapRows(int rowI, int rowJ) {
            if (rowI < 0 || rowI >= values.size() || rowJ < 0 || rowJ >= values.size() || rowI == rowJ) {
                return false;
            }

            Collections.swap(values, rowI, rowJ);
            if (rowI < rowJ) {
                fireTableRowsUpdated(rowI, rowJ);
            } else {
                fireTableRowsUpdated(rowJ, rowI);
            }

            return true;
        }

        @Override
        public int getRowCount() {
            return values.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= 0 && rowIndex < values.size()) {
                return values.get(rowIndex);
            }

            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex >= 0 && rowIndex < values.size()) {
                values.set(rowIndex, (String) aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return rowIndex < values.size();
        }

        @Override
        public String getColumnName(int column) {
            return "New values";
        }
    }

    private static class ArrayListModel extends AbstractListModel<String> {

        private final List<String> values;

        public ArrayListModel(List<String> values) {
            this.values = values;
        }

        @Override
        public int getSize() {
            return values.size();
        }

        @Override
        public String getElementAt(int index) {
            return values.get(index);
        }
    }
}
