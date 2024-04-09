package fr.poulpogaz.musicdb.opus;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class OggInputStream implements Closeable {

    // 131972 bytes
    // allow reading of at least 2 page consecutively.
    private static final int BUFFER_SIZE = Integer.highestOneBit(2 * OggPage.MAX_SIZE) << 1;

    private final FileChannel channel;
    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE)
                                                .limit(0)
                                                .order(ByteOrder.LITTLE_ENDIAN);

    private OggPage nextPage = null;
    private int nextHeaderType = Integer.MAX_VALUE;

    public OggInputStream(Path file) throws IOException {
        channel = FileChannel.open(file, StandardOpenOption.READ);
    }

    /**
     * Read the next page and perform CRC verification.
     */
    public OggPage nextPage() throws IOException {
        OggPage next;
        if (nextPage == null) {
            next = readPageFully();
        } else {
            // move buffer position to the beginning of the page
            // to ensure that the whole page is loaded
            buffer.position(buffer.position() - nextPage.getHeaderSize());
            if (ensureDataAvailable(nextPage.getPageSize())) {
                throw new IOException("Unterminated Ogg page");
            }

            // rollback position
            buffer.position(buffer.position() + nextPage.getHeaderSize());
            finishReadPage(nextPage);
            next = nextPage;
        }

        nextHeaderType = Integer.MAX_VALUE;
        nextPage = null;
        return next;
    }

    public boolean nextPageHasNewPacket() throws IOException {
        return (peekHeaderType() & 0x1) == 0;
    }

    public boolean nextPageIsBOS() throws IOException {
        return (peekHeaderType() & 0x2) != 0;
    }

    public boolean nextPageIsEOS() throws IOException {
        return (peekHeaderType() & 0x3) != 0;
    }

    /**
     * Read the header of the next Ogg page and return the page.
     * Data inside the page won't be read and CRC won't be verified.
     */
    public OggPage peekNextPage() throws IOException {
        if (nextPage == null) {
            if (ensureDataAvailable(OggPage.MAX_HEADER_SIZE)) {
                return null;
            }
            nextPage = readPageHeader();
        }

        return nextPage;
    }

    /**
     * Buffer position should be at the beginning of the next page.
     * The buffer position will be set to the end of next page's header.
     */
    private OggPage readPageFully() throws IOException {
        // check if header is already in buffer
        if (buffer.remaining() >= OggPage.MAX_HEADER_SIZE) {
            // in this case, use peek to get page size
            OggPage next = peekNextPage();

            if (next == null) {
                // no next page
                return null;
            } else {
                // and use next page
                return nextPage();
            }
        } else {
            if (ensureDataAvailable(OggPage.MAX_SIZE)) {
                return null;
            }

            OggPage page = readPageHeader();
            finishReadPage(page);
            return page;
        }
    }

    /**
     * Data should be in buffer.
     * Buffer position should be at the beginning of the next page.
     * The buffer position will be set to the end of next page's header.
     */
    private OggPage readPageHeader() throws IOException {
        OggPage page = new OggPage();
        IOUtils.assertByte(buffer, (byte) 'O');
        IOUtils.assertByte(buffer, (byte) 'g');
        IOUtils.assertByte(buffer, (byte) 'g');
        IOUtils.assertByte(buffer, (byte) 'S');

        page.version = buffer.get();
        page.headerType = buffer.get();
        page.granulePosition = buffer.getLong();
        page.bitstreamSerialNumber = buffer.getInt();
        page.pageSequenceNumber = buffer.getInt();
        page.CRC = buffer.getInt();

        int segments = Byte.toUnsignedInt(buffer.get());
        page.oggSegments = new int[segments];

        page.headerSize = 27 + page.oggSegments.length;
        page.packetSize = 0;
        for (int i = 0; i < page.oggSegments.length; i++) {
            page.oggSegments[i] = Byte.toUnsignedInt(buffer.get());
            page.packetSize += page.oggSegments[i];
        }

        return page;
    }

    /**
     * Data should be in buffer. Buffer position is at the start of data.
     * CRC is verified.
     * At the end of the method, buffer position is after the page.
     */
    private void finishReadPage(OggPage page) throws IOException {
        byte[] data = new byte[page.getPacketSize()];
        buffer.get(data);
        page.setData(data);

        int pageStart = buffer.position() - page.getPageSize();

        buffer.putInt(pageStart + 22, 0);
        int crc = CRC32.getCRC(buffer, pageStart, page.getPageSize());
        buffer.putInt(pageStart + 22, page.getCRC());

        if (crc != page.getCRC()) {
            throw new IOException(
                    "CRC verification failed. (expected: " + page.getCRC() + ", got: " + crc + ")");
        }
    }

    private int peekHeaderType() throws IOException {
        if (nextPage != null) {
            return nextPage.getHeaderType();
        } else if (nextHeaderType != Integer.MAX_VALUE) {
            return nextHeaderType;
        } else {
            ensureDataAvailable(6);

            nextHeaderType = buffer.get(buffer.position() + 5);
            return nextHeaderType;
        }
    }

    /**
     * Ensure that at least 'after' bytes are after the position of the buffer.
     * If there is not enough bytes after, then more bytes are read and bytes
     * before buffer position are discarded.
     */
    private boolean ensureDataAvailable(int after) throws IOException {
        if (buffer.remaining() < after) {
            buffer.compact();

            boolean eof = false;
            int read = 0;
            while (read < after) {
                int r = channel.read(buffer);

                if (r == -1) {
                    if (read == 0) {
                        eof = true;
                    }
                    break;
                }

                read += r;
            }

            buffer.flip();

            return eof;
        }

        return false;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
