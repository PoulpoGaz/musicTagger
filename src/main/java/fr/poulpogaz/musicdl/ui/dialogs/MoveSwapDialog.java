package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.model.Music;
import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.model.Templates;
import fr.poulpogaz.musicdl.opus.OpusFile;
import fr.poulpogaz.musicdl.ui.MusicdlFrame;
import fr.poulpogaz.musicdl.ui.layout.HCOrientation;
import fr.poulpogaz.musicdl.ui.layout.HorizontalConstraint;
import fr.poulpogaz.musicdl.ui.layout.HorizontalLayout;
import fr.poulpogaz.musicdl.ui.text.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MoveSwapDialog extends AbstractDialog {

    public static void showDialog(Frame owner) {
        new MoveSwapDialog(owner).setVisible(true);
    }

    private SelectComponent selectComponent;
    private JTextField from;
    private JTextField to;
    private JCheckBox swap;

    private JButton apply;
    private JButton cancel;

    private JProgressBar progress;

    private Worker worker;

    public MoveSwapDialog(Frame owner) {
        super(owner, "Move/swap metadata", true);
        init();
    }

    @Override
    protected float widthScaleFactor() {
        return 1;
    }

    @Override
    protected void initComponents() {
        selectComponent = new SelectComponent();
        from = createMetadataFieldTextField();
        to = createMetadataFieldTextField();
        swap = new JCheckBox("Swap values");

        progress = new JProgressBar();
        progress.setVisible(false);
        progress.setStringPainted(true);
        progress.setString("Musics ? of ?");

        cancel = new JButton("Cancel");
        cancel.addActionListener(_ -> cancel());
        apply = new JButton("Apply");
        apply.addActionListener(_ -> apply());

        JPanel top = new JPanel();
        top.setLayout(new GridBagLayout());
        top.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        GridBagConstraints c = new GridBagConstraints();

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        top.add(TextUtils.titledSeparator("Select musics"), c);

        c.gridy++;
        top.add(selectComponent, c);


        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.gridy++;
        top.add(TextUtils.titledSeparator("Operation"), c);

        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        c.insets.right = 5;
        top.add(new JLabel("From:"), c);

        c.gridx = 1;
        c.weightx = 1;
        top.add(from, c);

        c.gridx = 2;
        c.weightx = 0;
        top.add(new JLabel("To:"), c);

        c.gridx = 3;
        c.weightx = 1;
        top.add(to, c);

        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 0;
        c.insets.right = 0;
        top.add(swap, c);


        c.insets = new Insets(10, 15, 10, 15);
        c.gridy++;
        top.add(progress, c);


        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 0;
        c.gridy++;
        c.weighty = 1;
        top.add(Box.createVerticalGlue(), c);


        JPanel south = new JPanel();
        south.setLayout(new HorizontalLayout(4));
        HorizontalConstraint hc = new HorizontalConstraint();
        hc.orientation = HCOrientation.RIGHT;
        south.add(cancel, hc);
        south.add(apply, hc);

        setLayout(new BorderLayout());
        add(top, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(apply);
    }

    private JTextField createMetadataFieldTextField() {
        JTextField field = new JTextField();
        ((PlainDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string,
                                     AttributeSet attr) throws BadLocationException {
                super.insertString(fb, offset, OpusFile.reduce(string), attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text,
                                AttributeSet attrs) throws BadLocationException {
                super.replace(fb, offset, length, OpusFile.reduce(text), attrs);
            }
        });

        return field;
    }

    private void cancel() {
        if (worker != null) {
            worker.cancel(false);
        }
        dispose();
    }

    private void apply() {
        if (worker == null && !from.getText().equals(to.getText())) {
            selectComponent.setEnabled(false);
            from.setEnabled(false);
            to.setEnabled(false);
            swap.setEnabled(false);

            progress.setVisible(true);

            worker = new Worker();
            worker.execute();
        }
    }

    private class Worker extends SwingWorker<Void, Integer> {

        private static final Logger LOGGER = LogManager.getLogger(Worker.class);

        @Override
        protected Void doInBackground() {
            int total = selectComponent.countMusics();
            LOGGER.debug(total);
            publish(total);

            String from = MoveSwapDialog.this.from.getText();
            String to = MoveSwapDialog.this.to.getText();

            long lastTime = System.currentTimeMillis();
            int processed = 0;
            Iterator<Music> it = selectComponent.iterator();
            while (it.hasNext()) {
                Music music = it.next();

                if (swap.isSelected()) {
                    List<String> src = music.removeMetadata(from);
                    List<String> dst = music.removeMetadata(to);
                    if (src != null) {
                        music.putAllMetadata(to, src);
                    }
                    if (dst != null) {
                        music.putAllMetadata(from, dst);
                    }
                } else {
                    List<String> src = music.removeMetadata(from);
                    List<String> dst = music.getMetadata(to);
                    dst.clear();
                    dst.addAll(src);
                }

                processed++;

                if (System.currentTimeMillis() - lastTime > 200) {
                    publish(processed);
                    lastTime = System.currentTimeMillis();
                }
            }

            return null;
        }

        private boolean maxSet = false;

        @Override
        protected void process(List<Integer> chunks) {
            if (maxSet) {
                setProgressBar(chunks.getLast());
            } else {
                maxSet = true;
                progress.setMaximum(chunks.getFirst());

                if (chunks.size() > 1) {
                    setProgressBar(chunks.getLast());
                } else {
                    setProgressBar(0);
                }
            }
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                LOGGER.debug("Move/swap cancelled");
            } else {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.debug("Failed to move/swap", e);
                }

                setProgressBar(progress.getMaximum());
                dispose();

                for (Template t : Templates.getTemplates()) {
                    t.getData().notifyChanges();
                }
            }
        }

        private void setProgressBar(int value) {
            progress.setValue(value);
            progress.setString("Music " + value + " of " + progress.getMaximum());
        }
    }
}
