package fr.poulpogaz.musictagger.model;

import java.util.EventListener;

public interface TemplateKeysListener extends EventListener {

    void keysModified(Template template);
}
