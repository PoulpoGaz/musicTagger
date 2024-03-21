package fr.poulpogaz.musicdb.downloader;

import fr.poulpogaz.musicdb.model.Music;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class DownloadManager {

    public static final int DEFAULT_THREAD_COUNT = 2;


    static {
        ThreadFactory factory = Thread.ofVirtual().name("DownloadTask-", 1).factory();
        executor = Executors.newThreadPerTaskExecutor(factory);
    }




    private static final ExecutorService executor;
    private static final Semaphore sem = new Semaphore(DEFAULT_THREAD_COUNT);

    private static final Object LOCK = new Object();
    private static int maxThreadCount = DEFAULT_THREAD_COUNT;

    private static final Set<DownloadTask> tasks = new HashSet<>();


    private static final MultiValuedMap<EventThread, DownloadListener> listeners = new ArrayListValuedHashMap<>();

    private static Path downloadRoot = Path.of(System.getProperty("user.dir"));




    public static void setMaxThreadCount(int threads) {
        synchronized (LOCK) {
            if (threads > 0) {
                if (maxThreadCount > threads) {
                    sem.reducePermits(maxThreadCount - threads);
                } else {
                    sem.release(threads - maxThreadCount);
                }

                maxThreadCount = threads;
            }
        }
    }

    public static int getMaxThreadCount() {
        return maxThreadCount;
    }

    public static void shutdown() {
        executor.shutdown();
    }


    public static DownloadTask download(Music music) {
        DownloadTask task = new SingleMusicDownloadTask(music);

        fireEvent(DownloadListener.Event.QUEUED, task);
        pushTask(task);

        return task;
    }

    private static void pushTask(DownloadTask task) {
        tasks.add(task);
        executor.submit(new Worker(task));
    }



    public static void cancelAll() {
        for (DownloadTask task : tasks) {
            task.cancel();
        }
    }





    public static void addListener(DownloadListener listener) {
        addListener(EventThread.SAME_THREAD, listener);
    }

    public static void addListener(EventThread thread, DownloadListener listener) {
        listeners.put(thread, listener);
    }

    public static void removeListener(EventThread thread, DownloadListener listener) {
        listeners.removeMapping(thread, listener);
    }



    public static boolean isDownloading() {
        return !tasks.isEmpty();
    }

    public static boolean isQueueEmpty() {
        return tasks.size() - maxThreadCount < 0;
    }



    static void fireEvent(DownloadListener.Event event, DownloadTask task) {
        Collection<DownloadListener> same1 = listeners.get(EventThread.SAME_THREAD);
        Collection<DownloadListener> same2 = task.listeners.get(EventThread.SAME_THREAD);
        fireEvent(same1, same2, event, task);

        Collection<DownloadListener> swing1 = listeners.get(EventThread.SWING_THREAD);
        Collection<DownloadListener> swing2 = task.listeners.get(EventThread.SWING_THREAD);
        SwingUtilities.invokeLater(() -> fireEvent(swing1, swing2, event, task));

        fireEvent(listeners.mapIterator(), event, task);
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

    private static class Worker implements Callable<Void> {

        private static final Logger LOGGER = LogManager.getLogger(Worker.class);

        private final Downloader downloader = new Downloader();
        private final DownloadTask task;

        public Worker(DownloadTask task) {
            this.task = task;
        }

        @Override
        public Void call() throws InterruptedException {
            sem.acquire();

            try {
                if (!task.isCanceled()) {
                    fireEvent(DownloadListener.Event.STARTED, task);

                    try {
                        task.downloader = downloader;
                        task.call();
                    } catch (Exception e) {
                        LOGGER.warn("Task {} throws an exception", task, e);
                        fireEvent(DownloadListener.Event.FAILED, task);
                    }

                    fireEvent(DownloadListener.Event.FINISHED, task);
                }
            } finally {
                tasks.remove(task);
                sem.release();
            }

            return null;
        }
    }


    private static class Semaphore extends java.util.concurrent.Semaphore {

        public Semaphore(int permits) {
            super(permits);
        }

        @Override
        protected void reducePermits(int reduction) {
            super.reducePermits(reduction);
        }
    }
}
