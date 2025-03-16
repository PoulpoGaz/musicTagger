package fr.poulpogaz.musictagger.utils;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface LazyImage {

    BufferedImage getImageNow();

    BufferedImage getImage() throws InterruptedException;

    // if the image is already loaded, immediately call the callback
    // either, asynchronously load the image and call the callback
    // in the same thread that was used to load the image
    default BufferedImage getImageLater(BiConsumer<BufferedImage, Throwable> callback) {
        return getImageLater(callback, null);
    }

    BufferedImage getImageLater(BiConsumer<BufferedImage, Throwable> callback, Executor executor);

    LazyImage transformAsync(Function<BufferedImage, BufferedImage> transform);

    Exception getException();
}
