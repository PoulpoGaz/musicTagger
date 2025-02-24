package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.musicdl.opus.OpusFile;

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

    public boolean isMetadataKeySet() {
        return metadataKey != null;
    }

    public String getMetadataKey() {
        if (metadataKey == null) {
            return OpusFile.sanitize(name);
        } else {
            return metadataKey;
        }
    }

    public void setMetadataKey(String metadataKey) {
        String key = OpusFile.sanitize(metadataKey);
        if (key != null) {
            this.metadataKey = key;
        }
    }

    public boolean isMetadataKeyValid(String metadataKey) {
        return OpusFile.isValidKey(metadataKey);
    }


    public Template getTemplate() {
        return template;
    }
}
