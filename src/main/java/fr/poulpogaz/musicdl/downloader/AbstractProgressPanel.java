package fr.poulpogaz.musicdl.downloader;

import fr.poulpogaz.musicdl.Units;

import javax.swing.*;

public abstract class AbstractProgressPanel extends JPanel {

    public abstract void update(AbstractProgress progress);

    protected void setProgressBarValues(JProgressBar downloadProgressBar, AbstractProgress progress) {
        if (progress.getPostprocessor() != null) {
            downloadProgressBar.setIndeterminate(true);
            downloadProgressBar.setString("Postprocessing: " + progress.getPostprocessor());
        } else {
            if (progress.getDownloadedBytes() <= 0) {
                downloadProgressBar.setIndeterminate(false);
                downloadProgressBar.setValue(0);
                downloadProgressBar.setString(progress.getStatus());
            } else if (progress.getTotalBytes() <= 0) {
                downloadProgressBar.setIndeterminate(true);
                downloadProgressBar.setString(progress.getStatus() + " " +
                        Units.humanReadableBytes(progress.getDownloadedBytes()) + " of ? B");
            } else {
                int percent = (int) (100 * progress.getDownloadedBytes() / progress.getTotalBytes());

                downloadProgressBar.setIndeterminate(false);
                downloadProgressBar.setValue(percent);
                downloadProgressBar.setString(progress.getStatus() + " " +
                        Units.humanReadableBytes(progress.getDownloadedBytes()) + " of " +
                        Units.humanReadableBytes(progress.getTotalBytes()));
            }
        }
    }
}
