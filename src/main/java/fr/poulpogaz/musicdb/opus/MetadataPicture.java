package fr.poulpogaz.musicdb.opus;

import java.io.IOException;
import java.io.InputStream;

public class MetadataPicture {

    public static MetadataPicture fromInputStream(InputStream is) throws IOException {
        MetadataPicture pic = new MetadataPicture();

        int type = IOUtils.getIntB(is);
        pic.setType(Type.values()[type]);

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

    private Type type;
    private String mimeType;
    private String description;
    private int width;
    private int height;
    private int colorDepth;
    private int colorCount;
    private byte[] data;

    public MetadataPicture() {

    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
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

    public enum Type {
        OTHER("Other"),
        PNG_32x32_ICON("32x32 pixels 'file icon' (PNG only)"),
        ICON("Other file icon"),
        COVER_FRONT("Cover (front)"),
        COVER_BACK("Cover (back)"),
        LEAFLET_PAGE("Leaflet page"),
        MEDIA("Media (e.g. label side of CD)"),
        LEAD_ARTIST("Lead artist/lead performer/soloist"),
        ARTIST("Artist/performer"),
        CONDUCTOR("Conductor"),
        BAND("Band/Orchestra"),
        COMPOSER("Composer"),
        LYRICIST("Lyricist/text writer"),
        RECORDING_LOCATION("Recording Location"),
        DURING_RECORDING("During recording"),
        DURING_PERFORMANCE("During performance"),
        MOVIE_VIDEO_SCREEN_CAPTURE("Movie/video screen capture"),
        A_BRIGHT_COLORED_FISH("A bright coloured fish"),
        ILLUSTRATION("Illustration"),
        BAND_LOGOTYPE("Band/artist logotype"),
        PUBLISHER_LOGOTYPE("Publisher/Studio logotype");

        private final String fullDescription;

        Type(String fullDescription) {
            this.fullDescription = fullDescription;
        }

        public String getFullDescription() {
            return fullDescription;
        }
    }
}
