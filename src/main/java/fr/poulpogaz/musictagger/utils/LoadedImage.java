package fr.poulpogaz.musictagger.utils;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class LoadedImage implements LazyImage {

    private final BufferedImage image;

    public LoadedImage(BufferedImage image) {
        this.image = Objects.requireNonNull(image);
    }

    @Override
    public BufferedImage getImageNow() {
        return image;
    }

    @Override
    public BufferedImage getImageLater(BiConsumer<BufferedImage, Throwable> callback, Executor executor) {
        if (callback != null) {
            if (executor == null) {
                callback.accept(image, null);
            } else {
                executor.execute(() -> callback.accept(image, null));
            }
        }
        return image;
    }

    @Override
    public LazyImage transformAsync(Function<BufferedImage, BufferedImage> transform) {
        return new LoadedImage(transform.apply(image));
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public Exception getException() {
        return null;
    }
}
