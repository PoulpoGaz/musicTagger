package fr.poulpogaz.musicdl.ui;

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
}
