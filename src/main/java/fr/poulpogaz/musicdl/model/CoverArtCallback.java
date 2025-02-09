package fr.poulpogaz.musicdl.model;

import java.awt.image.BufferedImage;

public interface CoverArtCallback {

    void onImageLoad(BufferedImage image, Exception error);
}