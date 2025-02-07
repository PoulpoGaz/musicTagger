package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.musicdl.properties.ObjectProperty;
import fr.poulpogaz.musicdl.properties.Property;

import java.util.*;

public class Template implements Iterable<Key> {

    private final ObjectProperty<String> name = new ObjectProperty<>(null, this);
    private final ObjectProperty<Formatter> formatter = new ObjectProperty<>(null, this);

    private final List<TemplateKeyListListener> templateListeners = new ArrayList<>();
    private final List<Key> keys = new ArrayList<>();

    private final TemplateData data = new TemplateData(this);

    public Template() {

    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public Property<String> nameProperty() {
        return name;
    }


    public Formatter getFormatter() {
        return formatter.get();
    }

    public String getFormat() {
        return formatter.map(Formatter::getFormat);
    }

    public void setFormatter(Formatter formatter) {
        this.formatter.set(formatter);
    }

    public void setFormat(String format) {
        Formatter f = formatter.get();

        if (f == null) {
            formatter.set(new Formatter(format));
        } else {
            f.setFormat(format);
        }
    }

    public Property<Formatter> formatterProperty() {
        return formatter;
    }

    public boolean addKey(Key key) {
        return addKey(keys.size(), key);
    }

    public boolean addKey(int index, Key key) {
        if (key.getName() == null || key.getName().isEmpty() ||
                key.getTemplate() == this ||
                getKey(key.getName()) != null) {
            return false;
        }

        if (key.getTemplate() != null) {
            key.getTemplate().removeKey(key);
        }

        key.template = this;
        keys.add(index, key);
        fireEvent(TemplateKeyListListener.KEYS_ADDED, index, index);

        return true;
    }

    public boolean removeKey(int index) {
        if (index < 0 || index >= keys.size()) {
            return false;
        }

        Key key = keys.get(index);
        if (key.getTemplate() != this) {
            return false;
        }

        boolean removed = keys.remove(key);
        if (removed) {
            key.template = null;
            fireEvent(TemplateKeyListListener.KEYS_REMOVED, index, index);
        }
        return false;
    }

    public boolean removeKey(Key key) {
        return removeKey(keys.indexOf(key));
    }

    public void swap(int index1, int index2) {
        if (index1 < 0 || index1 >= keys.size()
                || index2 < 0 || index2 >= keys.size()
                || index1 == index2) {
            return;
        }

        Collections.swap(keys, index1, index2);
        fireEvent(TemplateKeyListListener.KEYS_SWAPPED, index1, index2);
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

    public void removeAllKeys() {
        for (Key key : keys) {
            key.template = null;
        }

        int size = keys.size();
        this.keys.clear();
        fireEvent(TemplateKeyListListener.KEYS_REMOVED, 0, size);
    }

    public void addAll(List<Key> keys) {
        Objects.requireNonNull(keys);

        int index = this.keys.size();
        for (Key k : keys) {
            addKey(k);
        }

        if (!keys.isEmpty()) {
            fireEvent(TemplateKeyListListener.KEYS_ADDED, index, this.keys.size());
        }
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

    public String getKeyMetadata(int index) {
        return keys.get(index).getMetadataKey();
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


    public TemplateData getData() {
        return data;
    }


    public boolean isInternalTemplate() {
        return Templates.isNameInternal(name.get());
    }


    public void addTemplateKeyListListener(TemplateKeyListListener listener) {
        templateListeners.add(listener);
    }

    public void removeTemplateKeyListListener(TemplateKeyListListener listener) {
        templateListeners.remove(listener);
    }

    private void fireEvent(int type, int index1, int index2) {
        for (TemplateKeyListListener listener : templateListeners) {
            listener.keyListModified(type, index1, index2);
        }
    }
}
