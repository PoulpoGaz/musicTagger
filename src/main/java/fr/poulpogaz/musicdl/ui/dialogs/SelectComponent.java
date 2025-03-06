package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.model.Music;
import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.model.Templates;
import fr.poulpogaz.musicdl.ui.MusicdlFrame;
import fr.poulpogaz.musicdl.ui.TemplatesPanel;
import org.apache.commons.collections4.iterators.FilterIterator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;

public class SelectComponent extends JComponent {

    private JCheckBox all;
    private JCheckBox allSelected;
    private JCheckBox allFromTemplate;
    private JCheckBox allSelectedFromTemplate;

    private JComboBox<Template> templateComboBox;

    private JCheckBox skipDownloaded;

    public SelectComponent() {
        initComponent();
    }

    private void initComponent() {
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


        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 2;
        c.gridy++;
        add(all, c);
        c.gridy++;
        add(allSelected, c);

        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        add(allFromTemplate, c);

        c.gridx = 1;
        c.gridheight = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(templateComboBox, c);

        c.gridx = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.gridy++;
        add(allSelectedFromTemplate, c);

        c.gridwidth = 2;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy++;
        add(skipDownloaded, c);
    }

    private void updateTemplateComboBox(ActionEvent e) {
        templateComboBox.setEnabled(allFromTemplate.isSelected() || allSelectedFromTemplate.isSelected());
    }

    @Override
    public void setEnabled(boolean enabled) {
        all.setEnabled(false);
        allSelected.setEnabled(false);
        allFromTemplate.setEnabled(false);
        allSelectedFromTemplate.setEnabled(false);
        templateComboBox.setEnabled(false);
        skipDownloaded.setEnabled(false);
        super.setEnabled(enabled);
    }

    public Iterator<Music> iterator() {
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

    public int countMusics() {
        Template template = getSelectedTemplate();

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

    public Template getSelectedTemplate() {
        return templateComboBox.isEnabled() ? (Template) templateComboBox.getSelectedItem() : null;
    }

    public boolean all() {
        return all.isSelected();
    }

    public boolean allSelected() {
        return allSelected.isSelected();
    }

    public boolean allFromTemplate() {
        return allFromTemplate.isSelected();
    }

    public boolean allSelectedFromTemplate() {
        return allSelectedFromTemplate.isEnabled();
    }

    public boolean skipDownloaded() {
        return skipDownloaded.isSelected();
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
