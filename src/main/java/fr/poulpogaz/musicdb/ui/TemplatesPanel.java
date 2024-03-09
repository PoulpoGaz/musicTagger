package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.model.Template;
import fr.poulpogaz.musicdb.model.Templates;
import fr.poulpogaz.musicdb.model.TemplatesListener;
import fr.poulpogaz.musicdb.properties.Property;
import fr.poulpogaz.musicdb.properties.PropertyListener;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TemplatesPanel extends JPanel {

    private final JToolBar toolBar;
    private JButton insertMusic;
    private JButton removeMusic;
    private final JTabbedPane templatesPane;

    private final Map<Template, TemplateTable> panels = new HashMap<>();
    private final PropertyListener<String> templateNameListener = this::setTabName;

    public TemplatesPanel() {
        toolBar = createToolBar();
        templatesPane = createTemplatesPane();

        for (Template t : Templates.getTemplates()) {
            templatesPane.addTab(t.getName(), new TemplateTable(t));
        }
        if (templatesPane.getTabCount() > 0) {
            templatesPane.setSelectedIndex(0);
        }

        Templates.addTemplateListener(createTemplateListener());

        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.WEST);
        add(templatesPane, BorderLayout.CENTER);
    }

    private JToolBar createToolBar() {
        insertMusic = new JButton(Icons.get("add.svg"));
        insertMusic.setToolTipText("Insert music below selection");
        insertMusic.addActionListener(e -> {
            TemplateTable table = getSelectedTemplateTable();
            table.addMusicBelowSelection();
        });

        removeMusic = new JButton(Icons.get("delete.svg"));
        removeMusic.setToolTipText("Delete selected musics");
        removeMusic.addActionListener(e -> {
            TemplateTable table = getSelectedTemplateTable();
            table.deleteSelectedMusics();
        });

        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.add(insertMusic);
        toolBar.add(removeMusic);

        return toolBar;
    }

    private JTabbedPane createTemplatesPane() {
        return new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) {
            @Override
            public void insertTab(String title, Icon icon, Component component, String tip, int index) {
                if (component instanceof TemplateTable panel) {
                    Template t = panel.getModel().getTemplate();
                    t.nameProperty().addListener(templateNameListener);
                    panels.put(t, panel);
                    super.insertTab(title, icon, component, tip, index);
                }
            }

            @Override
            public void removeTabAt(int index) {
                TemplateTable table = (TemplateTable) getComponentAt(index);
                table.getModel().getTemplate().nameProperty().removeListener(templateNameListener);

                super.removeTabAt(index);
            }
        };
    }

    private TemplatesListener createTemplateListener() {
        return (event, template) -> {
            if (event == TemplatesListener.TEMPLATE_ADDED) {
                templatesPane.addTab(template.getName(), new TemplateTable(template));
                int i = templatesPane.indexOfTab(template.getName());
                templatesPane.setSelectedIndex(i);
            } else if (event == TemplatesListener.TEMPLATE_REMOVED) {
                TemplateTable table = panels.remove(template);
                if (table != null) {
                    remove(table);
                }
            }
        };
    }

    private void setTabName(Property<? extends String> prop, String oldValue, String newValue) {
        if (prop.getOwner() instanceof Template template) {
            TemplateTable table = panels.get(template);
            int index = templatesPane.indexOfComponent(table);
            templatesPane.setTitleAt(index, template.getName());
        }
    }

    public TemplateTable getSelectedTemplateTable() {
        return ((TemplateTable) templatesPane.getSelectedComponent());
    }

    public Template getSelectedTemplate() {
        return ((TemplateTable) templatesPane.getSelectedComponent()).getModel().getTemplate();
    }
}

