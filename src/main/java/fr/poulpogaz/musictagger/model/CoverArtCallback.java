package fr.poulpogaz.musictagger.model;

import java.awt.image.BufferedImage;

public interface CoverArtCallback {

    void onImageLoad(BufferedImage image, Exception error);
}