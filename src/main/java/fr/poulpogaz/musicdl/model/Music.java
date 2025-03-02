package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.utils.Pair;
import fr.poulpogaz.musicdl.ArrayListValuedLinkedMap;
import fr.poulpogaz.musicdl.opus.*;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public class Music {

    private static final Logger LOGGER = LogManager.getLogger(Music.class);

    public static Pair<Music, String> load(IJsonReader jr) throws IOException, JsonException {
        Music m = new Music();
        String template = null;

        List<String> base64Covers = null;
        while (!jr.isObjectEnd()) {
            String key = jr.nextKey();

            switch (key) {
                case "template" -> template = jr.nextString();
                case "url" -> m.setDownloadURL(jr.nextString());
                case "metadata" -> {
                    jr.beginObject();
                    while (!jr.isObjectEnd()) {
                        String metadataKey = jr.nextKey();
                        jr.beginArray();
                        while (!jr.isArrayEnd()) {
                            m.addMetadata(metadataKey, jr.nextString());
                        }
                        jr.endArray();
                    }
                    jr.endObject();
                }
                case "base64covers" -> {
                    base64Covers = new ArrayList<>();
                    jr.beginArray();
                    while (!jr.isArrayEnd()) {
                        base64Covers.add(jr.nextString());
                    }
                    jr.endArray();
                }
                case "covers" -> {
                    if (base64Covers != null) {
                        base64Covers.clear();
                        base64Covers = null;
                    }

                    jr.beginArray();
                    m.addCoverArt(SoftCoverArt.createFromFile(Path.of(jr.nextString())));
                    jr.endArray();
                }
                default -> {
                    LOGGER.warn("Unsupported key: {}", key);
                    jr.skipValue();
                }
            }
        }

        if (base64Covers != null) {
            for (String base64 : base64Covers) {
                byte[] bytes = base64.getBytes();
                InputStream is = Base64.getDecoder().wrap(new ByteArrayInputStream(bytes));

                m.addCoverArt(new StrongCoverArt(ImageIO.read(is)));
            }
        }

        return new Pair<>(m, template);
    }




    private OpusFile file;

    private final ListValuedMap<String, String> metadata = new ArrayListValuedLinkedMap<>();
    private final List<CoverArt> covers = new ArrayList<>();
    Template template;
    int index = -1;

    private String downloadURL;
    private boolean downloading;

    public Music() {

    }

    public Music(OpusFile file) {
        this.file = file;

        setDownloadURL(getOriginalDownloadURL());
        covers.addAll(file.getCoverArts());
        metadata.putAll(file.getMetadata());
    }

    public void set(OpusFile file) {
        this.file = file;
        metadata.clear();
        metadata.putAll(file.getMetadata());

        covers.clear();
        covers.addAll(file.getCoverArts());
    }

    public OpusFile getOpusFile() {
        return file;
    }


    public void writeTo(IJsonWriter jw, boolean imageToBase64, Path coverArtDest)
            throws JsonException, IOException, InterruptedException {
        jw.beginObject();

        if (template != null) {
            jw.field("template", template.getName());
        } else {
            jw.nullField("template");
        }

        if (downloadURL != null) {
            jw.field("url", downloadURL);
        } else {
            jw.nullField("url");
        }

        // write metadata
        jw.key("metadata").beginObject();
        for (String key : metadata.keySet()) {
            List<String> values = metadata.get(key);

            jw.key(key).beginArray();
            for (String val : values) {
                jw.value(val);
            }
            jw.endArray();
        }
        jw.endObject();

        if (!covers.isEmpty()) {
            if (imageToBase64) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                jw.key("base64covers").beginArray();
                for (CoverArt cover : covers) {
                    BufferedImage img = cover.waitImage();
                    ImageIO.write(img, "png", baos);

                    String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                    jw.value(base64);
                }

                jw.endArray();
            }

            if (coverArtDest != null) {
                String fileName = coverArtDest.getFileName().toString();

                if (fileName.endsWith(".png")) {
                    fileName = fileName.substring(0, fileName.length() - 4);
                }

                jw.key("covers").beginArray();
                for (int i = 0; i < covers.size(); i++) {
                    CoverArt cover = covers.get(i);
                    BufferedImage img = cover.waitImage();

                    String name = fileName + (covers.size() == 1 ? "" : i) + ".png";
                    Path dest = coverArtDest.resolveSibling(name);
                    ImageIO.write(img, "png", dest.toFile());
                    jw.value(dest.toString());
                }
                jw.endArray();
            }
        }

        jw.endObject();
    }




    private void checkKey(String key) {
        Objects.requireNonNull(key);
        if (key.equals("METADATA_BLOCK_PICTURE")) {
            throw new IllegalArgumentException("To add a picture, please use #addCoverArt");
        } else if (key.equals("TEMPLATE")) {
            throw new IllegalArgumentException("Reserved metadata key");
        }
    }

    public void addMetadata(String key, String value) {
        key = OpusFile.sanitize(key);
        checkKey(key);
        metadata.put(key, value);
    }

    public void putMetadata(String key, String value) {
        if (value == null) {
            removeMetadata(key);
        } else {
            List<String> values = metadata.get(key);
            values.clear();
            values.add(value);
        }
    }

    public void putMetadata(int key, String value) {
        if (template != null) {
            putMetadata(template.getKeyMetadataField(key), value);
        }
    }

    public void removeMetadata(String key, String value) {
        metadata.removeMapping(OpusFile.sanitize(key), value);
    }

    public void removeMetadata(String key) {
        metadata.remove(OpusFile.sanitize(key));
    }

    public void removeMetadata(int key) {
        if (template != null) {
            metadata.remove(template.getKeyMetadataField(key));
        }
    }

    public List<String> getMetadata(String key) {
        key = OpusFile.sanitize(key);
        if (key == null) {
            return null;
        }
        return metadata.get(key);
    }

    public List<String> getMetadata(int key) {
        if (template == null) {
            return null;
        } else {
            return getMetadata(template.getKeyMetadataField(key));
        }
    }



    public List<String> getOriginalMetadata(int key) {
        if (file == null || template == null) {
            return null;
        } else {
            return file.get(template.getKeyMetadataField(key));
        }
    }

    public boolean metadataHasChanged(int key) {
        return template != null && !Objects.equals(getOriginalMetadata(key), getMetadata(key));
    }


    public MapIterator<String, String> metadataIterator() {
        return metadata.mapIterator();
    }


    public boolean hasMultipleValues(String key) {
        key = OpusFile.sanitize(key);

        if (key == null) {
            return false;
        } else if (key.equals("METADATA_COVER_ART")) {
            return covers.size() > 1;
        } else {
            return metadata.get(key).size() > 1;
        }
    }

    public boolean hasMultipleValues(int key) {
        return hasMultipleValues(template.getKeyMetadataField(key));
    }

    public boolean contains(String key) {
        key = OpusFile.sanitize(key);

        if (key == null) {
            return false;
        } else if (key.equals("METADATA_COVER_ART")) {
            return !covers.isEmpty();
        } else {
            return !metadata.get(key).isEmpty();
        }
    }



    public void addCoverArt(CoverArt cover) {
        if (cover != null) {
            covers.add(cover);
        }
    }

    public void removeCoverArt(CoverArt cover) {
        if (cover != null) {
            covers.remove(cover);
        }
    }

    public List<CoverArt> getCovers() {
        return covers;
    }



    public boolean hasChanged() {
        return file != null && (!file.getMetadata().equals(metadata)
                || !file.getCoverArts().equals(covers));
    }




    public Path getPath() {
        return file == null ? null : file.getFile();
    }

    public long getSize() {
        return file == null ? -1L : file.getSize();
    }

    public double getLength() {
        return file == null ? -1L : file.getLength();
    }

    public Channels getChannels() {
        return file == null ? Channels.UNKNOWN : file.getChannels();
    }


    public Template getTemplate() {
        return template;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public String getOriginalDownloadURL() {
        return file == null ? null : file.getFirst("PURL");
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public boolean isDownloading() {
        return downloading;
    }

    public boolean isDownloaded() {
        return file != null;
    }

    public void notifyChanges() {
        if (template != null) {
            template.getData().notifyChanges(this);
        }
    }
}
