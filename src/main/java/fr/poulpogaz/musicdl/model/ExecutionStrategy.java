package fr.poulpogaz.musicdl.model;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public interface ExecutionStrategy {

    static ExecutionStrategy sameThread() {
        return SameThreadStrategy.INSTANCE;
    }

    static ExecutionStrategy eventQueue() {
        return EventQueueStrategy.INSTANCE;
    }




    void execute(List<CoverArtCallback> callbacks, BufferedImage image, Exception exc);

    class SameThreadStrategy implements ExecutionStrategy {

        private static final SameThreadStrategy INSTANCE = new SameThreadStrategy();
        private SameThreadStrategy() {}


        @Override
        public void execute(List<CoverArtCallback> callbacks, BufferedImage image, Exception exc) {
            for (CoverArtCallback callback : callbacks) {
                callback.onImageLoad(image, exc);
            }
        }
    }

    class EventQueueStrategy implements ExecutionStrategy {

        private static final EventQueueStrategy INSTANCE = new EventQueueStrategy();
        private EventQueueStrategy() {}


        private List<Runnable> runnable;

        @Override
        public synchronized void execute(List<CoverArtCallback> callbacks, BufferedImage image, Exception exc) {
            boolean submit = false;
            if (runnable == null) {
                runnable = new ArrayList<>();
                submit = true;
            }
            for (CoverArtCallback callback : callbacks) {
                runnable.add(() -> callback.onImageLoad(image, exc));
            }
            if (submit) {
                SwingUtilities.invokeLater(this::run);
            }
        }

        private void run() {
            List<Runnable> runnable;
            synchronized (this) {
                runnable = this.runnable;
                this.runnable = null;
            }

            runnable.forEach(Runnable::run);
        }
    }
}