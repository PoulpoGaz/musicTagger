package fr.poulpogaz.musicdl.ui.table;

import javax.swing.*;

public interface MAction extends Action {

    void setTable(MTable table);

    MTable geTable();
}
