package fr.poulpogaz.musicdl.properties;

import java.util.Objects;
import java.util.function.Function;

public class ObjectProperty<T> extends AbstractProperty<T> {

    private T value;
    private Object owner;

    public ObjectProperty() {
        this(null);
    }

    public ObjectProperty(T value) {
        this.value = value;
    }

    public ObjectProperty(T value, Object owner) {
        this.value = value;
        this.owner = owner;
    }

    public void setOwner(Object owner) {
        this.owner = owner;
    }

    @Override
    public Object getOwner() {
        return owner;
    }

    @Override
    public T get() {
        return value;
    }

    public T getOrDefault(T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public <R> R map(Function<T, R> mapper) {
        if (value == null) {
            return null;
        } else {
            return mapper.apply(value);
        }
    }

    @Override
    public void set(T value) {
        if (!Objects.equals(value, this.value) && isValid(value)) {
            T old = this.value;
            this.value = value;
            fireListeners(old, value);
        }
    }

    public boolean isValid(T value) {
        return true;
    }
}
