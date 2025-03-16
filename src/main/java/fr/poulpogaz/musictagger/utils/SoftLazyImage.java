package fr.poulpogaz.musictagger.utils;

import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public abstract class SoftLazyImage extends AbstractLazyImage {

    private static final Logger LOGGER = LogManager.getLogger(SoftLazyImage.class);

    private static final Map<String, BufferedImage> CACHE = Collections.synchronizedMap(new ReferenceMap<>());



    public static SoftLazyImage createFromFile(File file) {
        return new FileSoftLazyImage(file, null);
    }

    public static SoftLazyImage createFromFile(File file, BufferedImage image) {
        return new FileSoftLazyImage(file, image);
    }



    protected String hash;

    public SoftLazyImage() {
    }

    public SoftLazyImage(String hash) {
        this.hash = hash;
    }

    @Override
    public BufferedImage getImageNow() {
        if (hash == null) {
            return null;
        }
        return CACHE.get(hash);
    }

    @Override
    protected void onImageLoad(BufferedImage img) {
        if (hash != null) {
            CACHE.put(hash, img);
        } else {
            LOGGER.warn("hash is null, cannot cache image");
        }
    }

    protected abstract BufferedImage loadImage() throws Exception;

    private static class FileSoftLazyImage extends SoftLazyImage {

        private final File file;

        public FileSoftLazyImage(File file) {
            this(file, null);
        }

        public FileSoftLazyImage(File file, BufferedImage image) {
            super(fileHash(file));
            this.file = file;

            if (image != null) {
                CACHE.putIfAbsent(hash, image);
            }
        }

        private static String fileHash(File file) {
            byte[] bytes = file.getAbsolutePath().getBytes(StandardCharsets.UTF_8);
            byte[] hash = Utils.SHA_256.digest(bytes);
            return Utils.bytesToHex(hash);
        }

        @Override
        public BufferedImage loadImage() throws Exception {
            LOGGER.debug("Loading cover art from {}", file);

            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                return ImageIO.read(file);
            }
        }
    }
}
