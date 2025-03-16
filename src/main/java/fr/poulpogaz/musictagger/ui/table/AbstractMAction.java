package fr.poulpogaz.musictagger.ui.table;

import javax.swing.*;
import java.util.Objects;

public abstract class AbstractMAction extends AbstractAction implements MAction {

    protected MTable table;

    public AbstractMAction() {
        setEnabled(isEnabled());
    }

    public AbstractMAction(String name) {
        super(name);
        setEnabled(isEnabled());
    }

    public AbstractMAction(String name, Icon icon) {
        super(name, icon);
        setEnabled(isEnabled());
    }

    public AbstractMAction(MTable table) {
        setTable(Objects.requireNonNull(table)); // if null, setEnabled won't be called in setTable
    }

    public AbstractMAction(String name, MTable table) {
        super(name);
        setTable(Objects.requireNonNull(table)); // if null, setEnabled won't be called in setTable
    }

    public AbstractMAction(String name, Icon icon, MTable table) {
        super(name, icon);
        setTable(Objects.requireNonNull(table)); // if null, setEnabled won't be called in setTable
    }

    @Override
    public void setTable(MTable table) {
        if (table != this.table) {
            if (this.table != null) {
                this.table.removeAction(this);
            }

            this.table = table;

            if (table != null) {
                table.addAction(this);
            }

            setEnabled(isEnabled());
        }
    }

    @Override
    public MTable geTable() {
        return table;
    }
}
