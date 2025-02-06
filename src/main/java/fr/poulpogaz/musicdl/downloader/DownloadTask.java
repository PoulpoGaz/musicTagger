package fr.poulpogaz.musicdl.downloader;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DownloadTask {

    private static final Logger LOGGER = LogManager.getLogger(DownloadTask.class);
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    final int id = ID_GENERATOR.getAndIncrement();
    final AtomicReference<State> state = new AtomicReference<>(State.CREATED);

    final MultiValuedMap<EventThread, DownloadListener> listeners = new ArrayListValuedHashMap<>();

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
            if (curr.isCancelable()) {
                return State.CANCELED;
            } else {
                return curr;
            }
        });

        if (oldState.isCancelable()) {
            LOGGER.debug("canceling task {}", id);

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

    public final State getStateImmediately() {
        return state.getPlain();
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

    public int getID() {
        return id;
    }
 }
