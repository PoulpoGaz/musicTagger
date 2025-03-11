package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.ui.table.MTable;
import fr.poulpogaz.musicdl.ui.table.RemoveRowAction;

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
