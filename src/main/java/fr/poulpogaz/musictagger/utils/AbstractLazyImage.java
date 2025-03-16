package fr.poulpogaz.musictagger.utils;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AbstractLazyImage implements LazyImage {

    private static final ThreadPoolExecutor EXECUTOR = (ThreadPoolExecutor) ExecutorWithException.newFixedThreadPool(1);

    public static void shutdown() {
        EXECUTOR.shutdown();
    }



    private final Object lock = new Object();
    private CompletableFuture<BufferedImage> future;
    private Exception exception;

    @Override
    public BufferedImage getImageLater(BiConsumer<BufferedImage, Throwable> callback, Executor executor) {
        BufferedImage image;
        synchronized (lock) {
            image = getImageNow();

            if (image == null && exception == null) {
                if (future == null) {
                    future = CompletableFuture.supplyAsync(this::run, EXECUTOR);
                }

                if (callback != null) {
                    if (executor == null) {
                        future.whenComplete(callback);
                    } else {
                        future.whenCompleteAsync(callback, executor);
                    }
                }
            }
        }

        if ((image != null || exception != null) && callback != null) {
            if (executor == null) {
                callback.accept(image, exception);
            } else {
                executor.execute(() -> callback.accept(image, exception));
            }
        }

        return image;
    }

    private BufferedImage run() {
        try {
            BufferedImage img = loadImage();
            onImageLoad(img);
            return img;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            exception = e;
            throw new CompletionException(e);
        } finally {
            synchronized (lock) {
                future = null;
            }
        }
    }

    protected abstract BufferedImage loadImage() throws Exception;

    protected void onImageLoad(BufferedImage img) {

    }

    @Override
    public BufferedImage getImage() throws InterruptedException {
        CompletableFuture<BufferedImage> f;
        synchronized (lock) {
            BufferedImage img = getImageNow();
            if (img != null) {
                return img;
            }


            if (future == null) {
                getImageLater(null, null);
            }
            f = future;
        }

        if (f == null) {
            return null;
        }

        try {
            return f.get();
        } catch (ExecutionException e) {
            return null;
        }
    }

    @Override
    public LazyImage transformAsync(Function<BufferedImage, BufferedImage> transform) {
        return new TransformedLazyImage(this) {
            @Override
            protected BufferedImage transform(BufferedImage image) {
                return transform.apply(image);
            }
        };
    }

    @Override
    public Exception getException() {
        return exception;
    }

    private static abstract class TransformedLazyImage implements LazyImage {

        private final LazyImage origin;
        private BufferedImage image;

        public TransformedLazyImage(LazyImage origin) {
            this.origin = Objects.requireNonNull(origin);
        }

        @Override
        public BufferedImage getImageNow() {
            return image;
        }

        @Override
        public BufferedImage getImage() throws InterruptedException {
            image = transform(origin.getImage());
            return image;
        }

        @Override
        public BufferedImage getImageLater(BiConsumer<BufferedImage, Throwable> callback, Executor executor) {
            if (image == null) {
                origin.getImageLater((i, t) -> {
                    if (i != null) {
                        image = transform(i);
                    }
                    executor.execute(() -> callback.accept(image, t));
                }, EXECUTOR);
            }

            return image;
        }

        @Override
        public LazyImage transformAsync(Function<BufferedImage, BufferedImage> transform) {
            return new TransformedLazyImage(this) {
                @Override
                protected BufferedImage transform(BufferedImage image) {
                    return transform.apply(image);
                }
            };
        }

        @Override
        public Exception getException() {
            return origin.getException();
        }

        protected abstract BufferedImage transform(BufferedImage image);
    }
}
