package fr.poulpogaz.musicdb.downloader;

import fr.poulpogaz.musicdb.BasicObjectPool;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.SignStyle;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DownloadTask implements Callable<Void> {

    private static final Logger LOGGER = LogManager.getLogger(DownloadTask.class);
    private static final BasicObjectPool<Downloader> POOL = BasicObjectPool.supplierPool(Downloader::new);
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    private final int id = ID_GENERATOR.getAndIncrement();
    private final AtomicReference<State> state = new AtomicReference<>(State.QUEUED);

    protected Downloader downloader;
    final MultiValuedMap<EventThread, DownloadListener> listeners = new ArrayListValuedHashMap<>();

    @Override
    public final Void call() throws InterruptedException {
        LOGGER.debug("task called {}", id);
        DownloadManager.THREAD_GUARD.acquire();
        LOGGER.debug("starting {}", id);

        try {
            if (state.compareAndSet(State.QUEUED, State.RUNNING)) {
                DownloadManager.fireEvent(DownloadListener.Event.STARTED, this);

                downloader = POOL.get();
                try {
                    download();
                } catch (Exception e) {
                    LOGGER.warn("Task {} throws an exception", this, e);
                    if (state.compareAndSet(State.RUNNING, State.FAILED)) {
                        DownloadManager.fireEvent(DownloadListener.Event.FAILED, this);
                    }
                }
                POOL.free(downloader);
                downloader = null;

                if (state.compareAndSet(State.RUNNING, State.FINISHED)) {
                    DownloadManager.fireEvent(DownloadListener.Event.FINISHED, this);
                }
            }
        } finally {
            DownloadManager.TASKS.remove(this);
            DownloadManager.THREAD_GUARD.release();
        }

        LOGGER.debug("finish {}", id);

        return null;
    }

    public abstract void download() throws Exception;

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
        State oldState = state.getAndUpdate((curr) -> {
            if (curr == State.QUEUED || curr == State.RUNNING) {
                return State.CANCELED;
            } else {
                return curr;
            }
        });

        if (oldState == State.QUEUED || oldState == State.RUNNING) {
            cancelImpl();
            DownloadManager.fireEvent(DownloadListener.Event.CANCELED, this);
        }
    }

    public final boolean isCanceled() {
        return state.get() == State.CANCELED;
    }

    public final State getState() {
        return state.get();
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
