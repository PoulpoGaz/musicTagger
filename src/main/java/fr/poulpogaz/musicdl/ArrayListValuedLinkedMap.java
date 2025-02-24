package fr.poulpogaz.musicdl;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.AbstractListValuedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ArrayListValuedLinkedMap<K, V> extends AbstractListValuedMap<K, V> {

    /**
     * The initial map capacity used when none specified in constructor.
     */
    private static final int DEFAULT_INITIAL_MAP_CAPACITY = 16;

    /**
     * The initial list capacity when using none specified in constructor.
     */
    private static final int DEFAULT_INITIAL_LIST_CAPACITY = 3;

    /**
     * The initial list capacity when creating a new value collection.
     */
    private final int initialListCapacity;

    /**
     * Creates an empty ArrayListValuedHashMap with the default initial
     * map capacity (16) and the default initial list capacity (3).
     */
    public ArrayListValuedLinkedMap() {
        this(DEFAULT_INITIAL_MAP_CAPACITY, DEFAULT_INITIAL_LIST_CAPACITY);
    }

    /**
     * Creates an empty ArrayListValuedHashMap with the default initial
     * map capacity (16) and the specified initial list capacity.
     *
     * @param initialListCapacity  the initial capacity used for value collections
     */
    public ArrayListValuedLinkedMap(final int initialListCapacity) {
        this(DEFAULT_INITIAL_MAP_CAPACITY, initialListCapacity);
    }

    /**
     * Creates an empty ArrayListValuedHashMap with the specified initial
     * map and list capacities.
     *
     * @param initialMapCapacity  the initial hashmap capacity
     * @param initialListCapacity  the initial capacity used for value collections
     */
    public ArrayListValuedLinkedMap(final int initialMapCapacity, final int initialListCapacity) {
        super(new LinkedHashMap<>(initialMapCapacity));
        this.initialListCapacity = initialListCapacity;
    }

    /**
     * Creates an ArrayListValuedHashMap copying all the mappings of the given map.
     *
     * @param map a <code>MultiValuedMap</code> to copy into this map
     */
    public ArrayListValuedLinkedMap(final MultiValuedMap<? extends K, ? extends V> map) {
        this(map.size(), DEFAULT_INITIAL_LIST_CAPACITY);
        super.putAll(map);
    }

    /**
     * Creates an ArrayListValuedHashMap copying all the mappings of the given map.
     *
     * @param map a <code>Map</code> to copy into this map
     */
    public ArrayListValuedLinkedMap(final Map<? extends K, ? extends V> map) {
        this(map.size(), DEFAULT_INITIAL_LIST_CAPACITY);
        super.putAll(map);
    }

    @Override
    protected ArrayList<V> createCollection() {
        return new ArrayList<>(initialListCapacity);
    }

    /**
     * Trims the capacity of all value collections to their current size.
     */
    public void trimToSize() {
        for (final Collection<V> coll : getMap().values()) {
            final ArrayList<V> list = (ArrayList<V>) coll;
            list.trimToSize();
        }
    }
}
