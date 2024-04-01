package fr.poulpogaz.musicdb.downloader;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadManager {


    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Object LOCK = new Object();
    private static final Worker WORKER = new Worker();
    private static final Queue<DownloadTask> TASKS = new ConcurrentLinkedQueue<>();

    private static final MultiValuedMap<EventThread, DownloadListener> LISTENERS = new ArrayListValuedHashMap<>();

    private static Path downloadRoot = Path.of(System.getProperty("user.dir"));



    public static void offer(DownloadTask task) {
        if (task != null && task.state.compareAndSet(State.CREATED, State.QUEUED)) {
            fireEvent(DownloadListener.Event.QUEUED, task);

            TASKS.offer(task);

            synchronized (LOCK) {
                if (!WORKER.running) {
                    WORKER.running = true;
                    EXECUTOR.submit(WORKER);
                }
            }
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



    public static boolean isDownloading() {
        return WORKER.task != null;
    }

    public static boolean isQueueEmpty() {
        return TASKS.isEmpty();
    }

    public static DownloadTask getRunningTask() {
        return WORKER.task;
    }

    public static Collection<DownloadTask> getQueue() {
        return Collections.unmodifiableCollection(TASKS);
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



    private static class Worker implements Runnable {

        private static final Logger LOGGER = LogManager.getLogger(Worker.class);

        private boolean running = false;
        private DownloadTask task;

        @Override
        public void run() {
            while (true) {
                synchronized (LOCK) {
                    task = TASKS.poll();

                    if (task == null) {
                        running = false;
                        break;
                    }
                }

                LOGGER.debug("starting {}", task.id);

                if (task.state.compareAndSet(State.QUEUED, State.RUNNING)) {
                    DownloadManager.fireEvent(DownloadListener.Event.STARTED, task);

                    try {
                        task.download();
                    } catch (Exception e) {
                        LOGGER.warn("Task {} throws an exception", this, e);
                        if (task.state.compareAndSet(State.RUNNING, State.FAILED)) {
                            DownloadManager.fireEvent(DownloadListener.Event.FAILED, task);
                            LOGGER.debug("failed {}", task.id);
                        }
                    }

                    if (task.state.compareAndSet(State.RUNNING, State.FINISHED)) {
                        DownloadManager.fireEvent(DownloadListener.Event.FINISHED, task);
                        LOGGER.debug("finished {}", task.id);
                    }
                }
            }
        }
    }
}
