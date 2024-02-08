package fr.poulpogaz.musicdb.model;

import java.util.*;

public class Template implements Iterable<Key> {

    private int id;
    private String name;
    private Formatter formatter;
    private final List<Key> keys = new ArrayList<>();

    public Template() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public String getFormat() {
        return formatter.getFormat();
    }

    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public void setFormat(String format) {
        if (formatter == null) {
            formatter = new Formatter(format);
        } else {
            formatter.setFormat(format);
        }
    }

    public boolean addKey(Key key) {
        if (key.getName() == null || key.getName().isEmpty() ||
                key.getTemplate() == this ||
                getKey(key.getName()) != null) {
            return false;
        }

        if (key.getTemplate() != null) {
            key.getTemplate().removeKey(key);
        }

        key.template = this;
        keys.add(key);

        return true;
    }

    public boolean removeKey(Key key) {
        if (key.getTemplate() != this) {
            return false;
        }

        boolean removed = keys.remove(key);
        if (removed) {
            key.template = null;
        }
        return false;
    }

    public Key getKey(String name) {
        if (name == null) {
            return null;
        }

        for (Key key : keys) {
            if (key.getName().equals(name)) {
                return key;
            }
        }

        return null;
    }

    public Key getKey(int index) {
        return keys.get(index);
    }

    public boolean containsKey(String name) {
        return getKey(name) != null;
    }

    public boolean containsKey(Key key) {
        return key.getTemplate() == this;
    }

    public String getKeyName(int index) {
        return keys.get(index).getName();
    }

    public void removeAllKeys() {
        for (Key key : keys) {
            key.template = null;
        }

        this.keys.clear();
    }

    public void setKeys(List<Key> keys) {
        Objects.requireNonNull(keys);

        removeAllKeys();
        for (Key k : keys) {
            addKey(k);
        }
    }

    public List<Key> getKeys() {
        return Collections.unmodifiableList(keys);
    }

    public int keyCount() {
        return keys.size();
    }

    @Override
    public Iterator<Key> iterator() {
        return keys.iterator();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Template template)) return false;

        return id == template.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
