package fr.poulpogaz.musicdl.opus;

import fr.poulpogaz.musicdl.BasicObjectPool;
import fr.poulpogaz.musicdl.Utils;
import fr.poulpogaz.musicdl.model.SoftCoverArt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class MetadataPicture {

    private static final Logger LOGGER = LogManager.getLogger(MetadataPicture.class);
    private static final MessageDigest SHA_256;

    static {
        try {
            SHA_256 = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static MetadataPicture readPicture(InputStream is) throws IOException {
        MetadataPicture pic = new MetadataPicture();

        int type = IOUtils.getIntB(is);
        pic.setType(CoverType.values()[type]);

        int mimeLength = IOUtils.getIntB(is);
        pic.setMimeType(IOUtils.readString(is, mimeLength));
        int descLength = IOUtils.getIntB(is);
        pic.setDescription(IOUtils.readString(is, descLength));
        pic.setWidth(IOUtils.getIntB(is));
        pic.setHeight(IOUtils.getIntB(is));
        pic.setColorDepth(IOUtils.getIntB(is));
        pic.setColorCount(IOUtils.getIntB(is));

        int dataLength = IOUtils.getIntB(is);
        pic.setData(is.readNBytes(dataLength));

        return pic;
    }

    private CoverType type;
    private String mimeType;
    private String description;
    private int width;
    private int height;
    private int colorDepth;
    private int colorCount;
    private byte[] data;

    public MetadataPicture() {

    }

    public BufferedImage createBufferedImage() {
        if (!mimeType.startsWith("image/")) {
            return null;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            LOGGER.debug("Failed to create buffered image from metadata picture", e);
            return null;
        }
    }

    public SoftCoverArt createSoftCoverArt(Path file, long pagePosition, long dataOffset) {
        LOGGER.debug("Creating soft cover art for {} at {} with an offset of {}", file, pagePosition, dataOffset);
        String sha256 = Utils.bytesToHex(SHA_256.digest(data));

        return new SoftCoverArt(sha256) {
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

    public CoverType getType() {
        return type;
    }

    public void setType(CoverType type) {
        this.type = type;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getColorDepth() {
        return colorDepth;
    }

    public void setColorDepth(int colorDepth) {
        this.colorDepth = colorDepth;
    }

    public int getColorCount() {
        return colorCount;
    }

    public void setColorCount(int colorCount) {
        this.colorCount = colorCount;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MetadataPicture{" +
                "type=" + type +
                "; mimeType='" + mimeType + '\'' +
                "; description='" + description + '\'' +
                "; width=" + width +
                "; height=" + height +
                "; colorDepth=" + colorDepth +
                "; colorCount=" + colorCount +
                "; dataLength=" + data.length +
                '}';
    }
}
