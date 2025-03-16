package fr.poulpogaz.musictagger.ui.table;

import javax.swing.*;
import java.util.List;

public class ActionUtils {

    public static JToolBar toolBarFromActions(List<Action> actions) {
        JToolBar bar = new JToolBar();
        for (Action action : actions) {
            if (action == null) {
                bar.addSeparator();
            } else {
                bar.add(action);
            }
        }

        return bar;
    }

    public static JPopupMenu popupMenuFromActions(List<Action> actions) {
        JPopupMenu menu = new JPopupMenu();
        for (Action action : actions) {
            if (action == null) {
                menu.addSeparator();
            } else {
                menu.add(action);
            }
        }

        return menu;
    }


    public static boolean isSingleItemSelected(ListSelectionModel model) {
        return !model.isSelectionEmpty() && model.getMaxSelectionIndex() == model.getMinSelectionIndex();
    }
}
