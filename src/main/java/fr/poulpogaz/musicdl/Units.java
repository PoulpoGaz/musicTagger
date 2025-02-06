package fr.poulpogaz.musicdl;

import java.text.DecimalFormat;

public class Units {

    private static final DecimalFormat format = new DecimalFormat("####.#");
    private static final String[] multipliers = new String[] {
        "", "Ki", "Mi", "Gi", "Ti", "Pi", "Ei", "Zi", "Yi"
    };

    public static String humanReadableBytes(double bytes) {
        int i = 0;
        double v = bytes;

        while (v >= 1024 && i < multipliers.length) {
            v /= 1024;
            i++;
        }

        return format.format(v) + multipliers[i] + "B";
    }

    public static String humanReadableSpeed(double bytesPerSecond) {
        return humanReadableBytes(bytesPerSecond) + "/s"; // don't show this to a physician
    }
}
