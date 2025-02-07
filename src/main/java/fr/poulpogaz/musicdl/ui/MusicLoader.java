package fr.poulpogaz.musicdl.ui;

import fr.poulpogaz.musicdl.model.Music;
import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.model.Templates;
import fr.poulpogaz.musicdl.opus.MetadataPicture;
import fr.poulpogaz.musicdl.opus.OggPage;
import fr.poulpogaz.musicdl.opus.OpusHead;
import fr.poulpogaz.musicdl.opus.OpusInputStream;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
        LOGGER.debug("Loading {}", path);

        Music music = new Music();
        music.setPath(path);

        String templateName = null;
        try (OpusInputStream file = new OpusInputStream(path)) {
            music.setSize(Files.size(path));

            OpusHead head = file.readOpusHead();
            file.readVendor();

            music.setChannels(head.getChannels());

            int c = (int) Math.min(file.readCommentCount(), 8192);
            for (; c > 0; c--) {
                String key = file.readKey();

                switch (key) {
                    case "METADATA_BLOCK_PICTURE" -> {
                        InputStream picIS = Base64.getDecoder().wrap(file.valueInputStream());
                        MetadataPicture pic = MetadataPicture.fromInputStream(picIS);
                        music.addPicture(pic);
                    }
                    case "TEMPLATE" -> templateName = file.readValue();
                    case "PURL" -> music.setDownloadURL(file.readValue());
                    default -> music.addMetadata(key, file.readValue());
                }
            }


            OggPage previous = null;
            OggPage page;
            while ((page = file.readPage()) != null) {
                previous = page;
            }
            if (previous != null) {
                System.out.println(previous.getGranulePosition());
            }

        } catch (IOException e) {
            LOGGER.debug("Failed to read opus file: {}", path, e);
            return;
        }

        output.addMusic(music, templateName);
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
