package fr.poulpogaz.musicdl.ui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class MTable extends JTable {

    protected Action newRowAction;
    protected Action removeRowAction;
    protected Action moveUpAction;
    protected Action moveDownAction;
    protected Action setNullAction;
    protected Action revertAction;

    protected final List<Action> actions = new ArrayList<>();
    protected MTableModel model;

    public MTable(MTableModel model) {
        super(model);
    }

    @Override
    public void setModel(TableModel dataModel) {
        if (dataModel instanceof MTableModel mModel) {
            this.model = mModel;
            super.setModel(dataModel);
        }
    }

    @Override
    public MTableModel getModel() {
        return (MTableModel) super.getModel();
    }



    @Override
    public void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);
        if (!e.getValueIsAdjusting()) {
            setActionEnable();
        }
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
        super.columnSelectionChanged(e);

        if (!e.getValueIsAdjusting()) {
            setActionEnable();
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        setActionEnable();
    }

    private void setActionEnable() {
        if (actions != null) {
            for (Action a : actions) {
                a.setEnabled(a.isEnabled());
            }
        }
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public void removeAction(Action action) {
        actions.remove(action);
    }

    public List<Action> getActions() {
        return actions;
    }


    public Action newRowAction(String rowName) {
        return newRowAction("New " + rowName,
                            "Create a new " + rowName + " below selection");
    }

    public Action newRowAction(String name, String tooltip) {
        if (newRowAction == null) {
            newRowAction = new AbstractAction(name, Icons.get("add.svg")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newRow(e);
                }

                @Override
                public boolean isEnabled() {
                    return isNewRowEnabled();
                }
            };
            newRowAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
            newRowAction.setEnabled(isNewRowEnabled());
            addAction(newRowAction);
        }

        return newRowAction;
    }

    public Action getNewRowAction() {
        return newRowAction;
    }

    protected void newRow(ActionEvent e) {
        int row = getSelectionModel().getMaxSelectionIndex();
        if (row == -1) {
            row = getRowCount();
        } else {
            row = Math.min(row + 1, getRowCount());
        }

        if (model.newRow(row)) {
            getSelectionModel().setSelectionInterval(row, row);
        }
    }

    protected boolean isNewRowEnabled() {
        return true;
    }



    public Action removeRowAction(String rowName) {
        return removeRowAction("Remove " + rowName,
                               "Remove selected " + rowName);
    }

    public Action removeRowAction(String name, String tooltip) {
        if (removeRowAction == null) {
            removeRowAction = new AbstractAction(name, Icons.get("delete.svg")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeRow(e);
                }

                @Override
                public boolean isEnabled() {
                    return isRemoveRowEnabled();
                }
            };
            removeRowAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
            removeRowAction.setEnabled(isRemoveRowEnabled());
            addAction(removeRowAction);
        }

        return removeRowAction;
    }

    public Action getRemoveRowAction() {
        return removeRowAction;
    }

    protected void removeRow(ActionEvent e) {
        int i = getSelectedRow();

        if (i != -1 && model.removeRow(i)) {
            int newSelection = Math.min(i, model.getRowCount() - 1);
            if (newSelection >= 0) {
                setRowSelectionInterval(newSelection, newSelection);
            }
        }
    }

    protected boolean isRemoveRowEnabled() {
        return getSelectedRow() >= 0 && getSelectedColumn() >= 0;
    }



    public Action moveUpAction(String rowName) {
        return moveUpAction("Move up",
                            "Move up selected " + rowName);
    }

    public Action moveUpAction(String name, String tooltip) {
        if (moveUpAction == null) {
            moveUpAction = new AbstractAction(name, Icons.get("move_up.svg")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveUp(e);
                }

                @Override
                public boolean isEnabled() {
                    return isMoveUpEnabled();
                }
            };
            moveUpAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
            moveUpAction.setEnabled(isMoveUpEnabled());
            addAction(moveUpAction);
        }

        return moveUpAction;
    }

    public Action getMoveUpAction() {
        return moveUpAction;
    }

    protected void moveUp(ActionEvent e) {
        int row = getSelectedRow();
        if (row > 0 && model.moveUp(row)) {
            setRowSelectionInterval(row - 1, row - 1);
        }
    }

    protected boolean isMoveUpEnabled() {
        return getSelectedRow() > 0;
    }



    public Action moveDownAction(String rowName) {
        return moveDownAction("Move down",
                              "Move down selected " + rowName);
    }

    public Action moveDownAction(String name, String tooltip) {
        if (moveDownAction == null) {
            moveDownAction = new AbstractAction(name, Icons.get("move_down.svg")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveDown(e);
                }

                @Override
                public boolean isEnabled() {
                    return isMoveDownEnabled();
                }
            };
            moveDownAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
            moveDownAction.setEnabled(isMoveDownEnabled());
            addAction(moveDownAction);
        }

        return moveDownAction;
    }

    public Action getMoveDownAction() {
        return moveDownAction;
    }

    protected void moveDown(ActionEvent e) {
        int row = getSelectedRow();
        if (row != -1 && model.moveDown(row)) {
            setRowSelectionInterval(row + 1, row + 1);
        }
    }

    protected boolean isMoveDownEnabled() {
        int r = getSelectedRow();
        return r >= 0 && r < getRowCount() - 1;
    }



    public Action revertAction() {
        return revertAction("Revert",
                            "Revert selected cells to their previous values");
    }

    public Action revertAction(String name, String tooltip) {
        if (revertAction == null) {
            revertAction = new AbstractAction(name, null) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    revert(e);
                }

                @Override
                public boolean isEnabled() {
                    return isRevertEnabled();
                }
            };
            revertAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
            revertAction.setEnabled(isMoveDownEnabled());
            addAction(revertAction);
        }

        return revertAction;
    }

    public Action getRevertAction() {
        return revertAction;
    }

    protected void revert(ActionEvent e) {
        ListSelectionModel selectedRows = getSelectionModel();
        ListSelectionModel selectedColumns = getColumnModel().getSelectionModel();

        if (selectedRows.isSelectionEmpty() || selectedColumns.isSelectionEmpty()) {
            return;
        }

        if (isSingleItemSelected(selectedRows) && isSingleItemSelected(selectedColumns)) {
            model.revert(selectedRows.getMinSelectionIndex(), selectedColumns.getMinSelectionIndex());
        } else {
            model.revert(selectedRows, selectedColumns);
        }
    }

    private boolean isSingleItemSelected(ListSelectionModel model) {
        return model.getMaxSelectionIndex() == model.getMinSelectionIndex();
    }

    protected boolean isRevertEnabled() {
        return !getSelectionModel().isSelectionEmpty()
                && !getColumnModel().getSelectionModel().isSelectionEmpty();
    }




    public Action setNullAction() {
        return setNullAction("Set null",
                             "Replace selected cell values with null values");
    }

    public Action setNullAction(String name, String tooltip) {
        if (setNullAction == null) {
            setNullAction = new AbstractAction(name, null) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setNull(e);
                }

                @Override
                public boolean isEnabled() {
                    return isSetNullEnabled();
                }
            };
            setNullAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
            setNullAction.setEnabled(isMoveDownEnabled());
            addAction(setNullAction);
        }

        return setNullAction;
    }

    public Action getSetNullAction() {
        return setNullAction;
    }

    protected void setNull(ActionEvent e) {
        ListSelectionModel selectedRows = getSelectionModel();
        ListSelectionModel selectedColumns = getColumnModel().getSelectionModel();

        if (selectedRows.isSelectionEmpty() || selectedColumns.isSelectionEmpty()) {
            return;
        }

        model.setValueAt(null, selectedRows.getMinSelectionIndex(), selectedColumns.getMinSelectionIndex());
    }

    protected boolean isSetNullEnabled() {
        return !getSelectionModel().isSelectionEmpty()
                && !getColumnModel().getSelectionModel().isSelectionEmpty();
    }
}
