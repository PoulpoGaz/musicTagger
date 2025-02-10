package fr.poulpogaz.musicdl.ui;

import fr.poulpogaz.musicdl.Utils;
import fr.poulpogaz.musicdl.model.*;
import fr.poulpogaz.musicdl.opus.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class MusicLoader extends SwingWorker<Void, MusicLoader.Chunk> {

    private static final MessageDigest SHA_256;

    static {
        try {
            SHA_256 = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private static final Logger LOGGER = LogManager.getLogger(MusicLoader.class);

    private final Path path;

    public MusicLoader(Path path) {
        this.path = path;
    }

    @Override
    protected Void doInBackground() throws Exception {
        long remaining = countFiles();
        long lastRemaining = remaining;
        publish(new Chunk(remaining));

        long time = System.currentTimeMillis();
        Chunk chunk = new Chunk();
        try (Stream<Path> stream = Files.walk(path)) {
            Iterator<Path> it = stream.filter(this::isOpusFile).iterator();

            while (it.hasNext()) {
                Path path = it.next();
                readMusic(path, chunk);
                remaining--;

                if (System.currentTimeMillis() - time > 1000) {
                    chunk.remaining = remaining;
                    publish(chunk);
                    chunk = new Chunk();

                    lastRemaining = remaining;
                    time = System.currentTimeMillis();
                }
            }
        }

        if (lastRemaining > 0) {
            publish(chunk);
        }

        LOGGER.debug("All musics loaded");

        return null;
    }

    private long countFiles() throws IOException {
        try (Stream<Path> stream = Files.walk(path)) {
            return stream.filter(this::isOpusFile).count();
        }
    }

    private void readMusic(Path path, Chunk output) {
        LOGGER.debug("Loading {}", path);

        MetadataPicture pic = new MetadataPicture();

        Music music = new Music();
        music.setPath(path);

        String templateName = null;
        try (OggInputStream ois = new OggInputStream(path)) {
            OpusInputStream file = new OpusInputStream(ois);

            music.setSize(Files.size(path));

            OpusHead head = file.readOpusHead();
            file.readVendor();

            music.setChannels(head.getChannels());

            int c = (int) Math.min(file.readCommentCount(), 8192);
            for (; c > 0; c--) {
                String key = file.readKey();

                switch (key) {
                    case "METADATA_BLOCK_PICTURE" -> {
                        long position = ois.currentPagePosition();
                        int offset = file.positionInPage();
                        InputStream picIS = Base64.getDecoder().wrap(file.valueInputStream());
                        pic.fromInputStream(picIS, false);

                        String sha256 = computeSHA256(picIS, pic.getDataLength());
                        SoftCoverArt cover = createSoftCoverArt(sha256, path, position, offset);
                        music.addCoverArt(cover);
                    }
                    case "TEMPLATE" -> templateName = file.readValue();
                    case "PURL" -> music.setDownloadURL(file.readValue());
                    default -> music.addMetadata(key, file.readValue());
                }
            }

            music.setLength(file.fileLength());

        } catch (IOException e) {
            LOGGER.debug("Failed to read opus file: {}", path, e);
            return;
        }

        output.addMusic(music, templateName);
    }

    private final byte[] buff = new byte[8192];

    private String computeSHA256(InputStream is, int length) throws IOException {
        int remaining = length;

        while (remaining > 0) {
            int toRead = Math.min(buff.length, remaining);
            int r = is.read(buff, 0, toRead);
            if (r < 0) {
                throw new IOException("Not enough bytes");
            }

            SHA_256.update(buff, 0, r);
            remaining -= r;
        }

        return Utils.bytesToHex(SHA_256.digest());
    }

    private SoftCoverArt createSoftCoverArt(String sha256, Path file, long pagePosition, long dataOffset) {
        LOGGER.debug("Creating soft cover art for {} at {} with an offset of {}", file, pagePosition, dataOffset);

        return new SoftCoverArt(sha256) {
            @Override
            public BufferedImage loadImage() throws IOException {
                LOGGER.debug("Loading cover art from {} at {} with an offset of {}", file, pagePosition, dataOffset);
                try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
                    channel.position(pagePosition);

                    OggInputStream ois = new OggInputStream(channel);
                    PacketInputStream pis = new PacketInputStream(ois);
                    pis.skipNBytes(dataOffset);

                    InputStream is = Base64.getDecoder().wrap(pis);
                    is.skipNBytes(4); // skip type
                    is.skipNBytes(IOUtils.getIntB(is)); // skip mimeLength
                    is.skipNBytes(IOUtils.getIntB(is)); // skip description
                    is.skipNBytes(16); // skip width, height, color depth and color count
                    int length = IOUtils.getIntB(is);

                    return ImageIO.read(new LimitedInputStream(is, length));
                }
            }
        };
    }


    private boolean isOpusFile(Path path) {
        return path.getFileName().toString().endsWith(".opus");
    }

    @Override
    protected void process(List<Chunk> chunks) {
        for (Chunk chunk : chunks) {
            if (chunk.musics != null) {
                for (int i = 0; i < chunk.musics.size(); i++) {
                    Music music = chunk.musics.get(i);
                    String templateName = chunk.templateNames.get(i);

                    Template template = Templates.getTemplate(templateName);
                    if (template == null) {
                        template = Templates.getDefaultTemplate();
                    }

                    template.getData().addMusic(music);
                }
            }
        }

        Chunk d = chunks.getLast();
        MusicdlFrame.getInstance().setLoadingMusicCount(d.remaining);
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Loading musics failed", e);
        }
    }

    protected static class Chunk {
        private long remaining;
        private final List<Music> musics;
        private final List<String> templateNames;

        public Chunk() {
            musics = new ArrayList<>();
            templateNames = new ArrayList<>();
        }

        public Chunk(long remaining) {
            this.remaining = remaining;
            musics = null;
            templateNames = null;
        }

        public void addMusic(Music music, String template) {
            musics.add(music);
            templateNames.add(template);
        }
    }
}
