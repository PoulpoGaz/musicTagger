package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.musicdl.opus.OpusFile;

public class Key {

    private String name;
    private String metadataField;

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

    public boolean isMetadataFieldSet() {
        return metadataField != null;
    }

    public String getMetadataField() {
        if (metadataField == null) {
            return OpusFile.sanitize(name);
        } else {
            return metadataField;
        }
    }

    public void setMetadataField(String metadataField) {
        String key = OpusFile.sanitize(metadataField);
        if (key != null) {
            this.metadataField = key;
        }
    }

    public boolean isMetadataKeyValid(String metadataKey) {
        return OpusFile.isValidKey(metadataKey);
    }


    public Template getTemplate() {
        return template;
    }
}
