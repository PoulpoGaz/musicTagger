package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.musicdl.ExecutorWithException;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.*;
import java.util.concurrent.*;

public abstract class SoftCoverArt extends CoverArt {

    private static final Logger LOGGER = LogManager.getLogger(SoftCoverArt.class);

    private static final Map<String, BufferedImage> cache = Collections.synchronizedMap(new ReferenceMap<>());
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) ExecutorWithException.newFixedThreadPool(1);

    static {
        executor.setKeepAliveTime(500, TimeUnit.MILLISECONDS);
        executor.allowCoreThreadTimeOut(true);
    }


    private final String hash;

    private int width;
    private int height;
    private int colorDepth;
    private int colorCount;

    private final Object futureLock = new Object();
    private Future<?> future;
    private ListValuedMap<ExecutionStrategy, CoverArtCallback> callbacks;
    private Exception exception;

    public SoftCoverArt(String hash) {
        this.hash = Objects.requireNonNull(hash);
    }

    @Override
    public BufferedImage getImageNow() {
        return cache.get(hash);
    }

    @Override
    public BufferedImage getImageLater(CoverArtCallback callback, ExecutionStrategy strategy) {
        BufferedImage image;
        synchronized (futureLock) {
            image = getImageNow();

            if (image == null && exception == null) {
                if (future == null) {
                    callbacks = new ArrayListValuedHashMap<>();
                    future = executor.submit(this::run);
                }
                callbacks.put(strategy, callback);
            }
        }

        if ((image != null || exception != null) && callback != null) {
            strategy.execute(List.of(callback), image, exception);
        }

        return image;
    }

    @Override
    public BufferedImage waitImage() {
        return null;
    }

    @Override
    public Exception getException() {
        return exception;
    }


    private void run() {
        BufferedImage image = null;
        try {
            image = loadImage();
            if (image == null) {
                LOGGER.warn("loadImage() returned null");
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to load covert art with hash={}", hash, e);
            exception = e;
        }

        if (image != null) {
            cache.put(hash, image);
            setFields(image);
        }

        ListValuedMap<ExecutionStrategy, CoverArtCallback> callbacks;
        synchronized (futureLock) {
            callbacks = this.callbacks;
            this.future = null;
            this.callbacks = null;
        }

        if (!callbacks.isEmpty()) {
            for (ExecutionStrategy s : callbacks.keySet()) {
                s.execute(callbacks.get(s), image, exception);
            }
        }
    }

    private void setFields(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        colorDepth = image.getColorModel().getPixelSize();

        if (image.getColorModel() instanceof IndexColorModel model) {
            colorCount = model.getMapSize();
        } else {
            colorCount = 0;
        }
    }

    public abstract BufferedImage loadImage() throws Exception;

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getColorDepth() {
        return colorDepth;
    }

    public void setColorDepth(int colorDepth) {
        this.colorDepth = colorDepth;
    }

    @Override
    public int getColorCount() {
        return colorCount;
    }

    public void setColorCount(int colorCount) {
        this.colorCount = colorCount;
    }
}
