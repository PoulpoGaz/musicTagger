package fr.poulpogaz.musicdl.utils;

import javax.swing.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.concurrent.Executor;

public class Utils {

    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    private static final Executor EVENT_QUEUE_EXECUTOR = SwingUtilities::invokeLater; // maybe use accumalativerunnable as demonstrated in SwingWorker


    public static final MessageDigest SHA_256;

    static {
        try {
            SHA_256 = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String escapeCharacter(String str, char quote, char escape) {
        return addQuote(str, quote, escape, false);
    }

    public static String addQuote(String str, char quote) {
        return addQuote(str, quote, quote, true);
    }

    public static String addQuote(String str, char quote, char escape, boolean outerQuote) {
        int quoteCount = count(str, quote);
        int len = str.length();

        char[] chars = new char[len + quoteCount + (outerQuote ? 2 : 0)];
        int j = 0;

        if (outerQuote) {
            chars[j++] = quote;
        }
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);

            if (c == quote) {
                chars[j++] = escape;
            }

            chars[j++] = c;
        }
        if (outerQuote) {
            chars[j] = quote;
        }

        return new String(chars);
    }

    public static int count(String str, char c) {
        int i = 0;
        int n = 0;

        while ((i = str.indexOf(c, i) + 1) > 0) {
            n++;
        }

        return n;
    }

    public static boolean equals(double a, double b) {
        return equals(a, b, 1e-3);
    }

    public static boolean equals(double a, double b, double epsilon) {
        return Math.abs(a - b) <= epsilon;
    }


    public static String sha256(InputStream is) throws IOException {
        return sha256(is, Long.MAX_VALUE);
    }

    public static String sha256(InputStream is, long limit) throws IOException {
        int c = (int) Math.min(limit, 8192);
        byte[] buff = new byte[c];

        long remaining = limit;
        while (remaining > 0) {
            int max = (int) Math.min(remaining, buff.length);
            int r = is.read(buff, 0, max);

            if (r == -1) {
                break;
            }

            SHA_256.update(buff, 0, r);

            remaining -= r;
        }

        return bytesToHex(SHA_256.digest());
    }

    public static String sha256(File file) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            return Utils.sha256(is);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            chars[2 * i] = HEX_CHARS[(bytes[i] & 0xF) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[bytes[i] & 0xF];
        }

        return new String(chars);
    }

    public static Executor eventQueueExecutor() {
        return EVENT_QUEUE_EXECUTOR;
    }
}
