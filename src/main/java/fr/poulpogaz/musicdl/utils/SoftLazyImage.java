package fr.poulpogaz.musicdl.utils;

import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.DigestInputStream;
import java.util.Collections;
import java.util.Map;

public abstract class SoftLazyImage extends AbstractLazyImage {

    private static final Logger LOGGER = LogManager.getLogger(SoftLazyImage.class);

    private static final Map<String, BufferedImage> CACHE = Collections.synchronizedMap(new ReferenceMap<>());



    public static SoftLazyImage createFromFile(File file) throws IOException {
        return createFromFile(file, false);
    }

    public static SoftLazyImage createFromFile(File file, boolean cacheImage) throws IOException {
        String sha256;
        BufferedImage image = null;

        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            if (cacheImage) {
                DigestInputStream dis = new DigestInputStream(is, Utils.SHA_256);
                image = ImageIO.read(dis);

                sha256 = Utils.bytesToHex(Utils.SHA_256.digest());
            } else {
                sha256 = Utils.sha256(is);
            }
        }

        return new FileSoftLazyImage(file, sha256, image);
    }

    public static SoftLazyImage createFromFileHashAsync(File file) {
        return new FileSoftLazyImage(file);
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
            this.file = file;
        }

        public FileSoftLazyImage(File file, String sha256, BufferedImage image) {
            super(sha256);
            this.file = file;

            if (image != null) {
                CACHE.putIfAbsent(sha256, image);
            }
        }

        @Override
        public BufferedImage loadImage() throws Exception {
            LOGGER.debug("Loading cover art from {}", file);

            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                BufferedImage image;
                if (hash == null) {
                    DigestInputStream dis = new DigestInputStream(is, Utils.SHA_256);
                    image = ImageIO.read(dis);

                    hash = Utils.bytesToHex(Utils.SHA_256.digest());
                    return image;
                } else {
                    return ImageIO.read(file);
                }
            }
        }
    }
}
