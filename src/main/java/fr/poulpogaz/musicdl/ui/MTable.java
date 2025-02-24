package fr.poulpogaz.musicdl.ui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;

public class MTable extends JTable {

    protected Action newRowAction;
    protected Action removeRowAction;
    protected Action moveUpAction;
    protected Action moveDownAction;

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
        if (newRowAction != null) newRowAction.setEnabled(isNewRowEnabled());
        if (removeRowAction != null) removeRowAction.setEnabled(isRemoveRowEnabled());
        if (moveUpAction != null) moveUpAction.setEnabled(isMoveUpEnabled());
        if (moveDownAction != null) moveDownAction.setEnabled(isMoveDownEnabled());
    }


    public Action newRowAction(String name, String tooltip) {
        if (newRowAction == null) {
            newRowAction = new AbstractAction(name, Icons.get("add.svg")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newRow(e);
                }
            };
            newRowAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
            newRowAction.setEnabled(isNewRowEnabled());
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



    public Action removeRowAction(String name, String tooltip) {
        if (removeRowAction == null) {
            removeRowAction = new AbstractAction(name, Icons.get("delete.svg")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeRow(e);
                }
            };
            removeRowAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
            removeRowAction.setEnabled(isRemoveRowEnabled());
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



    public Action moveUpAction(String name, String tooltip) {
        if (moveUpAction == null) {
            moveUpAction = new AbstractAction(name, Icons.get("move_up.svg")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveUp(e);
                }
            };
            moveUpAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
            moveUpAction.setEnabled(isMoveUpEnabled());
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



    public Action moveDownAction(String name, String tooltip) {
        if (moveDownAction == null) {
            moveDownAction = new AbstractAction(name, Icons.get("move_down.svg")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveDown(e);
                }
            };
            moveDownAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
            moveDownAction.setEnabled(isMoveDownEnabled());
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
}
