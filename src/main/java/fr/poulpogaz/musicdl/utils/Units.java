package fr.poulpogaz.musicdl.utils;

import java.text.DecimalFormat;

public class Units {

    private static final DecimalFormat format = new DecimalFormat("####.##");
    private static final String[] bytesPrefixes = new String[] {
        "", "Ki", "Mi", "Gi", "Ti", "Pi", "Ei", "Zi", "Yi", "Ri", "Qi"
    };

    public static String humanReadableBytes(long bytes) {
        int i = 0;
        long v = bytes;

        while (v >= 1024 && i < bytesPrefixes.length) {
            v /= 1024;
            i++;
        }

        return format.format(v) + bytesPrefixes[i] + "B";
    }

    public static String humanReadableSpeed(long bytesPerSecond) {
        return humanReadableBytes(bytesPerSecond) + "/s"; // don't show this to a physician
    }

    public static String humanReadableSeconds(long seconds) {
        if (seconds < 60) {
            return seconds + " s";
        } else if (seconds < 60 * 60) {
            return (seconds / 60) + " min " + (seconds % 60) + " s";
        } else {
            long hours = seconds / (60 * 60);
            long remaining = seconds - hours * 60 * 60;
            return hours + " h " + remaining / 60 + " min " + (remaining % 60) + " s";
        }
    }
}
