package fr.poulpogaz.musictagger.model;

import java.util.EventListener;

public interface TemplatesListener extends EventListener {

    int TEMPLATE_ADDED = 0;
    int TEMPLATE_REMOVED = 1;

    void templatesChanged(int eventType, Template template);
}
