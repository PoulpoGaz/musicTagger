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

public class TemplatesPanel extends JTabbedPane {

    private final Map<Template, TemplateTable> panels = new HashMap<>();
    private final PropertyListener<String> templateNameListener = this::setTabName;

    public TemplatesPanel() {
        super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        Templates.addTemplateListener(createTemplateListener());

        for (Template t : Templates.getTemplates()) {
            addTab(t.getName(), new TemplateTable(t));
        }
        if (getTabCount() > 0) {
            setSelectedIndex(0);
        }
    }

    private TemplatesListener createTemplateListener() {
        return (event, template) -> {
            if (event == TemplatesListener.TEMPLATE_ADDED) {
                addTab(template.getName(), new TemplateTable(template));
                int i = indexOfTab(template.getName());
                setSelectedIndex(i);
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
            int index = indexOfComponent(table);
            setTitleAt(index, template.getName());
        }
    }

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

    public Template getSelectedTemplate() {
        return ((TemplateTable) getSelectedComponent()).getModel().getTemplate();
    }
}

