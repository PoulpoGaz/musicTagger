package fr.poulpogaz.musictagger.ui;

import fr.poulpogaz.musictagger.model.CoverArt;
import fr.poulpogaz.musictagger.model.Music;
import fr.poulpogaz.musictagger.model.Template;
import fr.poulpogaz.musictagger.model.Templates;
import fr.poulpogaz.musictagger.opus.OpusFile;
import fr.poulpogaz.musictagger.ui.dialogs.MoveSwapDialog;
import org.apache.commons.collections4.MapIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class Actions {

    private static Action SAVE_ACTION;
    private static Action MOVE_METADATA_ACTION;

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
            SAVE_ACTION.putValue(Action.SHORT_DESCRIPTION, "Save all modified and downloaded musics");
        }

        return SAVE_ACTION;
    }

    private static class SaveWorker extends SwingWorker<Void, Void> {

        private static final Logger LOGGER = LogManager.getLogger(SaveWorker.class);

        private final Queue<Music> toProcess = new ConcurrentLinkedQueue<>();
        private boolean allAdded = false;

        @Override
        protected Void doInBackground() {
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

                    for (CoverArt cover : m.getCoverArts()) {
                        file.addCoverArt(cover);
                    }

                    Template t = m.getTemplate();
                    if (t != null) {
                        file.put("TEMPLATE", t.getName());
                    }

                    try {
                        file.save();
                    } catch (Exception e) {
                        LOGGER.error("Failed to save music to {}", file.getPath(), e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void done() {
            try {
                get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Failed to save musics", e);
            }
        }

        public void offer(Music music) {
            toProcess.offer(music);
        }

        public void allMusicsAdded() {
            allAdded = true;
        }
    }





    public static Action moveMetadata() {
        if (MOVE_METADATA_ACTION == null) {
            MOVE_METADATA_ACTION = new AbstractAction("Move metadata") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MoveSwapDialog.showDialog(MTFrame.getInstance());
                }
            };

        }
        return MOVE_METADATA_ACTION;
    }
}
