package fr.poulpogaz.musicdb.opus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * An input stream that allows at most 'length' bytes to be read from 'is'.
 * If there are not enough bytes in the 'is' an exception is thrown
 */
public class LimitedInputStream extends InputStream {

    private final InputStream is;
    private long remaining;

    private Runnable closeAction;

    LimitedInputStream(InputStream is, long length) {
        this.is = is;
        this.remaining = length;

        if (length < 0) {
            throw new IllegalArgumentException("Negative length");
        }
    }

    @Override
    public int read() throws IOException {
        if (remaining > 0) {
            int b = is.read();

            if (b >= 0) {
                remaining--;
                return b;
            }

            throw new IOException("Not enough bytes in InputStream");
        } else {
            return -1;
        }
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
        } else {
            remaining -= read;
            return read;
        }
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        if (remaining >= Integer.MAX_VALUE) {
            throw new IOException("Can't allocate array of size " + remaining);
        }

        int toRead = (int) Math.min(len, remaining);
        byte[] array = is.readNBytes(toRead);

        if (array.length != toRead) {
            throw new IOException("Not enough bytes in InputStream");
        }
        remaining -= array.length;
        return array;
    }

    String readKey() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (;;) {
            int b = read();

            if (b < 0) {
                throw new IOException("Character not found");
            } else if (b == '=') {
                break;
            } else  if (b >= 'a' && b <= 'z') {
                baos.write(b - 'a' + 'A');
            } else if (b >= 0x20 && b <= 0x7D) {
                baos.write(b);
            } else {
                throw new IOException("Invalid character in key: " + b);
            }
        }

        return baos.toString(StandardCharsets.UTF_8);
    }

    public void close() throws IOException {
        is.skipNBytes(remaining);
        remaining = 0;
        if (closeAction != null) {
            closeAction.run();
            closeAction = null;
        }
    }

    void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public long remainingBytes() {
        return remaining;
    }

    void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }
}
