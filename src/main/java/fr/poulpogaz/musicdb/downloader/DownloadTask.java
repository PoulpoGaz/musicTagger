package fr.poulpogaz.musicdb.downloader;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DownloadTask implements Callable<Void> {

    private static final AtomicInteger idGen = new AtomicInteger();
    private final AtomicBoolean canceled = new AtomicBoolean(false);

    Downloader downloader;
    final MultiValuedMap<EventThread, DownloadListener> listeners = new ArrayListValuedHashMap<>();

    final int id = idGen.getAndIncrement();


    @Override
    public abstract Void call() throws Exception;

    protected abstract void cancelImpl();

    public abstract String getDescription();

    public abstract Progress getProgress();



    public void addListener(DownloadListener listener) {
        addListener(EventThread.SAME_THREAD, listener);
    }

    public void addListener(EventThread executor, DownloadListener listener) {
        listeners.put(Objects.requireNonNull(executor), listener);
    }

    public void removeListener(EventThread thread, DownloadListener listener) {
        listeners.removeMapping(thread, listener);
    }

    public final void cancel() {
        if (!canceled.getAndSet(true)) {
            DownloadManager.fireEvent(DownloadListener.Event.CANCELED, this);
            cancelImpl();
        }
    }

    public final boolean isCanceled() {
        return canceled.get();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof DownloadTask task)) return false;

        return id == task.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
