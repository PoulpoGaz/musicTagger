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

    public OggInputStream(FileChannel channel) {
        this.channel = channel;
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
            IOUtils.finishReadPage(nextPage, buffer);
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
            nextPage = new OggPage();
            IOUtils.readPageHeader(nextPage, buffer);
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

            OggPage page = new OggPage();
            IOUtils.readPageHeader(page, buffer);
            IOUtils.finishReadPage(page, buffer);

            return page;
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

    public static void main(String[] args) throws IOException {
        try (OggInputStream ois = new OggInputStream(Path.of("Fate · Ending [FakeIt · Hiroyuki Sawano].opus"))) {
            OggPage page;
            while ((page = ois.nextPage()) != null) {
                System.out.println(page);
            }
        }
    }
}
