package fr.poulpogaz.musicdb.model;

public class Key {

    private int id;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key key)) return false;

        return id == key.id;
    }
}
