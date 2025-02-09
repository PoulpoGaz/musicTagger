package fr.poulpogaz.musicdl;

import java.text.DecimalFormat;

public class Utils {

    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

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

    public static String prettyPrintBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return format.format(bytes / 1024d) + " KiB";
        } else if (bytes < 1024 * 1024 * 1024L) {
            return format.format(bytes / (1024 * 1024d)) + " MiB";
        } else {
            return format.format(bytes / (1024 * 1024 * 1024d)) + " GiB";
        }
    }

    public static String prettyPrintSeconds(int length) {
        if (length < 60) {
            return length + " s";
        } else if (length < 60 * 60) {
            return (length / 60) + " min " + (length % 60) + " s";
        } else {
            int hours = length / (60 * 60);
            int remaining = length - hours * 60 * 60;
            return hours + " h " + remaining / 60 + " min " + (remaining % 60) + " s";
        }
    }

    public static boolean equals(double a, double b) {
        return equals(a, b, 1e-3);
    }

    public static boolean equals(double a, double b, double epsilon) {
        return Math.abs(a - b) <= epsilon;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            chars[2 * i] = HEX_CHARS[(bytes[i] & 0xF) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[bytes[i] & 0xF];
        }

        return new String(chars);
    }
}
