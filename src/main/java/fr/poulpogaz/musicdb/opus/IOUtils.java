package fr.poulpogaz.musicdb.opus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class IOUtils {

    public static void assertBytes(ByteBuffer buffer, byte[] expected) throws IOException {
        if (buffer.remaining() < expected.length) {
            throw new IOException("Not enough bytes");
        }

        for (byte b : expected) {
            if (buffer.get() != b) {
                throw new IOException("Invalid byte");
            }
        }
    }

    public static void assertByte(ByteBuffer buffer, byte expected) throws IOException {
        if (buffer.get() != expected) {
            throw new IOException("Invalid byte at position " + (buffer.position() - 1));
        }
    }



    public static void assertByte(byte value, byte expected) throws IOException {
        if (value != expected) {
            throw new IOException("Invalid byte");
        }
    }

    public static void assertBytes(byte[] bytes, byte[] expected) throws IOException {
        if (bytes.length < expected.length) {
            throw new IOException("Not enough bytes");
        }

        for (int i = 0; i < expected.length; i++) {
            if (bytes[i] != expected[i]) {
                throw new IOException("Invalid byte");
            }
        }
    }

    public static void assertEndOfArray(byte[] bytes, int pos, OggPage page) throws IOException {
        if (pos < bytes.length) {
            throwError("Too many bytes: " + pos + " < " + bytes.length, page);
        } else if (pos > bytes.length) {
            throwError("Not enough bytes: " + pos + " > " + bytes.length, page);
        }
    }

    public static byte getByte(byte[] bytes, int pos, OggPage page) throws IOException {
        if (pos < 0 || pos >= bytes.length) {
            throwError("Cannot read byte at position: " + pos, page);
        }

        return bytes[pos];
    }

    public static short getShort(byte[] bytes, int pos, OggPage page) throws IOException {
        if (pos < 0 || pos + 1 >= bytes.length) {
            throwError("Cannot read short at position: " + pos, page);
        }

        return (short) (bytes[pos] | bytes[pos + 1] << 8);
    }

    public static int getInt(byte[] bytes, int pos, OggPage page) throws IOException {
        if (pos < 0 || pos + 3 >= bytes.length) {
            throwError("Cannot read int at position: " + pos, page);
        }

        return bytes[pos] | bytes[pos + 1] << 8 | bytes[pos + 2] << 16 | bytes[pos + 3] << 24;
    }


    public static int getInt(InputStream is) throws IOException {
        int b1 = is.read();
        int b2 = is.read();
        int b3 = is.read();
        int b4 = is.read();

        if (b4 < 0) {
            throw new IOException("Cannot read int from InputStream: not enough data");
        }

        return (b1 & 0xFF) | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24;
    }

    public static int getIntB(InputStream is) throws IOException {
        int b1 = is.read();
        int b2 = is.read();
        int b3 = is.read();
        int b4 = is.read();

        if (b4 < 0) {
            throw new IOException("Cannot read int from InputStream: not enough data");
        }

        return (b4 & 0xFF) | (b3 & 0xFF) << 8 | (b2 & 0xFF) << 16 | (b1 & 0xFF) << 24;
    }

    private static void throwError(String error, OggPage page) throws IOException {
        throw new IOException("Error while reading Ogg page (page number=" + page.getPageSequenceNumber() + "): " + error);
    }


    public static String readString(InputStream is, long length) throws IOException {
        if (length >= Integer.MAX_VALUE) {
            throw new IOException("String too long");
        } else if (length < 0) {
            throw new IOException("Negative length");
        } else if (length == 0) {
            return "";
        } else {
            byte[] bytes = is.readNBytes((int) length);

            if (bytes.length != length) {
                throw new IOException("Not enough bytes");
            }

            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
