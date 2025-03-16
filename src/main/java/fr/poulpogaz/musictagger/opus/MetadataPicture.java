package fr.poulpogaz.musictagger.opus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MetadataPicture {

    private static final Logger LOGGER = LogManager.getLogger(MetadataPicture.class);

    private CoverType type;
    private String mimeType;
    private String description;
    private int width;
    private int height;
    private int colorDepth;
    private int colorCount;
    private int dataLength;
    private byte[] data;

    public MetadataPicture() {

    }

    public void fromInputStream(InputStream is, boolean readData) throws IOException {
        int type = IOUtils.getIntB(is);
        setType(CoverType.values()[type]);

        int mimeLength = IOUtils.getIntB(is);
        setMimeType(IOUtils.readString(is, mimeLength));
        int descLength = IOUtils.getIntB(is);
        setDescription(IOUtils.readString(is, descLength));
        setWidth(IOUtils.getIntB(is));
        setHeight(IOUtils.getIntB(is));
        setColorDepth(IOUtils.getIntB(is));
        setColorCount(IOUtils.getIntB(is));

        dataLength = IOUtils.getIntB(is);
        if (readData) {
            setData(is.readNBytes(dataLength));
        }
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
        dataLength = data.length;
    }

    public int getDataLength() {
        return dataLength;
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
