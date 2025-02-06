package fr.poulpogaz.musicdl.downloader;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractProgress implements Progress {

    private static final Pattern D_PATTERN =
            Pattern.compile("^d ([^ ]+) ([^ ]+) ([^ ]+) ([^ ]+) ([^ ]+) ([^ ]+)$");

    private static final Pattern P_PATTERN =
            Pattern.compile("^p ([^ ]+) ([^ ]+)$");

    private String status;
    private String postprocessor; // if not null, then all four fiels below are irrelevant

    private long timeElapsed; // millis
    private double speed; // bytes/second
    private long eta; // s
    private long downloadedBytes; // bytes
    private long totalBytes; // bytes

    private boolean canceled;

    public AbstractProgress() {
        reset();
    }

    @Override
    public synchronized void addOptions(YTDLP ytdlp) {
        ytdlp.addOption("--progress")
             .addOption("--newline")
             .addOption("--progress-template")
             .addOption("download:d %(progress.status)s %(progress.elapsed)s %(progress.eta)s %(progress.speed)s %(progress.downloaded_bytes)s %(progress.total_bytes)s")
             .addOption("--progress-template")
             .addOption("postprocess:p %(progress.status)s %(progress.postprocessor)s");
    }

    @Override
    public synchronized boolean parse(String line) {
        try {
            if (line.startsWith("d ")) {
                Matcher m = D_PATTERN.matcher(line);
                if (m.matches()) {
                    parseDownload(m);
                    return true;
                }

            } else if (line.startsWith("p ")) {
                Matcher m = P_PATTERN.matcher(line);
                if (m.matches()) {
                    parsePostprocessing(m);
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            // ignored
        }

        return false;
    }

    private void parseDownload(Matcher m) {
        status = m.group(1);
        postprocessor = null;
        timeElapsed = ((long) Float.parseFloat(m.group(2))) * 1000;

        if (m.group(3).equals("NA")) {
            eta = -1;
        } else {
            eta = Long.parseLong(m.group(3));
        }

        if (m.group(4).equals("NA")) {
            speed = -1;
        } else {
            speed = Double.parseDouble(m.group(4));
        }

        downloadedBytes = Long.parseLong(m.group(5));

        if (m.group(6).equals("NA")) {
            totalBytes = -1;
        } else {
            totalBytes = Long.parseLong(m.group(6));
        }
    }

    private void parsePostprocessing(Matcher m) {
        status = m.group(1);
        postprocessor = m.group(2);
        timeElapsed = -1;
        eta = -1;
        speed = -1;
        downloadedBytes = -1;
        totalBytes = -1;
    }

    @Override
    public synchronized void setCanceled() {
        canceled = true;
    }

    @Override
    public synchronized void reset() {
        status = null;
        postprocessor = null;
        canceled = false;
        timeElapsed = -1;
        eta = -1;
        speed = -1;
        downloadedBytes = -1;
        totalBytes = -1;
    }

    @Override
    public synchronized Component updateProgressComponent(Component component) {
        if (component instanceof AbstractProgressPanel panel) {
            panel.update(this);
            return panel;
        }
        return null;
    }

    public synchronized boolean isCanceled() {
        return canceled;
    }

    public synchronized String getStatus() {
        return status;
    }

    public synchronized String getPostprocessor() {
        return postprocessor;
    }

    public synchronized long getTimeElapsed() {
        return timeElapsed;
    }

    public synchronized double getSpeed() {
        return speed;
    }

    public synchronized long getETA() {
        return eta;
    }

    public synchronized long getDownloadedBytes() {
        return downloadedBytes;
    }

    public synchronized long getTotalBytes() {
        return totalBytes;
    }

}
