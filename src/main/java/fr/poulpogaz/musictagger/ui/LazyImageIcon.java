package fr.poulpogaz.musictagger.ui;

import fr.poulpogaz.musictagger.utils.LazyImage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LazyImageIcon implements Icon {

    private LazyImage image;
    private BufferedImage buffImg;

    public LazyImageIcon() {
    }

    public LazyImageIcon(LazyImage image) {
        this.image = image;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        tryGetImage();
        if (buffImg != null) {
            g.drawImage(buffImg, x, y, c);
        }
    }

    @Override
    public int getIconWidth() {
        tryGetImage();
        return buffImg == null ? 0 : buffImg.getWidth();
    }

    @Override
    public int getIconHeight() {
        tryGetImage();
        return buffImg == null ? 0 : buffImg.getHeight();
    }

    private void tryGetImage() {
        if (buffImg == null) {
            buffImg = image.getImageNow();
        }
    }

    public void setImage(LazyImage image) {
        this.image = image;
        if (image != null) {
            buffImg = image.getImageNow();
        } else {
            buffImg = null;
        }
    }

    public LazyImage getImage() {
        return image;
    }
}
