package fr.poulpogaz.musictagger.ui.dialogs;

import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.icons.FlatTreeOpenIcon;
import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.poulpogaz.musictagger.model.Music;
import fr.poulpogaz.musictagger.ui.layout.HCOrientation;
import fr.poulpogaz.musictagger.ui.layout.HorizontalConstraint;
import fr.poulpogaz.musictagger.ui.layout.HorizontalLayout;
import fr.poulpogaz.musictagger.ui.text.MTextField;
import fr.poulpogaz.musictagger.ui.text.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ExportDialog extends AbstractDialog {

    private static final FileNameExtensionFilter JSON_FILTER = new FileNameExtensionFilter("JSON files", "json");

    public static void showDialog(Frame owner) {
        new ExportDialog(owner).setVisible(true);
    }

    private JPanel top;

    private SelectComponent selectComponent;

    private JCheckBox saveImage;
    private JCheckBox inJSON;
    private JCheckBox inPNG;
    private JTextField pngFormat;

    private MTextField output;

    private JButton cancel;
    private JButton export;

    private JProgressBar progress;

    private Worker worker;
    private boolean done;

    public ExportDialog(Frame owner) {
        super(owner, "Export", true);
        init();
    }

    @Override
    protected float widthScaleFactor() {
        return 1f;
    }

    @Override
    protected void initComponents() {
        top = new JPanel();
        top.setLayout(new GridBagLayout());
        top.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        selectComponent = new SelectComponent();

        saveImage = new JCheckBox("Save cover arts");
        inJSON = new JCheckBox("In JSON (base 64)", true);
        inJSON.setEnabled(false);
        inPNG = new JCheckBox("In PNG");
        inPNG.setEnabled(false);
        pngFormat = new JTextField("cover_arts/{id}.png");
        pngFormat.setEnabled(false);
        saveImage.addActionListener(_ -> {
            inJSON.setEnabled(saveImage.isSelected());
            inPNG.setEnabled(saveImage.isSelected());
            pngFormat.setEnabled(saveImage.isSelected() && inPNG.isSelected());
        });
        inPNG.addActionListener(_ -> pngFormat.setEnabled(saveImage.isSelected() && inPNG.isSelected()));

        FlatButton open = new FlatButton();
        open.setIcon(new FlatTreeOpenIcon());
        open.setToolTipText("Choose a file");
        open.setButtonType(FlatButton.ButtonType.toolBarButton);
        open.addActionListener(this::openFileChooser);
        output = new MTextField(Dialogs.WORKING_DIRECTORY.toPath().resolve("musics.json").toString());
        output.setTrailingComponent(open);

        progress = new JProgressBar();
        progress.setVisible(false);
        progress.setStringPainted(true);
        progress.setString("Musics ? of ?");


        cancel = new JButton("Cancel");
        cancel.addActionListener(_ -> cancel());
        export = new JButton("Export");
        export.addActionListener(_ -> exportOrDone());


        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });




        GridBagConstraints c = new GridBagConstraints();

        /* Musics to export */

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        top.add(TextUtils.titledSeparator("Musics to export"), c);

        c.gridy++;
        top.add(selectComponent, c);

        /* Options */

        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.gridy++;
        top.add(TextUtils.titledSeparator("Options"), c);

        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE;
        c.gridy++;
        top.add(saveImage, c);
        c.gridy++;
        top.add(inJSON, c);

        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        top.add(inPNG, c);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        top.add(pngFormat, c);

        c.gridx = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.gridwidth = 2;
        c.gridy++;
        top.add(new JLabel("Output:"), c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridy++;
        top.add(output, c);

        /* Progress bar + vertical glue */

        c.insets = new Insets(10, 15, 10, 15);
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy++;
        top.add(progress, c);

        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy++;
        c.weighty = 1;
        top.add(Box.createVerticalGlue(), c);


        JPanel south = new JPanel();
        south.setLayout(new HorizontalLayout(4));
        HorizontalConstraint hc = new HorizontalConstraint();
        hc.orientation = HCOrientation.RIGHT;
        south.add(cancel, hc);
        south.add(export, hc);

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(top, BorderLayout.CENTER);
        content.add(south, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(export);
    }

    private void openFileChooser(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(JSON_FILTER);

        File f = new File(output.getText());
        while (!f.exists() || !f.isDirectory()) {
            File next = f.getParentFile();
            if (next == f) break;
            f = next;
        }

        chooser.setCurrentDirectory(f);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            output.setText(file.toString());
        }
    }

    private void exportOrDone() {
        if (done) {
            dispose();
        } else {
            selectComponent.setEnabled(false);
            saveImage.setEnabled(false);
            inJSON.setEnabled(false);
            pngFormat.setEnabled(false);
            output.setEnabled(false);
            export.setEnabled(false);

            progress.setVisible(true);
            worker = new Worker();
            worker.execute();
        }
    }

    private void cancel() {
        if (worker != null) {
            worker.cancel(false);
        }
        dispose();
    }

    private class Worker extends SwingWorker<Void, Integer> {

        private static final Logger LOGGER = LogManager.getLogger(Worker.class);

        @Override
        protected Void doInBackground() throws Exception {
            int total = selectComponent.countMusics();
            publish(total);

            LOGGER.debug("{} musics to export", total);

            if (total == 0) {
                return null;
            }

            Path out = Path.of(output.getText());
            try (BufferedWriter bw = Files.newBufferedWriter(out)) {
                IJsonWriter jw = new JsonPrettyWriter(bw);
                jw.beginArray();

                String coverOut = pngFormat.getText();
                long lastTime = System.currentTimeMillis();
                int processed = 0;
                Iterator<Music> it = selectComponent.iterator();
                while (!worker.isCancelled() && it.hasNext()) {
                    Music m = it.next();

                    Path coverOutput = null;
                    if (saveImage.isSelected() && inPNG.isSelected()) {
                        coverOutput = Path.of(coverOut.replace("{id}", Integer.toString(processed)));

                        Path parent = coverOutput.getParent();
                        if (parent != null) {
                            Files.createDirectories(parent);
                        }
                    }
                    m.writeTo(jw, saveImage.isSelected() && inJSON.isSelected(), coverOutput);
                    processed++;

                    if (System.currentTimeMillis() - lastTime > 200) {
                        publish(processed);
                        lastTime = System.currentTimeMillis();
                    }
                }

                jw.endArray();
                jw.close();
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
                LOGGER.debug("Export cancelled");
            } else {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.debug("Failed to export", e);
                }

                setProgressBar(progress.getMaximum());

                done = true;
                export.setEnabled(true);
                export.setText("Done");
            }
        }

        private void setProgressBar(int value) {
            progress.setValue(value);
            progress.setString("Music " + value + " of " + progress.getMaximum());
        }
    }
}
