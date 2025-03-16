package fr.poulpogaz.musictagger.ui.table;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

public class MTable extends JTable {

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
}
