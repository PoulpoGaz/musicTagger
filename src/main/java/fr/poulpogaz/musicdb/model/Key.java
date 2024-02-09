package fr.poulpogaz.musicdb.model;

import fr.poulpogaz.musicdb.properties.ObjectProperty;
import fr.poulpogaz.musicdb.properties.Property;

public class Key {

    private final ObjectProperty<String> name = new ObjectProperty<>() {
        @Override
        public boolean isValid(String value) {
            return isNameValid(value);
        }
    };
    private final ObjectProperty<String> metadataKey = new ObjectProperty<>();

    Template template;

    public Key() {
    }

    public Key(String name) {
        if (!isNameValid(name)) {
            throw new IllegalArgumentException("Invalid name");
        }
        setName(name);
    }



    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public boolean isNameValid(String name) {
        return name != null && !name.isEmpty() && (template == null || !template.containsKey(name));
    }

    public Property<String> nameProperty() {
        return name;
    }


    public String getMetadataKey() {
        return metadataKey.get();
    }

    public void setMetadataKey(String metadataKey) {
        this.metadataKey.set(metadataKey);
    }

    public Property<String> metadataKeyProperty() {
        return metadataKey;
    }


    public Template getTemplate() {
        return template;
    }
}
