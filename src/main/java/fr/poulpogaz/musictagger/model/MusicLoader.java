package fr.poulpogaz.musictagger.model;

import fr.poulpogaz.json.IJsonReader;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonReader;
import fr.poulpogaz.json.utils.Pair;
import fr.poulpogaz.musictagger.opus.OpusFile;
import fr.poulpogaz.musictagger.ui.MTFrame;
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

    public static void load(Template dest, File[] files) {
        synchronized (LOCK) {
            if (loader == null) {
                loader = new MusicLoader();
                loader.execute();
            }

            loader.offer(dest, files);
        }
    }

    public static void shutdown() {
        counter.shutdown();
    }



    private static final Logger LOGGER = LogManager.getLogger(MusicLoader.class);

    private final Queue<Task> queue = new ArrayDeque<>();
    private final AtomicLong fileCount = new AtomicLong();

    private Chunk chunk;
    private long lastPublish = 0;


    private MusicLoader() {}

    public void offer(Template defaultTemplate, File[] files) {
        queue.add(new Task(defaultTemplate.getName(), files));

        for (File file : files) {
            if (file.isDirectory()) {
                counter.execute(() -> countFiles(file.toPath()));
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
            Task task;
            synchronized (LOCK) {
                if (queue.isEmpty()) {
                    loader = null;
                    break;
                }

                task = queue.poll();
            }

            if (chunk == null) {
                chunk = new Chunk(task.defaultTemplateName);
            }

            for (File file : task.files) {
                Path path = file.toPath();

                if (!Files.exists(path)) {
                    LOGGER.warn("File doesn't exist: {}", path);
                    continue;
                }

                try {
                    if (Files.isDirectory(path)) {
                        processDirectory(task.defaultTemplateName, path);
                    } else if (Files.isRegularFile(path)) {
                        if (isJsonFile(path)) {
                            processJson(task.defaultTemplateName, path);
                        } else if (isOpusFile(path)) {
                            processMusic(task.defaultTemplateName, path);
                        } else {
                            LOGGER.warn("Not a JSON or opus: {}", path);
                        }
                    } else {
                        LOGGER.warn("Invalid file type: {}", path);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to load music", e);
                }
            }
        }

        sendChunk(null, true);
        LOGGER.debug("Music loader stopped");
        return null;
    }

    private void processDirectory(String defaultTemplate, Path directory) {
        try (Stream<Path> stream = Files.walk(directory)) {
            Iterator<Path> it = stream.filter(this::isOpusFile).iterator();

            while (it.hasNext()) {
                Path path = it.next();
                processMusic(defaultTemplate, path);
            }
        } catch (IOException e) {
            LOGGER.warn("Exception while processing directory {}", directory, e);
        }
    }

    private void processJson(String defaultTemplate, Path path) {
        try (BufferedReader br = Files.newBufferedReader(path)) {
            IJsonReader jr = new JsonReader(br);

            jr.beginArray();
            while (!jr.isArrayEnd()) {
                jr.beginObject();
                Pair<Music, String> music = Music.load(jr);
                jr.endObject();
                chunk.addMusic(music.getLeft(), music.getRight());
                sendChunk(defaultTemplate, false);
            }

            jr.endArray();
        } catch (IOException | JsonException e) {
            LOGGER.debug("Failed to read opus file: {}", path, e);
        } finally {
            fileCount.decrementAndGet();
        }
    }

    private void processMusic(String defaultTemplate, Path path) {
        Pair<Music, String> music = null;
        try {
            OpusFile file = new OpusFile(path);
            List<String> templates = file.removeAll("TEMPLATE");
            String template;
            if (templates.isEmpty()) {
                template = null;
            } else {
                template = templates.getFirst();
            }
            music = new Pair<>(new Music(file), template);
        } catch (IOException e) {
            LOGGER.debug("Failed to read opus file: {}", path, e);
        } finally {
            if (music != null) {
                chunk.addMusic(music.getLeft(), music.getRight());
            }

            fileCount.decrementAndGet();
            sendChunk(defaultTemplate, false);
        }
    }


    private void sendChunk(String defaultTemplate, boolean force) {
        if (force || System.currentTimeMillis() - lastPublish >= 200
                || (chunk != null && !chunk.musics.isEmpty() && !chunk.defaultTemplate.equals(defaultTemplate))) {
            chunk.remaining = fileCount.get();
            publish(chunk);
            chunk = new Chunk(defaultTemplate);
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
                Template template = getTemplate(templateName, chunk.defaultTemplate);

                template.getData().addMusic(music);
            }
        }

        Chunk d = chunks.getLast();
        MTFrame.getInstance().setLoadingFileCount(Math.max(d.remaining, 0));
    }

    private Template getTemplate(String templateName, String defaultTemplateName) {
        Template template = null;
        if (templateName != null) {
            template = Templates.getTemplate(templateName);
        }

        if (template == null && defaultTemplateName != null) {
            template = Templates.getTemplate(defaultTemplateName);
        }

        if (template == null) {
            return Templates.getTemplates().iterator().next();
        } else {
            return template;
        }
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
                MTFrame.getInstance().setLoadingFileCount(0);
            }
        }
    }

    protected static class Chunk {
        private long remaining;
        private final String defaultTemplate;
        private final List<Music> musics = new ArrayList<>();
        private final List<String> templateNames = new ArrayList<>();

        public Chunk(String defaultTemplate) {
            this.defaultTemplate = defaultTemplate;
        }

        public void addMusic(Music music, String template) {
            musics.add(music);
            templateNames.add(template);
        }
    }

    private record Task(String defaultTemplateName, File[] files) {

    }
}
