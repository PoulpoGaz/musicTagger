package fr.poulpogaz.musicdl.properties;

public interface Property<T> {

    Object getOwner();

    T get();

    void set(T value);

    void addListener(PropertyListener<? super T> listener);

    void removeListener(PropertyListener<? super T> listener);
}
