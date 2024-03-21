package fr.poulpogaz.musicdb.downloader;

import fr.poulpogaz.musicdb.model.Music;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Downloader {


    private static final Logger LOGGER = LogManager.getLogger(Downloader.class);
    private static final ExecutorService READ_ERR_EXECUTOR;

    static {
        ThreadFactory factory = Thread.ofVirtual().name("yt-dlp err read-", 1).factory();
        READ_ERR_EXECUTOR = Executors.newThreadPerTaskExecutor(factory);
    }




    private final Object READ_LOCK = new Object();

    private final AtomicBoolean isDownloading = new AtomicBoolean(false);
    private final StringBuilder errors = new StringBuilder();

    private Process process;
    private Music music;
    private Path location;
    private Progress progress;


    public Downloader() {

    }

    public Path download(Music music, Progress progress) throws IOException {
        if (isDownloading.getAndSet(true)) {
            throw new AlreadyDownloadingException();
        }

        this.music = music;
        this.progress = progress;

        if (progress != null) {
            progress.reset();
        }

        try {
            if (music.getDownloadURL() == null) {
                LOGGER.info("No youtube url set for {}", music);
                return null;
            }

            Path output = getOutput(music);

            /* if (output == null) {
                LOGGER.info("Music already downloaded {}", music.getEffectiveDownloadPath());
                return null;
            } */

            LOGGER.info("Downloading {} to {}", music.getDownloadURL(), output);

            process = createProcess(music, output.toString());

            Future<?> err = READ_ERR_EXECUTOR.submit(() -> read(process.errorReader(), false));
            read(process.inputReader(), true);

            try {
                err.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            if (!errors.isEmpty()) {
                throw new IOException(errors.toString());
            }

            return location;
        } finally {
            clean();
        }
    }

    private Path getOutput(Music music) {
        Path downloadRoot = DownloadManager.getDownloadRoot();

        if (music.getTemplate() != null) {
            String output = music.getTemplate().getFormatter().format(music);

            if (output != null) {
                return downloadRoot.resolve(output);
            }
        }

        return downloadRoot.resolve(music + ".opus");
    }




    private Process createProcess(Music music, String output) throws IOException {
        ArgBuilder builder = new ArgBuilder();
        builder.add("--abort-on-error");
        if (progress != null) {
            progress.addArguments(builder);
        }
        builder.add("--print", "after_move:filepath"); // final file location
        builder.add("--no-simulate"); // print implies --simulate

        builder.add("--windows-filenames")
                .add("--extract-audio")
                .add("--audio-format", "opus")
                .add("-f", "bestaudio")
                .add("--no-overwrites")
                .add("--embed-thumbnail")
                .add("--embed-metadata")
                .add("--parse-metadata", ":(?P<meta_language>)")
                .add("--parse-metadata", ":(?P<meta_synopsis>)")
                .add("--parse-metadata", "%(title)s:%(meta_yt_title)s") // wtf is this, thanks to https://stackoverflow.com/questions/71347719/set-metadata-based-on-the-output-filename-in-yt-dlp
                .add("--parse-metadata", "%(artist,creator,uploader,uploader_id)s:%(meta_yt_artist)s");

        /*for (int i = 0; i < music.getTemplate().keyCount(); i++) {
            Key key = music.getTemplate().getKey(i);

            if (key.getMetadataKey() != null) {
                builder.add("--parse-metadata", ytdlpFormatEscape(music.getTag(i)) + ":%(meta_" + key.getMetadataKey() + ")s");
            }
        }*/


        builder.add("-o", output).add(music.getDownloadURL().toString());

        LOGGER.debug("Executing {}", builder.getCommands());

        return new ProcessBuilder()
                .command(builder.getCommands()).start();
    }

    /**
     * Escape str to make it python-compliant
     */
    private static String ytdlpFormatEscape(String str) {
        boolean word = true;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == ':') {
                sb.append('\\');
                word = false;
            } else if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && c != '_') { // ie not a-zA-Z_
                word = false;
            }
            sb.append(c);
        }

        if (word) {
            sb.append(" "); // otherwise, yt-dlp think it is a field: https://github.com/yt-dlp/yt-dlp/blob/45db357289b4e1eec09093c8bc5446520378f426/yt_dlp/postprocessor/metadataparser.py#L27
        }

        return sb.toString();
    }



    private void read(BufferedReader br, boolean std) {
        try {
            String l;
            while ((l = br.readLine()) != null) {
                synchronized (READ_LOCK) {
                    LOGGER.trace("YT-DLP: {}", l);
                    doRead(l, std);
                }
            }

            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doRead(String line, boolean std) {
        if (progress != null && progress.parse(line)) {
            return; // line indicates progression
        }

        if (std) {
            location = Path.of(line);
        } else if (!line.startsWith("WARNING")) {
            if (!errors.isEmpty()) {
                errors.append(System.lineSeparator());
            }
            errors.append(line);
        }
    }

    private void clean() {
        errors.setLength(0);
        progress = null;
        location = null;
        music = null;
        process = null;
        isDownloading.set(false);
    }

    public void cancel() {
        if (isDownloading.get()) {
            process.destroy();

            if (progress != null) {
                progress.setCanceled();
            }
        }
    }

    public boolean isDownloading() {
        return isDownloading.get();
    }

    public Progress getProgress() {
        return progress;
    }
}
