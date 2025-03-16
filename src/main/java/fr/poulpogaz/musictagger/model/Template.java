package fr.poulpogaz.musictagger.model;

import fr.poulpogaz.musictagger.properties.ObjectProperty;
import fr.poulpogaz.musictagger.properties.Property;

import java.util.*;

public class Template implements Iterable<Key> {

    private final ObjectProperty<String> name = new ObjectProperty<>(null, this);
    private final ObjectProperty<Formatter> formatter = new ObjectProperty<>(null, this);

    private final List<TemplateKeysListener> templateListeners = new ArrayList<>();
    private final List<Key> keys = new ArrayList<>();
    private final List<MetadataGenerator> generators = new ArrayList<>();

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
    }

    public Key getKey(String name) {
        int i = indexOfKey(name);
        return i == -1 ? null : keys.get(i);
    }

    public void removeAllKeys() {
        for (Key key : keys) {
            key.template = null;
        }

        this.keys.clear();
    }

    public void addAll(List<Key> keys) {
        Objects.requireNonNull(keys);

        int index = this.keys.size();
        for (Key k : keys) {
            addKey(k);
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

    public int indexOfKey(String name) {
        if (name != null) {
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i).getName().equals(name)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public String getKeyName(int index) {
        return keys.get(index).getName();
    }

    public String getKeyMetadataField(int index) {
        return keys.get(index).getMetadataField();
    }

    public List<Key> getKeys() {
        return Collections.unmodifiableList(keys);
    }

    public int keyCount() {
        return keys.size();
    }


    public void addTemplateKeyListListener(TemplateKeysListener listener) {
        templateListeners.add(listener);
    }

    public void removeTemplateKeyListListener(TemplateKeysListener listener) {
        templateListeners.remove(listener);
    }

    public void fireTemplateKeysEvent() {
        for (TemplateKeysListener listener : templateListeners) {
            listener.keysModified(this);
        }
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


    public void addMetadataGenerator(MetadataGenerator generator) {
        generators.add(generator);
    }

    public void removeMetadataGenerator(MetadataGenerator generator) {
        generators.remove(generator);
    }

    public List<MetadataGenerator> getGenerators() {
        return generators;
    }

    public static class MetadataGenerator {

        private String key;
        private final Formatter formatter = new Formatter();

        public MetadataGenerator() {
        }

        public MetadataGenerator(String key, String value) {
            this.key = key;
            formatter.setFormat(value);
        }

        public Formatter getFormatter() {
            return formatter;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return formatter.getFormat();
        }

        public void setValue(String value) {
            formatter.setFormat(value);
        }
    }
}
