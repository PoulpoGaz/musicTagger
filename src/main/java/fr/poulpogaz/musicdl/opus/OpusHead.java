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
    private int channelMappingFamily;
    private int streamCount;
    private int twoChannelStreamCount;
    private byte[] channelMapping;

    OpusHead(OggPage page) throws IOException {
        this.page = page;

        page.assertBytes(MAGIC_HEADER_BYTES);
        this.version = page.getByte(8);
        majorVersion = version >> 4;
        minorVersion = version & 0xF;

        if (majorVersion != 0) {
            throw new IOException("Unsupported Opus version: " + version);
        }

        channelCount = page.getUByte(9);
        preSkip = page.getUShort(10);
        rate = page.getUInt(12);
        outputGain = page.getShort(16);

        channelMappingFamily = page.getUByte(18);
        if (channelMappingFamily != 0) {
            streamCount = page.getUByte(19);
            twoChannelStreamCount = page.getUByte(20);
            channelMapping = new byte[channelCount];
            assertEndOfPage(page, 20 + channelCount);
            System.arraycopy(page.getData(), 21, channelMapping, 0, channelCount);
        } else {
            assertEndOfPage(page, 19);
        }
    }

    private void assertEndOfPage(OggPage page, int pos) throws IOException {
        int size = page.getPacketSize();
        if (pos < size) {
            throw new IOException("Error while reading OpusHead: too many bytes: " + pos + " < " + size);
        } else if (pos > size) {
            throw new IOException("Error while reading OpusHead: Not enough bytes: " + pos + " > " + size);
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

    public int getChannelMappingFamily() {
        return channelMappingFamily;
    }

    public double computeStreamLength(OggPage lastPage) {
        return (lastPage.getGranulePosition() - preSkip) / 48000.0;
    }

    public Channels getChannels() {
        return switch (channelCount) {
            case 1 -> Channels.MONO;
            case 2 -> Channels.STEREO;
            case 3 -> Channels.LINEAR_SURROUND;
            case 4 -> Channels.QUADRAPHONIC;
            case 5 -> Channels.SURROUND_5_0;
            case 6 -> Channels.SURROUND_5_1;
            case 7 -> Channels.SURROUND_6_1;
            case 8 -> Channels.SURROUND_7_1;
            default -> Channels.UNKNOWN;
        };
    }

    public int getStreamCount() {
        return streamCount;
    }

    public int getTwoChannelStreamCount() {
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
