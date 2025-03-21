package fr.poulpogaz.musictagger.opus;

import fr.poulpogaz.musictagger.model.CoverArt;
import fr.poulpogaz.musictagger.utils.ArrayListValuedLinkedMap;
import fr.poulpogaz.musictagger.utils.LimitedInputStream;
import fr.poulpogaz.musictagger.utils.SoftLazyImage;
import fr.poulpogaz.musictagger.utils.Utils;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.UnmodifiableMultiValuedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class OpusFile {

    private static final int INVALID = -1;
    private static final int UPPERCASE = 0;
    private static final int LOWERCASE = 1;

    public static boolean isValidKey(String key) {
        return keyStatus(key) != INVALID;
    }

    public static String sanitize(String key) {
        int s = keyStatus(key);

        if (s == INVALID) {
            return null;
        } else if (s == UPPERCASE) {
            return key;
        } else {
            return key.toUpperCase(Locale.ROOT);
        }
    }

    public static String reduce(String key) {
        int status = keyStatus(key);

        if (status == UPPERCASE) {
            return key;
        } else if (status == LOWERCASE) {
            return key.toUpperCase(Locale.ROOT);
        } else {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < key.length(); i++) {
                char c = key.charAt(i);

                if (c >= 0x20 && c <= 0x7D) {
                    if (c >= 0x61 && c <= 0x7A) {
                        sb.append(Character.toUpperCase(c));
                    } else {
                        sb.append(c);
                    }
                }
            }

            return sb.toString();
        }
    }

    private static int keyStatus(String key) {
        if (key == null || key.isEmpty()) {
            return INVALID;
        }

        boolean lowercase = false;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);

            if (c < 0x20 || c > 0x7D) {
                return INVALID;
            } else if (c >= 0x61 && c <= 0x7A) {
                lowercase = true;
            }
        }

        return lowercase ? LOWERCASE : UPPERCASE;
    }



    private static final Logger LOGGER = LogManager.getLogger(OpusFile.class);

    private Path file;
    private long fileSize;

    private String vendor;
    private Channels channels;
    private double length;

    private final ListValuedMap<String, String> metadata = new ArrayListValuedLinkedMap<>();
    private final List<CoverArt> covers = new ArrayList<>();

    public OpusFile() {

    }

    public OpusFile(Path file) throws IOException {
        load(file);
    }

    public void load(Path file) throws IOException {
        this.file = file;
        clear();
        clearCoverArt();

        MetadataPicture pic = new MetadataPicture();

        fileSize = Files.size(file);
        try (OpusInputStream ois = new OpusInputStream(file)) {
            channels = ois.readOpusHead().getChannels();
            vendor = ois.readVendor();

            long n = ois.readCommentCount();
            int lim = (int) Math.min(n, 8192);
            for (int i = 0; i < lim; i++) {
                String key = ois.readKey();

                if (key.equals("METADATA_BLOCK_PICTURE")) {
                    long position = ois.currentPagePosition();
                    int offset = ois.positionInPage();

                    InputStream picIS = Base64.getDecoder().wrap(ois.valueInputStream());
                    pic.fromInputStream(picIS, false);

                    String sha256 = Utils.sha256(picIS, pic.getDataLength());
                    picIS.close();
                    CoverArt cover = new CoverArt(createSoftLazyImage(sha256, file, position, offset));
                    cover.setType(pic.getType());
                    cover.setDescription(pic.getDescription());
                    cover.setMimeType(pic.getMimeType());
                    addCoverArt(cover);
                } else {
                    put(key, ois.readValue());
                }
            }

            if (n != lim) {
                ois.skipComments();;
            }

            length = ois.fileLength();
        }
    }

    private SoftLazyImage createSoftLazyImage(String sha256, Path file, long pagePosition, long dataOffset) {
        LOGGER.debug("Creating SoftLazyImage for {} at {} with an offset of {}", file, pagePosition, dataOffset);

        return new SoftLazyImage(sha256) {
            @Override
            public BufferedImage loadImage() throws IOException {
                LOGGER.debug("Loading cover art from {} at {} with an offset of {}", file, pagePosition, dataOffset);
                try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
                    channel.position(pagePosition);

                    OggInputStream ois = new OggInputStream(channel);
                    PacketInputStream pis = new PacketInputStream(ois);
                    pis.skipNBytes(dataOffset);

                    InputStream is = Base64.getDecoder().wrap(pis);
                    is.skipNBytes(4); // skip type
                    is.skipNBytes(IOUtils.getIntB(is)); // skip mimeLength
                    is.skipNBytes(IOUtils.getIntB(is)); // skip description
                    is.skipNBytes(16); // skip width, height, color depth and color count
                    int length = IOUtils.getIntB(is);

                    return ImageIO.read(new LimitedInputStream(is, length));
                }
            }
        };
    }

    public void save() throws IOException, InterruptedException {
        save(file);
    }

    public void save(Path file) throws IOException, InterruptedException {
        Objects.requireNonNull(file);
        this.file = file;

        LOGGER.debug("Saving {}", file);
        OpusMetadataWriter omw = new OpusMetadataWriter(file);
        omw.setVendor(Objects.requireNonNullElse(vendor, "musicTagger"));

        MapIterator<String, String> it = metadata.mapIterator();
        while (it.hasNext()) {
            it.next();
            omw.addComment(it.getKey(), it.getValue());
        }

        for (CoverArt coverArt : covers) {
            BufferedImage img = coverArt.getImage();
            omw.addCoverArt(img, coverArt.getDescription(), coverArt.getType());
        }

        omw.write();
    }

    public Path getPath() {
        return file;
    }

    public long getSize() {
        return fileSize;
    }

    public Channels getChannels() {
        return channels;
    }

    public double getLength() {
        return length;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVendor() {
        return vendor;
    }

    public List<String> get(String key) {
        key = sanitize(key);
        return metadata.get(key);
    }

    public String getFirst(String key) {
        List<String> values = get(key);

        return values.isEmpty() ? null : values.getFirst();
    }

    public void put(String key, String value) {
        key = sanitize(key);
        if (key != null && value != null) {
            metadata.put(key, value);
        }
    }

    public void remove(String key, String value) {
        key = sanitize(key);
        if (key != null && value != null) {
            metadata.removeMapping(key, value);
        }
    }

    public List<String> removeAll(String key) {
        key = sanitize(key);
        if (key != null) {
            return metadata.remove(key);
        } else {
            return List.of();
        }
    }

    public MultiValuedMap<String, String> getMetadata() {
        return UnmodifiableMultiValuedMap.unmodifiableMultiValuedMap(metadata);
    }

    public void clear() {
        metadata.clear();
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

    public void clearCoverArt() {
        covers.clear();
    }

    public List<CoverArt> getCoverArts() {
        return Collections.unmodifiableList(covers);
    }
}
