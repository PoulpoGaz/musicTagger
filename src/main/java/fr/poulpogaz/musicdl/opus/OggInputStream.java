package fr.poulpogaz.musicdl.opus;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class OggInputStream implements Closeable {

    // 131972 bytes
    // allow reading of at least 2 page consecutively.
    private static final int BUFFER_SIZE = Integer.highestOneBit(2 * OggPage.MAX_SIZE) << 1;

    private final FileChannel channel;
    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE)
                                                .limit(0)
                                                .order(ByteOrder.LITTLE_ENDIAN);

    private OggPage nextPage = null;
    private long lastPageSize = 0;
    private long currentPagePosition = -1;

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
        return nextPage(null);
    }

    public OggPage nextPage(OggPage page) throws IOException {
        OggPage next = peekNextPage(page);
        if (next == null) {
            return null;
        }

        // position is at page start
        if (ensureDataAvailable(next.getPageSize())) {
            throw new IOException("Unterminated Ogg page");
        }

        next.finishReadPage(buffer);

        lastPageSize = nextPage.getPageSize();
        nextPage = null;
        return next;
    }

    /**
     * Read the header of the next Ogg page and return the page.
     * Data inside the page won't be read and CRC won't be verified.
     */
    public OggPage peekNextPage() throws IOException {
        return peekNextPage(null);
    }

    public OggPage peekNextPage(OggPage dest) throws IOException {
        if (nextPage == null) {
            return doPeakNextPage(dest);
        } else if (dest == null) {
            return nextPage;
        } else if (nextPage != dest) {
            dest.copyHeaderFrom(nextPage);
            nextPage = dest;
            return dest;
        } else {
            return dest;
        }
    }

    private OggPage doPeakNextPage(OggPage dest) throws IOException {
        if (currentPagePosition < 0) {
            currentPagePosition = 0;
        } else {
            currentPagePosition += lastPageSize;
        }

        if (ensureDataAvailable(OggPage.MAX_HEADER_SIZE)) {
            if (buffer.remaining() == 0) {
                return null; // no more data,
            } else if (buffer.remaining() < OggPage.MIN_HEADER_SIZE) {
                throw new IOException("Not enough bytes to read page header");
            }
        }

        nextPage = Objects.requireNonNullElseGet(dest, OggPage::new);
        nextPage.readPageHeader(buffer);
        buffer.position(buffer.position() - nextPage.getHeaderSize());
        // buffer position is at page start

        return nextPage;
    }


    /**
     * Ensure that at least N bytes are remaining.
     * Otherwise, read bytes until at least N bytes are remaining or end of file is reached.
     * All bytes before buffer position are discarded.
     * In the case EOF is reached, true is returned and no exception is thrown if not enough bytes are remaining.
     *
     * @return true if EOF is reached.
     * @throws IOException if any I/O exception occurs.
     */
    private boolean ensureDataAvailable(int N) throws IOException {
        if (buffer.remaining() < N) {
            buffer.compact();

            boolean eof = false;
            while (buffer.position() < N) {
                int r = channel.read(buffer);

                if (r == -1) {
                    eof = true;
                    break;
                }
            }

            buffer.flip();

            return eof;
        }

        return false;
    }

    /**
     * Returns the position of the first byte of the current page.
     * The current page is the page that was returned by the last
     * call to {@link #peekNextPage()}, {@link #nextPage()} or
     * {@link #readLastPage(int)}
     *
     * @return position of the first byte of the current page.
     */
    public long currentPagePosition() {
        return currentPagePosition;
    }


    /**
     * @return skip all pages and read last page
     */
    public OggPage readLastPage(int bitstreamSerialNumber) throws IOException {
        // read one page
        long size = channel.size();
        long newPos = Math.max(size - OggPage.MAX_SIZE, 0);
        channel.position(newPos);
        buffer.limit(0);
        ensureDataAvailable((int) (size - newPos));

        // find last page
        int index = reverseFindInBuffer(OggPage.MAGIC_HEADER_BYTES);
        if (index < 0) {
            throw new IOException("Can't find last page");
        }

        // read last page
        buffer.position(index);
        OggPage page = nextPage();

        if (page.bitstreamSerialNumber == bitstreamSerialNumber && page.isLastPage()) {
            currentPagePosition = newPos;
            return page;
        }

        // need to read the whole file
        channel.position(0);
        buffer.limit(0);

        while ((page = nextPage()) != null) {
            if (page.bitstreamSerialNumber == bitstreamSerialNumber && page.isLastPage()) {
                return page;
            }
        }

        throw new IOException("Can't find last page with bitstream serial number: " + bitstreamSerialNumber);
    }

    private int reverseFindInBuffer(byte[] array) {
        for (int i = buffer.limit() - array.length - 1;
             i >= buffer.position(); i--) {

            boolean found = true;
            for (int j = 0; j < array.length; j++) {
                if (buffer.get(i + j) != array[j]) {
                    found = false;
                    break;
                }
            }

            if (found) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    public static void main(String[] args) throws IOException {
        long time = System.currentTimeMillis();
        try (OggInputStream ois = new OggInputStream(Path.of("Fate · Ending [FakeIt · Hiroyuki Sawano].opus"))) {
            OggPage page = null;
            while ((page = ois.nextPage(page)) != null) {
                System.out.println(page);
            }
        }

        long time2 = System.currentTimeMillis();
        System.out.println("Reading done in " + (time2 - time) + " ms");
    }
}
