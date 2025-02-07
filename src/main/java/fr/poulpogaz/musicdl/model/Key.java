package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.musicdl.properties.ObjectProperty;
import fr.poulpogaz.musicdl.properties.Property;

public class Key {

    private String name;
    private String metadataKey;

    Template template;

    public Key() {
    }

    public Key(String name) {
        if (!isNameValid(name)) {
            throw new IllegalArgumentException("Invalid name");
        }
        this.name = name;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (isNameValid(name)) {
            this.name = name;
        }
    }

    public boolean isNameValid(String name) {
        return name != null && !name.isEmpty() && (template == null || !template.containsKey(name));
    }


    public String getMetadataKey() {
        if (metadataKey == null) {
            return name;
        } else {
            return metadataKey;
        }
    }

    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }


    public Template getTemplate() {
        return template;
    }
}
