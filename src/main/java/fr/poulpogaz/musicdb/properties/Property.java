package fr.poulpogaz.musicdb.properties;

public interface Property<T> {

    T get();

    void set(T value);

    void addListener(PropertyListener<? super T> listener);

    void removeListener(PropertyListener<? super T> listener);
}
