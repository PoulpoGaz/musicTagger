package fr.poulpogaz.musicdl.downloader;

import java.util.Objects;
import java.util.concurrent.Executor;

public record EventThread(Type type, Executor executor) {

    public static final EventThread SAME_THREAD = new EventThread(Type.SAME_THREAD, null);
    public static final EventThread SWING_THREAD = new EventThread(Type.SWING_THREAD, null);

    public EventThread(Type type, Executor executor) {
        if (type == Type.EXECUTOR) {
            Objects.requireNonNull(executor);
            this.executor = executor;
        } else {
            this.executor = null;
        }

        this.type = type;
    }

    public EventThread(Executor executor) {
        this(Type.EXECUTOR, executor);
    }

    public enum Type {
        SAME_THREAD,
        SWING_THREAD,
        EXECUTOR
    }

}
