package fr.poulpogaz.musicdl.ui;

import fr.poulpogaz.json.utils.Pair;
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
        Pair<Music, String> music;
        try {
            music = Music.load(path);
        } catch (IOException e) {
            LOGGER.debug("Failed to read opus file: {}", path, e);
            return;
        }

        output.addMusic(music.getLeft(), music.getRight());
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
