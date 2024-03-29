package fr.poulpogaz.musicdb.downloader;

import fr.poulpogaz.musicdb.Units;
import fr.poulpogaz.musicdb.ui.layout.VerticalConstraint;
import fr.poulpogaz.musicdb.ui.layout.VerticalLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class YTDLPDownloadTask extends DownloadTask {


    public static YTDLP ytdlp(String url) {
        YTDLP ytdlp = new YTDLP(url);

        ytdlp.abortOnError(true)
             .embedThumbnail(true)
             .noOverwrites()
             .useWindowsFilenames(true)
             .addOption("--print", "after_move:filepath")
             .addOption("--no-simulate")
             .addOption("--extract-audio")
             .addOption("--format", "bestaudio")
             .addOption("--embed-metadata")
             .addOption("--parse-metadata", ":(?P<meta_language>)")
             .addOption("--parse-metadata", ":(?P<meta_synopsis>)")
             .addOption("--parse-metadata", "%(title)s:%(meta_yt_title)s") // wtf is this, thanks to https://stackoverflow.com/questions/71347719/set-metadata-based-on-the-output-filename-in-yt-dlp
             .addOption("--parse-metadata", "%(artist,creator,uploader,uploader_id)s:%(meta_yt_artist)s");

        return ytdlp;
    }








    private static final Logger LOGGER = LogManager.getLogger(YTDLPDownloadTask.class);
    private static final ExecutorService READ_ERR_EXECUTOR;

    static {
        ThreadFactory factory = Thread.ofVirtual().name("yt-dlp err read-", 1).factory();
        READ_ERR_EXECUTOR = Executors.newThreadPerTaskExecutor(factory);
    }


    private final YTDLP ytdlp;
    private final Progress progress = new TaskProgress();
    private StringBuilder errors;

    public YTDLPDownloadTask(YTDLP ytdlp) {
        this.ytdlp = Objects.requireNonNull(ytdlp).copy();
    }

    @Override
    public void download() throws Exception {
        LOGGER.info("Downloading {} to {}", ytdlp.getURL(), ytdlp.getOutput());
        progress.reset();
        progress.addOptions(ytdlp);


        ProcessBuilder builder = ytdlp.createProcess();
        LOGGER.debug("Executing {}", builder.command());
        Process process = ytdlp.createProcess().start();

        Future<?> err = READ_ERR_EXECUTOR.submit(() -> read(process.errorReader(), false));
        read(process.inputReader(), true);
        err.get();

        if (errors != null) {
            throw new IOException(errors.toString());
        }
    }

    private Void read(BufferedReader br, boolean std) throws IOException {
        String l;
        while ((l = br.readLine()) != null) {
            LOGGER.trace("YT-DLP: {}", l);
            doRead(l, std);
        }

        br.close();
        return null;
    }

    private void doRead(String line, boolean std) {
        if (progress.parse(line)) {
            return; // line indicates progression
        }

        if (std) {
            // location = Path.of(line);
        } else if (!line.startsWith("WARNING")) {
            if (errors == null) {
                errors = new StringBuilder();
            } else {
                errors.append(System.lineSeparator());
            }
        }
    }

    @Override
    protected void cancelImpl() {

    }

    @Override
    public String getDescription() {
        return "Downloading " + ytdlp.getURL();
    }

    @Override
    public Progress getProgress() {
        return progress;
    }

    protected static class TaskProgress extends AbstractProgress {

        @Override
        public synchronized Component createProgressComponent() {
            return updateProgressComponent(new ProgressPanel());
        }
    }

    protected static class ProgressPanel extends AbstractProgressPanel {

        protected final JProgressBar downloadProgressBar;
        protected final JLabel detail;

        public ProgressPanel() {
            downloadProgressBar = new JProgressBar();
            downloadProgressBar.setStringPainted(true);
            detail = new JLabel();

            setLayout(new VerticalLayout());
            VerticalConstraint c = new VerticalConstraint();
            c.fillXAxis = true;
            c.topGap = 2;
            c.bottomGap = 2;
            add(downloadProgressBar, c);
            add(detail, c);
        }

        @Override
        public void update(AbstractProgress progress) {
            setProgressBarValues(downloadProgressBar, progress);

            StringBuilder sb = new StringBuilder();
            boolean space = false;
            if (progress.getSpeed() >= 0) {
                sb.append(Units.humanReadableSpeed(progress.getSpeed()));
                space = true;
            }
            if (progress.getETA() >= 0) {
                if (space) {
                    sb.append(" ");
                }
                sb.append("ETA ").append(progress.getETA()).append(" s");
            }
            if (progress.isCanceled()) {
                if (space) {
                    sb.append(" ");
                }
                sb.append("Canceled...");
            }

            detail.setText(sb.toString());
        }
    }
}
