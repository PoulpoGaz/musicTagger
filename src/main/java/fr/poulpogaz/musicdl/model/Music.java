package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.musicdl.opus.Channels;
import fr.poulpogaz.musicdl.opus.MetadataPicture;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.functors.IfClosure;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Music {

    private final ListValuedMap<String, String> metadata = new ArrayListValuedHashMap<>();
    private final List<MetadataPicture> pictures = new ArrayList<>();
    Template template;

    private Path path; // location on disk
    private String downloadURL;

    private long size;
    private Channels channels;

    public Music() {

    }

    private String transform(String key) {
        return key.toUpperCase(Locale.ROOT);
    }

    private void checkKey(String key) {
        if (key.equals("METADATA_BLOCK_PICTURE")) {
            throw new IllegalArgumentException("To add a picture, please use #addPicture");
        } else if (key.equals("TEMPLATE")) {
            throw new IllegalArgumentException("Reserved metadata key");
        }

    }

    public void addMetadata(String key, String value) {
        key = transform(key);
        checkKey(key);
        metadata.put(key, value);
    }

    public void removeMetadata(String key, String value) {
        metadata.removeMapping(transform(key), value);
    }

    public void removeMetadata(String key) {
        metadata.remove(key);
    }

    public List<String> getMetadata(String key) {
        return metadata.get(transform(key));
    }

    public MapIterator<String, String> metadataIterator() {
        return metadata.mapIterator();
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


    public String getTag(int key) {
        if (template == null) {
            return null;
        } else {
            List<String> str = getMetadata(template.getKeyMetadata(key));

            return str.isEmpty() ? null : String.join("; ", str);
        }
    }

    public void putTag(int key, String value) {
        if (template != null) {
            String metadataKey = transform(template.getKeyMetadata(key));
            checkKey(metadataKey);
            List<String> values = metadata.get(metadataKey);
            values.clear();
            values.add(value);
        }
    }

    public void removeTag(int key) {
        if (template != null) {
            String metadataKey = transform(template.getKeyMetadata(key));
            removeMetadata(metadataKey);
        }
    }


    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public Channels getChannels() {
        return channels;
    }

    public void setChannels(Channels channels) {
        this.channels = channels;
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
