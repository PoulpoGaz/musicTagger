package fr.poulpogaz.musicdl.model;

import java.util.EventListener;

public interface TemplateKeysListener extends EventListener {

    void keysModified(Template template);
}
