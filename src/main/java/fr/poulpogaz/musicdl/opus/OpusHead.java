package fr.poulpogaz.musicdl.opus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class OpusHead {

    public static final String MAGIC_HEADER = "OpusHead";
    public static final byte[] MAGIC_HEADER_BYTES = MAGIC_HEADER.getBytes(StandardCharsets.UTF_8);

    private final OggPage page;

    private byte version;
    private int majorVersion;
    private int minorVersion;

    private int channelCount;
    private int preSkip;
    private long rate;
    private int outputGain;
    private byte channelMappingFamily;
    private byte streamCount;
    private byte twoChannelStreamCount;
    private byte[] channelMapping;

    OpusHead(OggPage page) throws IOException {
        this.page = page;

        byte[] data = page.getData();
        IOUtils.assertBytes(data, MAGIC_HEADER_BYTES);
        this.version = data[8];
        majorVersion = version >> 4;
        minorVersion = version & 0xF;

        if (majorVersion != 0) {
            throw new IOException("Unsupported Opus version: " + version);
        }

        channelCount = Byte.toUnsignedInt(data[9]);
        preSkip = Short.toUnsignedInt(IOUtils.getShort(data, 10, page));
        rate = Integer.toUnsignedLong(IOUtils.getInt(data, 12, page));
        outputGain = IOUtils.getShort(data, 16, page);

        channelMappingFamily = data[18];
        if (channelMappingFamily != 0) {
            streamCount = data[19];
            twoChannelStreamCount = data[20];
            channelMapping = new byte[channelCount];
            IOUtils.assertEndOfArray(data, 20 + channelCount, page);
            System.arraycopy(data, 21, channelMapping, 0, channelCount);
        } else {
            IOUtils.assertEndOfArray(data, 19, page);
        }
    }

    public OggPage getPage() {
        return page;
    }

    public byte getVersion() {
        return version;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public int getPreSkip() {
        return preSkip;
    }

    public long getRate() {
        return rate;
    }

    public int getOutputGain() {
        return outputGain;
    }

    public byte getChannelMappingFamily() {
        return channelMappingFamily;
    }

    public Channels getChannels() {
        return switch (channelCount) {
            case 1 -> Channels.MONO;
            case 2 -> Channels.STERO;
            case 3 -> Channels.LINEAR_SURROUND;
            case 4 -> Channels.QUADRAPHONIC;
            case 5 -> Channels.SURROUND_5_0;
            case 6 -> Channels.SURROUND_5_1;
            case 7 -> Channels.SURROUND_6_1;
            case 8 -> Channels.SURROUND_7_1;
            default -> Channels.UNKNOWN;
        };
    }

    public byte getStreamCount() {
        return streamCount;
    }

    public byte getTwoChannelStreamCount() {
        return twoChannelStreamCount;
    }

    public byte[] getChannelMapping() {
        return channelMapping;
    }

    @Override
    public String toString() {
        return "OpusHead{" +
                "page=" + page.getPageSequenceNumber() +
                ", version=" + version +
                ", majorVersion=" + majorVersion +
                ", minorVersion=" + minorVersion +
                ", channels=" + channelCount +
                ", preSkip=" + preSkip +
                ", rate=" + rate +
                ", outputGain=" + outputGain +
                ", channelMappingFamily=" + channelMappingFamily +
                ", streamCount=" + streamCount +
                ", twoChannelStreamCount=" + twoChannelStreamCount +
                ", channelMapping=" + Arrays.toString(channelMapping) +
                '}';
    }
}
