package fr.poulpogaz.musicdl.opus;

import java.util.Arrays;

public class OggPage {

    public static final int MAX_SIZE = 65307; // in bytes
    public static final int MIN_HEADER_SIZE = 27; // in bytes
    public static final int MAX_HEADER_SIZE = 282; // in bytes, = 27 + 255
    public static final int MAX_PACKET_SIZE = 65025; // in bytes, = 255 * 255

    byte version;
    byte headerType;
    long granulePosition;
    int bitstreamSerialNumber;
    int pageSequenceNumber;
    int CRC;
    int[] oggSegments;

    int headerSize;
    int packetSize;

    private long position = -1;


    private byte[] data;

    public OggPage() {

    }


    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getHeaderType() {
        return headerType;
    }

    public void setHeaderType(byte headerType) {
        this.headerType = headerType;
    }

    public long getGranulePosition() {
        return granulePosition;
    }

    public void setGranulePosition(long granulePosition) {
        this.granulePosition = granulePosition;
    }

    public int getPageSequenceNumber() {
        return pageSequenceNumber;
    }

    public int getCRC() {
        return CRC;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getPageSize() {
        return headerSize + packetSize;
    }

    public boolean isFreshPacket() {
        return (headerType & 0x1) == 0;
    }

    public boolean isFirstPage() {
        return (headerType & 0x2) != 0;
    }

    public boolean isLastPage() {
        return (headerType & 0x4) != 0;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "OggPage{size=" + getPageSize()
                + "; header size=" + headerSize
                + "; packet size=" + packetSize
                + "; fresh packet=" + isFreshPacket()
                + "; first page=" + isFirstPage()
                + "; last page=" + isLastPage()
                + "; version=" + version
                + "; granule position=" + granulePosition
                + "; page sequence number=" + pageSequenceNumber
                + "; CRC=" + CRC
                + "; Ogg segments=" + Arrays.toString(oggSegments)
                + '}';
    }
}
