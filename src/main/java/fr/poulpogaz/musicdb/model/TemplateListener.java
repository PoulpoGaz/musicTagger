package fr.poulpogaz.musicdb.model;

import java.util.EventListener;

public interface TemplateListener extends EventListener {

    void onTemplateModification(TemplateEvent event);
}
