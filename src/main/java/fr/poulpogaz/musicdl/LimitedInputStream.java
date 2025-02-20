package fr.poulpogaz.musicdl;

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends InputStream {

    protected final InputStream is;
    protected long remaining;

    public LimitedInputStream(InputStream is, long length) {
        this.is = is;
        this.remaining = length;

        if (length < 0) {
            throw new IllegalArgumentException("Negative length");
        }
    }

    @Override
    public int read() throws IOException {
        if (remaining <= 0) {
            return -1;
        }

        int b = is.read();
        if (b < 0) {
            throw new IOException("Not enough bytes in InputStream");
        }

        remaining--;
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (remaining <= 0) {
            return -1;
        }

        int read;
        if (remaining >= Integer.MAX_VALUE) {
            read = is.read(b, off, len);
        } else {
            read = is.read(b, off, (int) Math.min(len, remaining));
        }

        if (read < 0) {
            throw new IOException("Not enough bytes in InputStream");
        }

        remaining -= read;
        return read;
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        if (len < 0) {
            throw new IOException("Negative length");
        }

        int toRead = (int) Math.min(len, remaining);
        byte[] array = is.readNBytes(toRead);

        if (array.length != toRead) {
            throw new IOException("Not enough bytes in InputStream");
        }
        remaining -= array.length;
        return array;
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

    public long remainingBytes() {
        return remaining;
    }
}
