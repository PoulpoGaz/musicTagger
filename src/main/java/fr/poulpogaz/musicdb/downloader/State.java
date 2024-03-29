package fr.poulpogaz.musicdb.downloader;

public enum State {

    CREATED(true),  // task is created but not queued. initial state
    QUEUED(true),
    RUNNING(true),
    CANCELED(false),
    FAILED(false),
    FINISHED(false);

    private final boolean cancelable;

    State(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public boolean isCancelable() {
        return cancelable;
    }
}
