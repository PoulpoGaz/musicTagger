package fr.poulpogaz.musicdl.model;

import java.util.EventListener;

public interface TemplateKeyListListener extends EventListener {

    int KEYS_REMOVED = 1;
    int KEYS_ADDED = 2;
    int KEYS_MODIFIED = 3;
    int KEYS_SWAPPED = 4;

    /**
     *
     * @param type type of event
     * @param index1 inclusive
     * @param index2 inclusive
     */
    void keyListModified(int type, int index1, int index2);
}
