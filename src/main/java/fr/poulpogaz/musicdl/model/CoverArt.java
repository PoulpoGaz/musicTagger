package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.musicdl.opus.CoverType;

import java.awt.image.BufferedImage;

public abstract class CoverArt {

    protected CoverType type;
    protected String mimeType;
    protected String description;

    public abstract BufferedImage getImageNow();

    public abstract BufferedImage getImageLater(CoverArtCallback callback, ExecutionStrategy strategy);

    public abstract BufferedImage waitImage() throws InterruptedException;

    public abstract Exception getException();

    public CoverType getType() {
        return type;
    }

    public void setType(CoverType type) {
        this.type = type;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract int getColorDepth();

    public abstract int getColorCount();
}
