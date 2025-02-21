package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.utils.Pair;
import fr.poulpogaz.musicdl.opus.*;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
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

    private final ListValuedMap<String, String> metadata = new ArrayListValuedHashMap<>();
    private final List<CoverArt> covers = new ArrayList<>();
    Template template;
    int index = -1;

    private Path path; // location on disk
    private String downloadURL;
    private boolean downloading;

    public Music() {

    }

    public Music(OpusFile file) {
        this.file = file;

        setDownloadURL(file.getFirst("PURL"));
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


    public void writeTo(IJsonWriter jw, boolean imageToBase64, Path coverArtDest)
            throws JsonException, IOException, InterruptedException {
        jw.beginObject();

        if (template != null) {
            jw.field("template", template.getName());
        } else {
            jw.nullField("template");
        }

        jw.field("url", downloadURL);

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






    private String transform(String key) {
        return key.toUpperCase(Locale.ROOT);
    }

    private void checkKey(String key) {
        if (key.equals("METADATA_BLOCK_PICTURE")) {
            throw new IllegalArgumentException("To add a picture, please use #addCoverArt");
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


    public void addCoverArt(CoverArt cover) {
        covers.add(cover);
    }

    public void removeCoverArt(CoverArt cover) {
        covers.remove(cover);
    }

    public List<CoverArt> getCovers() {
        return covers;
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
            if (value == null) {
                removeTag(key);
            } else {
                String metadataKey = transform(template.getKeyMetadata(key));
                checkKey(metadataKey);
                List<String> values = metadata.get(metadataKey);
                values.clear();
                values.add(value);
            }
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

    public long getSize() {
        return file.getSize();
    }

    public double getLength() {
        return file.getLength();
    }

    public Channels getChannels() {
        return file.getChannels();
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

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public boolean isDownloading() {
        return downloading;
    }

    public boolean isDownloaded() {
        return path != null;
    }

    public void notifyChanges() {
        template.getData().notifyChanges(this);
    }
}
