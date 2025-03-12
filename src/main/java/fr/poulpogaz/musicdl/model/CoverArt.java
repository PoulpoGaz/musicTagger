package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.musicdl.opus.CoverType;
import fr.poulpogaz.musicdl.utils.LazyImage;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CoverArt implements LazyImage {

    private final LazyImage image;
    private int width;
    private int height;
    private int colorDepth;
    private int colorCount;

    protected CoverType type;
    protected String mimeType;
    protected String description;

    public CoverArt(LazyImage image) {
        this.image = image;
    }

    @Override
    public BufferedImage getImageNow() {
        return image.getImageNow();
    }

    @Override
    public BufferedImage getImageLater(BiConsumer<BufferedImage, Throwable> callback, Executor executor) {
        return image.getImageLater(callback, executor);
    }

    @Override
    public LazyImage transformAsync(Function<BufferedImage, BufferedImage> transform) {
        return image.transformAsync(transform);
    }

    @Override
    public BufferedImage getImage() throws InterruptedException {
        return image.getImage();
    }

    @Override
    public Exception getException() {
        return image.getException();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        BufferedImage img = getImageNow();
        return img == null ? width : img.getWidth();
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        BufferedImage img = getImageNow();
        return img == null ? height : img.getHeight();
    }

    public void setColorDepth(int colorDepth) {
        this.colorDepth = colorDepth;
    }

    public int getColorDepth() {
        BufferedImage img = getImageNow();
        return img == null ? colorDepth : img.getColorModel().getPixelSize();
    }

    public void setColorCount(int colorCount) {
        this.colorCount = colorCount;
    }

    public int getColorCount() {
        BufferedImage img = getImageNow();

        if (img == null) {
            return colorCount;
        } else {
            if (img.getColorModel() instanceof IndexColorModel model) {
                return model.getMapSize();
            } else {
                return  0;
            }
        }
    }

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
}
