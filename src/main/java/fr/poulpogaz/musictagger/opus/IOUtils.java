package fr.poulpogaz.musictagger.opus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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

    public static void writeIntB(OutputStream os, int value) throws IOException {
        os.write((value >> 24) & 0xFF);
        os.write((value >> 16) & 0xFF);
        os.write((value >> 8) & 0xFF);
        os.write(value & 0xFF);
    }

    public static void writeIntL(OutputStream os, int value) throws IOException {
        os.write(value & 0xFF);
        os.write((value >> 8) & 0xFF);
        os.write((value >> 16) & 0xFF);
        os.write((value >> 24) & 0xFF);
    }

    public static void writeInt(byte[] array, int offset, int value) {
        array[offset] = (byte) (value & 0xFF);
        array[offset + 1] = (byte) ((value >> 8) & 0xFF);
        array[offset + 2] = (byte) ((value >> 16) & 0xFF);
        array[offset + 3] = (byte) ((value >> 24) & 0xFF);
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
                throw new IOException("Not enough bytes: expected " + length + " but got " + bytes.length);
            }

            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    public static void writeStringWithLength(OutputStream os, String str) throws IOException {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeIntB(os, bytes.length);
        os.write(bytes);
    }

    /**
     * Moves every byte after src to dest and reduce the file size by src - dest
     */
    public static void shrink(FileChannel fc, long src, long dest) throws IOException {
        if (dest > src) {
            return;
        }

        long size = fc.size();
        long count = size - src;
        ByteBuffer buff = ByteBuffer.allocate((int) Math.min(count, 8192));

        long transferred = 0;
        long srcPos = src;
        fc.position(dest);
        while (transferred < count) {
            buff.limit((int) Math.min(count - transferred, 8192));
            int read = fc.read(buff, srcPos);
            if (read == 0) {
                continue;
            }
            buff.flip();

            while (buff.hasRemaining()) {
                fc.write(buff);
            }
            transferred += read;
            srcPos += read;
            buff.clear();
        }

        fc.truncate(fc.size() - (src - dest));
    }

    /**
     * Moves every byte after src to dest and reduce the file size by src - dest
     */
    public static void grow(FileChannel fc, long src, long dest) throws IOException {
        if (dest < src) {
            return;
        }

        long size = fc.size();
        long count = size - src;
        long offset = dest - src;
        long newSize = size + offset;
        ByteBuffer buff = ByteBuffer.allocate((int) Math.min(count, 8192));

        long transferred = 0;
        long srcPos = size;
        long destPos = newSize;

        while (transferred < count) {
            buff.limit((int) Math.min(count - transferred, 8192));
            srcPos -= buff.limit();
            destPos -= buff.limit();

            int read = fc.read(buff, srcPos);
            if (read == 0) {
                continue;
            }
            buff.flip();

            while (buff.hasRemaining()) {
                fc.write(buff, destPos);
            }
            transferred += read;
            buff.clear();
        }
    }
}
