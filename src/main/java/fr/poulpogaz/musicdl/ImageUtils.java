package fr.poulpogaz.musicdl;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {

    public static Image scale(BufferedImage img, int availableWidth, int availableHeight, Zoom zoom) {
        return scale(img, zoom.getScaleFactor(img, availableWidth, availableHeight));
    }

    public static Image scale(BufferedImage img, double scaleFactor) {
        return img.getScaledInstance((int) Math.round(img.getWidth() * scaleFactor),
                                     (int) Math.round(img.getHeight() * scaleFactor),
                                     Image.SCALE_SMOOTH);
    }
}
