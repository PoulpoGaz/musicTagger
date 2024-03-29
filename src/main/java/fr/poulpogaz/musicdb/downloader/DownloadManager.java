package fr.poulpogaz.musicdb.downloader;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DownloadManager {

    public static final int DEFAULT_THREAD_COUNT = 2;


    static {
        ThreadFactory factory = Thread.ofVirtual().name("DownloadTask-", 0).factory();
        EXECUTOR = Executors.newThreadPerTaskExecutor(factory);
    }




    private static final ExecutorService EXECUTOR;
    static final Semaphore THREAD_GUARD = new Semaphore(DEFAULT_THREAD_COUNT);
    static final Set<DownloadTask> TASKS = Collections.synchronizedSet(new HashSet<>());

    private static final Object LOCK = new Object();
    private static int maxThreadCount = DEFAULT_THREAD_COUNT;



    private static final MultiValuedMap<EventThread, DownloadListener> LISTENERS = new ArrayListValuedHashMap<>();

    private static Path downloadRoot = Path.of(System.getProperty("user.dir"));




    public static void setMaxThreadCount(int threads) {
        synchronized (LOCK) {
            if (threads > 0) {
                if (maxThreadCount > threads) {
                    THREAD_GUARD.reducePermits(maxThreadCount - threads);
                } else {
                    THREAD_GUARD.release(threads - maxThreadCount);
                }

                maxThreadCount = threads;
            }
        }
    }

    public static int getMaxThreadCount() {
        return maxThreadCount;
    }


    public static void offer(DownloadTask task) {
        if (task != null && task.state.compareAndSet(State.CREATED, State.QUEUED)) {
            TASKS.add(task);
            EXECUTOR.submit(task);
        }
    }



    public static void cancelAll() {
        for (DownloadTask task : TASKS) {
            task.cancel();
        }
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
    }





    public static void addListener(DownloadListener listener) {
        addListener(EventThread.SAME_THREAD, listener);
    }

    public static void addListener(EventThread thread, DownloadListener listener) {
        LISTENERS.put(thread, listener);
    }

    public static void removeListener(EventThread thread, DownloadListener listener) {
        LISTENERS.removeMapping(thread, listener);
    }



    public static boolean isDownloading() {
        return !TASKS.isEmpty();
    }

    public static boolean isQueueEmpty() {
        return TASKS.size() - maxThreadCount < 0;
    }



    static void fireEvent(DownloadListener.Event event, DownloadTask task) {
        Collection<DownloadListener> same1 = LISTENERS.get(EventThread.SAME_THREAD);
        Collection<DownloadListener> same2 = task.listeners.get(EventThread.SAME_THREAD);
        fireEvent(same1, same2, event, task);

        Collection<DownloadListener> swing1 = LISTENERS.get(EventThread.SWING_THREAD);
        Collection<DownloadListener> swing2 = task.listeners.get(EventThread.SWING_THREAD);
        SwingUtilities.invokeLater(() -> fireEvent(swing1, swing2, event, task));

        fireEvent(LISTENERS.mapIterator(), event, task);
        fireEvent(task.listeners.mapIterator(), event, task);
    }

    private static void fireEvent(MapIterator<EventThread, DownloadListener> it,
                                  DownloadListener.Event event,
                                  DownloadTask task) {
        while (it.hasNext()) {
            EventThread t = it.next();

            if (t.type() == EventThread.Type.EXECUTOR) {
                t.executor().execute(() -> it.getValue().onEvent(event, task));
            }
        }
    }

    private static void fireEvent(Collection<DownloadListener> c1,
                                  Collection<DownloadListener> c2,
                                  DownloadListener.Event event,
                                  DownloadTask task) {
        c1.forEach(l -> l.onEvent(event, task));
        c2.forEach(l -> l.onEvent(event, task));
    }


    public static Path getDownloadRoot() {
        return downloadRoot;
    }

    public static void setDownloadRoot(Path downloadRoot) {
        DownloadManager.downloadRoot = downloadRoot;
    }

    static class Semaphore extends java.util.concurrent.Semaphore {

        public Semaphore(int permits) {
            super(permits, true);
        }

        @Override
        protected void reducePermits(int reduction) {
            super.reducePermits(reduction);
        }
    }
}
