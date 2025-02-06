package fr.poulpogaz.musicdl.properties;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractProperty<T> implements Property<T> {

    protected final List<PropertyListener<? super T>> listeners = new ArrayList<>();

    protected void fireListeners(T oldValue, T newValue) {
        for (PropertyListener<? super T> listener : listeners) {
            listener.propertyChanged(this, oldValue, newValue);
        }
    }

    @Override
    public void addListener(PropertyListener<? super T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(PropertyListener<? super T> listener) {
        listeners.remove(listener);
    }
}
