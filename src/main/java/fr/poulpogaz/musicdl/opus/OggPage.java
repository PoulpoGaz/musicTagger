package fr.poulpogaz.musicdl.opus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class OggPage {

    public static final String MAGIC_HEADER = "OggS";
    public static final byte[] MAGIC_HEADER_BYTES = MAGIC_HEADER.getBytes(StandardCharsets.UTF_8);

    public static final int MIN_HEADER_SIZE = 27; // in bytes
    public static final int MAX_HEADER_SIZE = 282; // in bytes, = 27 + 255
    public static final int MAX_PACKET_SIZE = 65025; // in bytes, = 255 * 255
    public static final int MAX_SIZE = MAX_HEADER_SIZE + MAX_PACKET_SIZE; // in bytes

    byte version;
    byte headerType;
    long granulePosition;
    int bitstreamSerialNumber;
    int pageSequenceNumber;
    int CRC;
    int[] oggSegments;

    int headerSize;
    int packetSize;


    private byte[] data;

    public OggPage() {

    }

    /**
     * Data should be in buffer.
     * Buffer position should be at the beginning of the page
     * The buffer position will be set to the end of the page's header.
     * Buffer should at least contains {@link #MIN_HEADER_SIZE} bytes
     */
    public void readPageHeader(ByteBuffer buffer) throws IOException {
        if (buffer.remaining() < MIN_HEADER_SIZE) {
            throw new IOException("Not enough bytes to read page header");
        }

        IOUtils.assertBytes(buffer, MAGIC_HEADER_BYTES);

        version = buffer.get();
        headerType = buffer.get();
        granulePosition = buffer.getLong();
        bitstreamSerialNumber = buffer.getInt();
        pageSequenceNumber = buffer.getInt();
        CRC = buffer.getInt();

        int segments = Byte.toUnsignedInt(buffer.get());
        oggSegments = new int[segments];

        if (buffer.remaining() < oggSegments.length) {
            throw new IOException("Not enough bytes to read page header");
        }

        headerSize = 27 + oggSegments.length;
        packetSize = 0;
        for (int i = 0; i < oggSegments.length; i++) {
            oggSegments[i] = Byte.toUnsignedInt(buffer.get());
            packetSize += oggSegments[i];
        }
    }


    /**
     * Whole page should be in the buffer and the buffer position must be at the
     * beginning of the page to perform CRC check. Then, if successful the page content
     * is copied into "data" array. Finally, buffer position will be set just after the page.
     */
    public void finishReadPage(ByteBuffer buffer) throws IOException {
        buffer.putInt(buffer.position() + 22, 0);
        int crc = CRC32.getCRC(buffer, buffer.position(), getPageSize());
        buffer.putInt(buffer.position() + 22, getCRC());

        if (crc != getCRC()) {
            throwError("CRC verification failed. (expected: " + getCRC() + ", got: " + crc + ")");
        }

        buffer.position(buffer.position() + getHeaderSize());
        byte[] data = new byte[getPacketSize()];
        buffer.get(data);
        setData(data);
    }


    public void assertBytes(byte[] expected) throws IOException {
        if (data.length < expected.length) {
            throwError("Can't find expected bytes at start of OggPage: not enough bytes");
        }

        for (int i = 0; i < expected.length; i++) {
            if (data[i] != expected[i]) {
                throwError("Invalid byte at position " + i + ". (expected " + expected[i] + ", got: " + data[i] + ")");
            }
        }
    }


    public byte getByte(int pos) throws IOException {
        if (pos < 0 || pos >= data.length) {
            throwError("Cannot read byte at position: " + pos);
        }

        return data[pos];
    }

    public int getUByte(int pos) throws IOException {
        return Byte.toUnsignedInt(getByte(pos));
    }

    public short getShort(int pos) throws IOException {
        if (pos < 0 || pos + 1 >= data.length) {
            throwError("Cannot read short at position: " + pos);
        }

        return (short) (data[pos] | data[pos + 1] << 8);
    }

    public int getUShort(int pos) throws IOException {
        return Short.toUnsignedInt(getShort(pos));
    }

    public int getInt(int pos) throws IOException {
        if (pos < 0 || pos + 3 >= data.length) {
            throwError("Cannot read int at position: " + pos);
        }

        return data[pos] | data[pos + 1] << 8 | data[pos + 2] << 16 | data[pos + 3] << 24;
    }

    public long getUInt(int pos) throws IOException {
        return Integer.toUnsignedLong(getInt(pos));
    }

    private void throwError(String error) throws IOException {
        throw new IOException("Error while reading Ogg page (page number=" + pageSequenceNumber + "): " + error);
    }


    public byte getVersion() {
        return version;
    }

    public byte getHeaderType() {
        return headerType;
    }

    public long getGranulePosition() {
        return granulePosition;
    }

    public int getBitstreamSerialNumber() {
        return bitstreamSerialNumber;
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
                + "; bitstream serial number=" + bitstreamSerialNumber
                + "; page sequence number=" + pageSequenceNumber
                + "; CRC=" + CRC
                + "; Ogg segments=" + Arrays.toString(oggSegments)
                + '}';
    }
}
