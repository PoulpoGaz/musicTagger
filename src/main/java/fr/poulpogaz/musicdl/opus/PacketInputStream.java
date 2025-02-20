package fr.poulpogaz.musicdl.opus;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class PacketInputStream extends InputStream {

    private final OggInputStream ois;
    private OggPage currentPage;
    private int position; // position inside currentPage

    private boolean readFirstPacket = false;

    public PacketInputStream(OggInputStream ois) {
        this.ois = ois;
    }

    @Override
    public int read() throws IOException {
        if (readNextPageIfNeeded()) {
            return currentPage.getData()[position++] & 0xFF;
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int pos = off;
        int remaining = len;

        boolean eof = false;
        while (remaining > 0) {
            eof = !readNextPageIfNeeded();

            if (eof) {
                break;
            }

            byte[] data = currentPage.getData();
            int length = Math.min(remaining, currentPage.getPacketSize() - position);
            System.arraycopy(data, position, b, pos, length);
            position += length;
            pos += length;
            remaining -= length;
        }

        if (eof && pos == off) {
            return -1;
        } else {
            return pos - off;
        }
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        long skipped = skip(n);

        if (skipped != n) {
            throw new EOFException();
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long toSkip = n;
        while (toSkip > 0) {
            if (readNextPageIfNeeded()) {
                int read = (int) Math.min(toSkip, currentPage.getPacketSize() - position);
                toSkip -= read;
                position += read;
            } else {
                break;
            }
        }

        return n - toSkip;
    }

    /**
     * @return false if eof
     */
    private boolean readNextPageIfNeeded() throws IOException {
        if (currentPage == null || position == currentPage.getPacketSize()) {
            currentPage = ois.peekNextPage(currentPage);

            // valid page if:
            // - fresh packet AND no packet has been read before
            // - not a fresh packet
            if (currentPage != null &&
                    (currentPage.isFreshPacket() && !readFirstPacket || !currentPage.isFreshPacket())) {
                currentPage = ois.nextPage(currentPage);
                position = 0;
                readFirstPacket = true;
            } else {
                currentPage = null;
            }
        }

        return currentPage != null;
    }

    public int getPositionInPage() {
        return position;
    }
}
