package fr.poulpogaz.musicdb.downloader;

public interface DownloadListener {

    enum Event {
        QUEUED,
        STARTED,
        FINISHED,
        CANCELED,
        FAILED
    }

    void onEvent(Event event, DownloadTask task);
}
