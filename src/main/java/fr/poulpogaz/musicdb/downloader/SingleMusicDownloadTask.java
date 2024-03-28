package fr.poulpogaz.musicdb.downloader;

import fr.poulpogaz.musicdb.Units;
import fr.poulpogaz.musicdb.model.Music;
import fr.poulpogaz.musicdb.ui.layout.VerticalConstraint;
import fr.poulpogaz.musicdb.ui.layout.VerticalLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SingleMusicDownloadTask extends DownloadTask {

    private final Music music;
    private final Progress progress = new TaskProgress();

    public SingleMusicDownloadTask(Music music) {
        this.music = music;
    }

    @Override
    public void download() throws IOException {
        downloader.download(music, progress);
    }

    @Override
    protected void cancelImpl() {
        if (downloader != null) {
            downloader.cancel();
        }
    }

    @Override
    public String getDescription() {
        return "Downloading " + music.getDownloadURL();
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
