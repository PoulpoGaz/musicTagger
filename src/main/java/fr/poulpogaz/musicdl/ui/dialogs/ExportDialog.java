package fr.poulpogaz.musicdl.ui.dialogs;

import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.icons.FlatTreeOpenIcon;
import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.poulpogaz.musicdl.model.Music;
import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.model.Templates;
import fr.poulpogaz.musicdl.ui.MusicdlFrame;
import fr.poulpogaz.musicdl.ui.TemplatesPanel;
import fr.poulpogaz.musicdl.ui.layout.HCOrientation;
import fr.poulpogaz.musicdl.ui.layout.HorizontalConstraint;
import fr.poulpogaz.musicdl.ui.layout.HorizontalLayout;
import fr.poulpogaz.musicdl.ui.text.MTextField;
import org.apache.commons.collections4.iterators.FilterIterator;
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
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ExportDialog extends AbstractDialog {

    private static final FileNameExtensionFilter JSON_FILTER = new FileNameExtensionFilter("JSON files", "json");

    public static void showDialog(Frame owner) {
        new ExportDialog(owner).setVisible(true);
    }

    private JPanel top;

    private JCheckBox all;
    private JCheckBox allSelected;
    private JCheckBox allFromTemplate;
    private JCheckBox allSelectedFromTemplate;

    private JComboBox<Template> templateComboBox;

    private JCheckBox skipDownloaded;

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

        all = new JCheckBox("All", true);
        allSelected = new JCheckBox("All selected");
        allFromTemplate = new JCheckBox("All from template:");
        allSelectedFromTemplate = new JCheckBox("All selected from template:");
        all.addActionListener(this::updateTemplateComboBox);
        allSelected.addActionListener(this::updateTemplateComboBox);
        allFromTemplate.addActionListener(this::updateTemplateComboBox);
        allSelectedFromTemplate.addActionListener(this::updateTemplateComboBox);

        ButtonGroup group = new ButtonGroup();
        group.add(all);
        group.add(allSelected);
        group.add(allFromTemplate);
        group.add(allSelectedFromTemplate);

        templateComboBox = new JComboBox<>(new TemplateComboBoxModel());
        templateComboBox.setEnabled(false);
        templateComboBox.setSelectedIndex(0);
        templateComboBox.setRenderer(new TemplateCellRenderer());

        skipDownloaded = new JCheckBox("Skip downloaded musics");

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
        top.add(titledSeparator("Musics to export"), c);

        c.weightx = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 2;
        c.gridy++;
        top.add(all, c);
        c.gridy++;
        top.add(allSelected, c);

        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        top.add(allFromTemplate, c);

        c.gridx = 1;
        c.gridheight = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        top.add(templateComboBox, c);

        c.gridx = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridy++;
        top.add(allSelectedFromTemplate, c);

        c.gridwidth = 2;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy++;
        top.add(skipDownloaded, c);


        /* Options */

        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.gridy++;
        top.add(titledSeparator("Options"), c);

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

    private JComponent titledSeparator(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.weighty = 1;

        panel.add(new JLabel(title), c);
        c.gridx = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(2, 6, 0, 0);
        panel.add(new JSeparator(), c);

        return panel;
    }

    private void updateTemplateComboBox(ActionEvent e) {
        templateComboBox.setEnabled(allFromTemplate.isSelected() || allSelectedFromTemplate.isSelected());
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
            all.setEnabled(false);
            allSelected.setEnabled(false);
            allFromTemplate.setEnabled(false);
            allSelectedFromTemplate.setEnabled(false);
            templateComboBox.setEnabled(false);
            skipDownloaded.setEnabled(false);
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

    private static class TemplateComboBoxModel extends AbstractListModel<Template> implements ComboBoxModel<Template> {

        private Template selected;
        private final List<Template> templates = new ArrayList<>();

        public TemplateComboBoxModel() {
            templates.addAll(Templates.getTemplates());
            templates.sort(Comparator.comparing(Template::getName));
        }

        @Override
        public void setSelectedItem(Object anItem) {
            if (selected != anItem && anItem instanceof Template t) {
                this.selected = t;
                fireContentsChanged(this, -1, -1);
            }
        }

        @Override
        public Template getSelectedItem() {
            return selected;
        }

        @Override
        public int getSize() {
            return templates.size();
        }

        @Override
        public Template getElementAt(int index) {
            if (index >= 0 && index < templates.size()) {
                return templates.get(index);
            } else {
                return null;
            }
        }
    }

    private static class TemplateCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Template t) {
                setText(t.getName());
            }

            return this;
        }
    }

    private class Worker extends SwingWorker<Void, Integer> {

        private static final Logger LOGGER = LogManager.getLogger(Worker.class);

        @Override
        protected Void doInBackground() throws Exception {
            int total = countMusics();
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
                Iterator<Music> it = iterator();
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

        private int countMusics() {
            Template template = (Template) Objects.requireNonNull(templateComboBox.getSelectedItem());

            if (!skipDownloaded.isSelected() && all.isSelected()) {
                return Templates.totalMusicCount();
            } else if (!skipDownloaded.isSelected() && allFromTemplate.isSelected()) {
                return template.getData().getMusicCount();
            } else {
                Iterator<Music> m = iterator();

                int count = 0;
                while (m.hasNext()) {
                    m.next();
                    count++;
                }

                return count;
            }
        }

        private Iterator<Music> iterator() {
            Iterator<Music> it;
            if (all.isSelected()) {
                it = Templates.allMusicsIterator();
            } else if (allSelected.isSelected()) {
                it = new AllTemplateFilterIterator();
            } else if (templateComboBox.getSelectedItem() instanceof Template template) {
                if (allFromTemplate.isSelected()) {
                    it = template.getData().iterator();
                } else {
                    it = new TemplateFilterIterator(template,
                                                      MusicdlFrame.getInstance().getTemplatesPanel()
                                                                  .getTemplateTableFor(template).getSelectedRows());
                }
            } else {
                throw new IllegalStateException();
            }

            if (skipDownloaded.isSelected()) {
                return new FilterIterator<>(it, m -> !m.isDownloaded());
            } else {
                return it;
            }
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

    private static class AllTemplateFilterIterator implements Iterator<Music> {

        private final TemplatesPanel templates = MusicdlFrame.getInstance().getTemplatesPanel();
        private final TemplateFilterIterator it;
        private int templateIndex;

        public AllTemplateFilterIterator() {
            templateIndex = 0;
            it = new TemplateFilterIterator(templates.getTemplateTable(templateIndex).getTemplate(),
                                            templates.getTemplateTable(templateIndex).getSelectedRows());
        }

        @Override
        public boolean hasNext() {
            while (templateIndex >= 0 && templateIndex < templates.getTemplateTableCount()) {
                if (it.hasNext()) {
                    return true;
                } else {
                    templateIndex++;

                    if (templateIndex >= 0 && templateIndex < templates.getTemplateTableCount()) {
                        it.reset(templates.getTemplateTable(templateIndex).getTemplate(),
                                 templates.getTemplateTable(templateIndex).getSelectedRows());
                    }
                }
            }

            return false;
        }

        @Override
        public Music next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return it.next();
        }
    }

    private static class TemplateFilterIterator implements Iterator<Music> {

        private Template template;
        private ListSelectionModel selected;

        private int index;

        public TemplateFilterIterator(Template template, ListSelectionModel selected) {
            reset(template, selected);
        }

        public void reset(Template template, ListSelectionModel selected) {
            this.template = Objects.requireNonNull(template);
            this.selected = Objects.requireNonNull(selected);
            index = selected.getMinSelectionIndex();
        }

        @Override
        public boolean hasNext() {
            while (!selected.isSelectedIndex(index) && index <= selected.getMaxSelectionIndex()) {
                index++;
            }

            return index >= 0 && index <= selected.getMaxSelectionIndex();
        }

        @Override
        public Music next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return template.getData().getMusic(index++);
        }
    }
}
