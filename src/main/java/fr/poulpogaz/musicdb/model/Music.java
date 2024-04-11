package fr.poulpogaz.musicdb.model;

import fr.poulpogaz.musicdb.opus.MetadataPicture;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Music {

    private MultiValuedMap<String, String> metadata;
    private List<MetadataPicture> pictures;

    private Template template;
    private final Map<String, String> tags = new HashMap<>();
    private String downloadURL;
    private Path downloadPath;

    public Music() {

    }

    public Music(Template template) {
        this.template = template;
    }

    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new ArrayListValuedHashMap<>();
        }

        if (key.equals("METADATA_BLOCK_PICTURE")) {
            throw new IllegalArgumentException("To add a picture, please use #addPicture");
        } else if (key.equals("TEMPLATE")) {
            throw new IllegalArgumentException("Reserved metadata key");
        }

        metadata.put(key, value);
    }

    public void removeMetadata(String key, String value) {
        if (metadata != null) {
            metadata.removeMapping(key, value);
        }
    }

    public void addPicture(MetadataPicture picture) {
        if (pictures == null) {
            pictures = new ArrayList<>();
        }

        pictures.add(picture);
    }

    public void removePicture(MetadataPicture picture) {
        if (pictures != null) {
            pictures.remove(picture);
        }
    }

    public Path getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(Path downloadPath) {
        this.downloadPath = downloadPath;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getTag(String key) {
        return tags.get(key);
    }

    public String getTag(int key) {
        return tags.get(template.getKeyName(key));
    }

    public String putTag(String key, String value) {
        return tags.put(key, value);
    }

    public String putTag(int key, String value) {
        return tags.put(template.getKeyName(key), value);
    }

    public String removeTag(String key) {
        return tags.remove(key);
    }

    public String removeTag(int key) {
        return tags.remove(template.getKeyName(key));
    }
}
