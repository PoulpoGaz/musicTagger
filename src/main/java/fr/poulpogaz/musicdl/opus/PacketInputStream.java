package fr.poulpogaz.musicdl.opus;

import java.io.IOException;
import java.io.InputStream;

public class PacketInputStream extends InputStream {

    private final OggInputStream ois;
    private OggPage currentPage;
    private int position; // position inside currentPage

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
            int length = Math.min(remaining, data.length - position);
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
        while (n > 0) {
            if (readNextPageIfNeeded()) {
                int read = (int) Math.min(n, currentPage.getPacketSize() - position);
                n -= read;
                position += read;
            } else {
                throw new IOException("Unable to skip exactly");
            }
        }
    }

    /**
     * @return false if eof
     */
    private boolean readNextPageIfNeeded() throws IOException {
        if (currentPage == null || position == currentPage.getPacketSize()) {
            currentPage = ois.nextPage();
            position = 0;
        }

        return currentPage != null;
    }

    public int getPositionInPage() {
        return position;
    }
}
