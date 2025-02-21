package fr.poulpogaz.musicdl.opus;

import fr.poulpogaz.musicdl.LimitedInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * An input stream that allows at most 'length' bytes to be read from 'is'.
 * If there are not enough bytes in the 'is' an exception is thrown
 */
class CommentInputStream extends LimitedInputStream {

    private Runnable closeAction;

    public CommentInputStream(InputStream is, long length) {
        super(is, length);
    }

    String readKey() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (;;) {
            int b = read();

            if (b < 0) {
                throw new IOException("Character not found");
            } else if (b == '=') {
                break;
            } else if (b >= 'a' && b <= 'z') {
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

    void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }
}
