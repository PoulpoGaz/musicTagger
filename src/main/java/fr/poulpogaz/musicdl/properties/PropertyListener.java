package fr.poulpogaz.musicdl.properties;

import java.util.EventListener;

public interface PropertyListener<T> extends EventListener {

    void propertyChanged(Property<? extends T> property, T oldValue, T newValue);
}
