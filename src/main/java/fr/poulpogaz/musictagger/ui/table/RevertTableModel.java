package fr.poulpogaz.musictagger.ui.table;

import javax.swing.*;
import javax.swing.table.TableModel;

public interface RevertTableModel extends TableModel {

    boolean revert(int row, int column);

    boolean revert(ListSelectionModel selectedRows, ListSelectionModel selectedColumns);
}
