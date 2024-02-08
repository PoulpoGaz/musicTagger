package fr.poulpogaz.musicdb.model;

public class Key {

    private String name;
    private String metadataKey;

    Template template;

    public Key() {
    }

    public Key(String name) {
        if (!setName(name)) {
            throw new IllegalArgumentException("Invalid name");
        }
    }

    public String getName() {
        return name;
    }

    public boolean setName(String name) {
        if (name == null || name.isEmpty() || (template != null && template.containsKey(name))) {
            return false;
        }

        this.name = name;
        return true;
    }

    public String getMetadataKey() {
        return metadataKey;
    }

    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }

    public Template getTemplate() {
        return template;
    }
}
