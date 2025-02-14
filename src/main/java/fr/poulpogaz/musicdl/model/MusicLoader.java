package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonReader;
import fr.poulpogaz.json.utils.Pair;
import fr.poulpogaz.musicdl.ui.MusicdlFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class MusicLoader extends SwingWorker<Void, MusicLoader.Chunk> {

    private static final Object LOCK = new Object();

    private static MusicLoader loader;
    private static final ExecutorService counter = Executors.newSingleThreadExecutor();

    public static void load(File[] files) {
        synchronized (LOCK) {
            if (loader == null) {
                loader = new MusicLoader();
                loader.execute();
            }

            loader.offerAll(files);
        }
    }

    public static void shutdown() {
        counter.shutdown();
    }



    private static final Logger LOGGER = LogManager.getLogger(MusicLoader.class);

    private final Queue<Path> queue = new ArrayDeque<>();
    private final AtomicLong fileCount = new AtomicLong();

    private Chunk chunk = new Chunk();
    private long lastPublish = 0;


    private MusicLoader() {}

    public void offerAll(File[] files) {
        for (File file : files) {
            Path path = file.toPath();
            queue.add(path);

            if (Files.isDirectory(path)) {
                counter.execute(() -> countFiles(path));
            } else {
                fileCount.incrementAndGet();
            }
        }
    }

    private void countFiles(Path path) {
        try (Stream<Path> stream = Files.walk(path)) {
            Iterator<Path> it = stream.filter(this::isOpusFile).iterator();
            while (it.hasNext()) {
                it.next();
                fileCount.incrementAndGet();
            }

        } catch (IOException e) {
            LOGGER.debug("Count files at {} failed", path, e);
        }
    }


    @Override
    protected Void doInBackground() {
        LOGGER.debug("Music loader start");
        while (true) {
            Path path;
            synchronized (LOCK) {
                if (queue.isEmpty()) {
                    loader = null;
                    break;
                }

                path = queue.poll();
            }

            if (!Files.exists(path)) {
                LOGGER.warn("File doesn't exist: {}", path);
                continue;
            }

            if (Files.isDirectory(path)) {
                processDirectory(path);
            } else if (Files.isRegularFile(path)) {
                if (isJsonFile(path)) {
                    processJson(path);
                } else if (isOpusFile(path)) {
                    processMusic(path);
                } else {
                    LOGGER.warn("Not a JSON or opus: {}", path);
                }
            } else {
                LOGGER.warn("Invalid file type: {}", path);
            }
        }

        sendChunk(true);
        LOGGER.debug("Music loader stopped");
        return null;
    }

    private void processDirectory(Path directory) {
        try (Stream<Path> stream = Files.walk(directory)) {
            Iterator<Path> it = stream.filter(this::isOpusFile).iterator();

            while (it.hasNext()) {
                Path path = it.next();
                processMusic(path);
            }
        } catch (IOException e) {
            LOGGER.warn("Exception while processing directory {}", directory, e);
        }
    }

    private void processJson(Path path) {
        try (BufferedReader br = Files.newBufferedReader(path)) {
            IJsonReader jr = new JsonReader(br);

            jr.beginArray();
            while (!jr.isArrayEnd()) {
                jr.beginObject();
                Pair<Music, String> music = Music.load(jr);
                jr.endObject();
                chunk.addMusic(music.getLeft(), music.getRight());
                sendChunk(false);
            }

            jr.endArray();
        } catch (IOException | JsonException e) {
            LOGGER.debug("Failed to read opus file: {}", path, e);
        } finally {
            fileCount.decrementAndGet();
        }
    }

    private void processMusic(Path path) {
        Pair<Music, String> music = null;
        try {
            music = Music.load(path);
        } catch (IOException e) {
            LOGGER.debug("Failed to read opus file: {}", path, e);
        } finally {
            if (music != null) {
                chunk.addMusic(music.getLeft(), music.getRight());
            }

            fileCount.decrementAndGet();
            sendChunk(false);
        }
    }


    private void sendChunk(boolean force) {
        if (System.currentTimeMillis() - lastPublish >= 200 || force) {
            chunk.remaining = fileCount.get();
            publish(chunk);
            chunk = new Chunk();
            lastPublish = System.currentTimeMillis();
        }
    }


    private boolean isOpusFile(Path path) {
        return path.getFileName().toString().endsWith(".opus");
    }

    private boolean isJsonFile(Path path) {
        return path.getFileName().toString().endsWith(".json");
    }






    @Override
    protected void process(List<Chunk> chunks) {
        for (Chunk chunk : chunks) {
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

        Chunk d = chunks.getLast();
        MusicdlFrame.getInstance().setLoadingFileCount(Math.max(d.remaining, 0));
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Loading musics failed", e);
        }

        // in case a loader crash
        synchronized (LOCK) {
            if (loader == null) {
                MusicdlFrame.getInstance().setLoadingFileCount(0);
            }
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

        public void addMusic(Music music, String template) {
            musics.add(music);
            templateNames.add(template);
        }
    }
}
