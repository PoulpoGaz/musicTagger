package fr.poulpogaz.musicdl.ui;

import fr.poulpogaz.musicdl.model.Music;
import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.model.Templates;
import fr.poulpogaz.musicdl.opus.MetadataPicture;
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

public class MusicLoader extends SwingWorker<Void, MusicLoader.Data> {

    private static final Logger LOGGER = LogManager.getLogger(MusicLoader.class);

    private final Path path;

    private final MultiValuedMap<String, String> metadata = new ArrayListValuedHashMap<>();
    private final List<MetadataPicture> pictures = new ArrayList<>();

    public MusicLoader(Path path) {
        this.path = path;
    }

    @Override
    protected Void doInBackground() throws Exception {
        int remaining = countFiles();
        int lastRemaining = remaining;
        publish(new Data(remaining, null));

        List<Music> musics = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(path)) {
            Iterator<Path> it = stream.filter(this::isOpusFile).iterator();

            while (it.hasNext()) {
                Path path = it.next();
                Music m = readMusic(path);
                remaining--;

                if (m != null) {
                    musics.add(m);
                }

                if (lastRemaining - remaining >= 128) {
                    if (musics.isEmpty()) {
                        publish(new Data(remaining, null));
                    } else {
                        publish(new Data(remaining, musics));
                        musics = new ArrayList<>();
                    }

                    lastRemaining = remaining;
                }
            }
        }

        if (lastRemaining > 0) {
            publish(new Data(0, musics));
        }

        LOGGER.debug("All musics loaded");

        return null;
    }

    private int countFiles() throws IOException {
        try (Stream<Path> stream = Files.walk(path)) {
            return Math.toIntExact(stream.filter(this::isOpusFile).count());
        }
    }

    private Music readMusic(Path path) {
        LOGGER.debug("Loading {}", path);

        metadata.clear();
        pictures.clear();
        Template template = null;

        try (OpusInputStream file = new OpusInputStream(path)) {
            file.readOpusHead();
            file.readVendor();

            int c = (int) Math.min(file.readCommentCount(), 8192);
            for (; c > 0; c--) {
                String key = file.readKey();

                if (key.equals("METADATA_BLOCK_PICTURE")) {
                    InputStream picIS = Base64.getDecoder().wrap(file.valueInputStream());
                    MetadataPicture pic = MetadataPicture.fromInputStream(picIS);
                    pictures.add(pic);
                } else if (key.equals("TEMPLATE")) {
                    template = Templates.getTemplate(file.readValue());
                } else {
                    metadata.put(key, file.readValue());
                }
            }
        } catch (IOException e) {
            LOGGER.debug("Failed to read opus file: {}", path, e);
            return null;
        }

        return createMusic(template);
    }

    private Music createMusic(Template template) {
        if (template == null) {
            return null; // template = Templates.getDefaultTemplate();
        }

        Music m = new Music(template);
        for (MetadataPicture p : pictures) {
            m.addPicture(p);
        }

        for (Map.Entry<String, Collection<String>> entry : metadata.asMap().entrySet()) {
            String key = entry.getKey();
            Collection<String> values = entry.getValue();
            String value = String.join("; ", values);

            if (template.containsKey(key)) {
                m.putTag(key, value);
            } else {
                m.addMetadata(key, value);
            }
        }

        return m;
    }

    private boolean isOpusFile(Path path) {
        return path.getFileName().toString().endsWith(".opus");
    }

    @Override
    protected void process(List<Data> chunks) {
        for (Data data : chunks) {
            if (data.musics != null) {
                for (Music music : data.musics) {
                    music.getTemplate().getData().addMusic(music); // uh
                }
            }
        }

        Data d = chunks.getLast();
        MusicdlFrame.getInstance().setLoadingMusicCount(d.remaining);
    }

    protected record Data(int remaining, List<Music> musics) {}
}
