package fr.poulpogaz.musicdl.opus;

import java.nio.ByteBuffer;

// https://xiph.org/ogg/doc/framing.html
// http://www.ross.net/crc/download/crc_v3.txt
public class CRC32 {

    private static final int GENERATOR_POLYNOMIAL = 0x04C11DB7;

    private static final int[] CRC_TABLE = new int[256];

    static {
        int crc;
        for (int i = 0; i < 256; i++) {
            crc = i << 24;

            for (int j = 0; j < 8; j++) {
                if ((crc & 0x80000000) != 0) {
                    crc = ((crc << 1) ^ GENERATOR_POLYNOMIAL);
                } else {
                    crc <<= 1;
                }
            }

            CRC_TABLE[i] = crc;
        }
    }


    public static int getCRC(ByteBuffer buffer) {
        int crc = 0;
        while (buffer.hasRemaining()) {
            crc = crc << 8 ^ CRC_TABLE[ ((crc >>> 24) & 0xff) ^ (buffer.get() & 0xff) ];
        }

        return crc;
    }

    public static int getCRC(ByteBuffer buffer, int index, int length) {
        int crc = 0;
        int end = index + length;
        for (; index < end; index++) {
            crc = (crc << 8) ^ CRC_TABLE[ ((crc >>> 24) & 0xff) ^ (buffer.get(index) & 0xff) ];
        }

        return crc;
    }
}
