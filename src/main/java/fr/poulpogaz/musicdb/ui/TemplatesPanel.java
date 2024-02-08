package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.model.Template;
import fr.poulpogaz.musicdb.model.TemplateListener;
import fr.poulpogaz.musicdb.model.Templates;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TemplatesPanel extends JTabbedPane {

    private final Map<Template, TemplateTable> panels = new HashMap<>();

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

    private TemplateListener createTemplateListener() {
        return event -> {
            if (event.isNewTemplate()) {
                Template t = event.getTemplate();

                addTab(t.getName(), new TemplateTable(t));
                int i = indexOfTab(t.getName());
                setSelectedIndex(i);
            } else if (event.isTemplateModified()) {
                Template t = event.getTemplate();

                int index = indexOfComponent(panels.get(t));
                setTitleAt(index, t.getName());
            }
        };
    }

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        if (component instanceof TemplateTable panel) {
            panels.put(panel.getModel().getTemplate(), panel);
            super.insertTab(title, icon, component, tip, index);
        }
    }
}

