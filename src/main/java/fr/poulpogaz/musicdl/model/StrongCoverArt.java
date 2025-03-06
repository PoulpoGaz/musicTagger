package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.musicdl.opus.CoverType;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.List;

public class StrongCoverArt extends CoverArt {

    private BufferedImage image;

    private CoverType type;
    private String mimeType;
    private String description;

    public StrongCoverArt() {

    }

    public StrongCoverArt(BufferedImage image) {
        this.image = image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public BufferedImage getImageNow() {
        return image;
    }

    @Override
    public BufferedImage getImageLater(CoverArtCallback callback, ExecutionStrategy strategy) {
        BufferedImage image = this.image;
        if (callback != null) {
            strategy.execute(List.of(callback), image, null);
        }
        return image;
    }

    @Override
    public Exception getException() {
        return null;
    }

    @Override
    public BufferedImage waitImage() {
        return image;
    }

    public void setType(CoverType type) {
        this.type = type;
    }

    @Override
    public CoverType getType() {
        return type;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getWidth() {
        return image == null ? 0 : image.getWidth();
    }

    @Override
    public int getHeight() {
        return image == null ? 0 : image.getHeight();
    }

    @Override
    public int getColorDepth() {
        return image == null ? 0 : image.getColorModel().getPixelSize();
    }

    @Override
    public int getColorCount() {
        if (image.getColorModel() instanceof IndexColorModel model) {
            return model.getMapSize();
        } else {
            return 0;
        }
    }
}
