package fr.poulpogaz.musicdl.ui;

import fr.poulpogaz.musicdl.model.CoverArt;
import fr.poulpogaz.musicdl.model.Music;
import fr.poulpogaz.musicdl.model.Templates;
import fr.poulpogaz.musicdl.opus.OpusFile;
import org.apache.commons.collections4.MapIterator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Actions {

    private static Action SAVE_ACTION;

    public static Action saveAction() {
        if (SAVE_ACTION == null) {
            SAVE_ACTION = new AbstractAction("Save", Icons.get("save.svg")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SaveWorker worker = new SaveWorker();
                    worker.execute();
                    for (Iterator<Music> it = Templates.allMusicsIterator(); it.hasNext(); ) {
                        Music m = it.next();

                        if (m.hasChanged()) {
                            worker.offer(m);
                        }
                    }
                    worker.allMusicsAdded();
                }
            };
        }

        return SAVE_ACTION;
    }

    private static class SaveWorker extends SwingWorker<Void, Void> {

        private final Queue<Music> toProcess = new ConcurrentLinkedQueue<>();
        private boolean allAdded = false;

        @Override
        protected Void doInBackground() throws Exception {
            while (!allAdded) {
                Music m = toProcess.poll();
                if (m != null) {
                    OpusFile file = m.getOpusFile();
                    file.clearCoverArt();
                    file.clear();

                    for (MapIterator<String, String> it = m.metadataIterator(); it.hasNext(); ) {
                        it.next();
                        file.put(it.getKey(), it.getValue());
                    }

                    for (CoverArt cover : m.getCovers()) {
                        file.addCoverArt(cover);
                    }

                    file.save();
                }
            }

            return null;
        }

        public void offer(Music music) {
            toProcess.offer(music);
        }

        public void allMusicsAdded() {
            allAdded = true;
        }
    }
}
