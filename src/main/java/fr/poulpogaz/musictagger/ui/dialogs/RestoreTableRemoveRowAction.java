package fr.poulpogaz.musictagger.ui.dialogs;

import fr.poulpogaz.musictagger.ui.table.MTable;
import fr.poulpogaz.musictagger.ui.table.RemoveRowAction;

public class RestoreTableRemoveRowAction extends RemoveRowAction {

    public RestoreTableRemoveRowAction(MTable table, String rowName) {
        super(table);
        putValues(this, rowName);
    }

    @Override
    protected int newSelectionIndex(MTable table, int firstRemovedRow) {
        RestoreTableModel<?> model = (RestoreTableModel<?>) table.getModel();

        return Math.min(firstRemovedRow, model.notRemovedRowCount() - 1);
    }
}
