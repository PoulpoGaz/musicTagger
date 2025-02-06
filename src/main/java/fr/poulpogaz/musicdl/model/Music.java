package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.musicdl.opus.MetadataPicture;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Music {

    private final ListValuedMap<String, String> metadata = new ArrayListValuedHashMap<>();
    private final List<MetadataPicture> pictures = new ArrayList<>();
    Template template;

    private Path path; // location on disk
    private String downloadURL;

    public Music() {

    }

    public void addMetadata(String key, String value) {
        if (key.equals("METADATA_BLOCK_PICTURE")) {
            throw new IllegalArgumentException("To add a picture, please use #addPicture");
        } else if (key.equals("TEMPLATE")) {
            throw new IllegalArgumentException("Reserved metadata key");
        }

        metadata.put(key, value);
    }

    public void removeMetadata(String key, String value) {
        metadata.removeMapping(key, value);
    }


    public String getTag(String key) {
        List<String> l = metadata.get(key);

        return l.isEmpty() ? null : l.getFirst();
    }

    public String getTag(int key) {
        return getTag(template.getKeyName(key));
    }

    public void putTag(String key, String value) {
        metadata.put(key, value);
    }

    public void putTag(int key, String value) {
        putTag(template.getKeyName(key), value);
    }

    public void removeTag(String key) {
        metadata.remove(key);
    }

    public void removeTag(int key) {
        removeTag(template.getKeyName(key));
    }


    public void addPicture(MetadataPicture picture) {
        pictures.add(picture);
    }

    public void removePicture(MetadataPicture picture) {
        pictures.remove(picture);
    }

    public List<MetadataPicture> getPictures() {
        return pictures;
    }


    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Template getTemplate() {
        return template;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }
}
