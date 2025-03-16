package fr.poulpogaz.musictagger.utils;

import java.awt.image.BufferedImage;

public abstract class StrongLazyImage extends AbstractLazyImage {

    private BufferedImage image;

    @Override
    protected void onImageLoad(BufferedImage img) {
        image = img;
    }

    @Override
    public BufferedImage getImageNow() {
        return image;
    }
}
