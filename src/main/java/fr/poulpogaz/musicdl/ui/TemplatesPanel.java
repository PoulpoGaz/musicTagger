package fr.poulpogaz.musicdl.ui;

import com.formdev.flatlaf.icons.FlatOptionPaneErrorIcon;
import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.model.Templates;
import fr.poulpogaz.musicdl.model.TemplatesListener;
import fr.poulpogaz.musicdl.properties.Property;
import fr.poulpogaz.musicdl.properties.PropertyListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class TemplatesPanel extends JPanel {

    private final TabPane templatesPane;
    private final JToolBar toolBar;

    private final Map<Template, TemplateTable> panels = new HashMap<>();

    public TemplatesPanel() {
        toolBar = new JToolBar(SwingConstants.VERTICAL);
        templatesPane = new TabPane();
        for (Template t : Templates.getTemplates()) {
            templatesPane.addTab(t.getName(), new TemplateTable(t));
        }

        setLayout(new BorderLayout());
        add(templatesPane, BorderLayout.CENTER);
        add(toolBar, BorderLayout.WEST);

        Templates.addTemplatesListener(createTemplateListener());
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
                    templatesPane.remove(table);
                }
            }
        };
    }

    private void changeToolbar() {
        TemplateTable table = getSelectedTemplateTable();
        if (table != null) {
            toolBar.removeAll();
            table.populateToolbar(toolBar);
            toolBar.add(Actions.saveAction());
            toolBar.revalidate();
            toolBar.repaint();
            new FlatOptionPaneErrorIcon();
        }
    }

    public TemplateTable getSelectedTemplateTable() {
        return ((TemplateTable) templatesPane.getSelectedComponent());
    }

    public Template getSelectedTemplate() {
        return ((TemplateTable) templatesPane.getSelectedComponent()).getTemplate();
    }

    public TemplateTable getTemplateTableFor(Template template) {
        return panels.get(template);
    }

    public TemplateTable getTemplateTable(int index) {
        return (TemplateTable) templatesPane.getComponentAt(index);
    }

    public int getTemplateTableCount() {
        return templatesPane.getTabCount();
    }

    public void addChangeListener(ChangeListener listener) {
        templatesPane.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        templatesPane.removeChangeListener(listener);
    }


    private class TabPane extends JTabbedPane {

        private final TabPopupMenu popupMenu;
        private final PropertyListener<String> templateNameListener = this::setTabName;

        public TabPane() {
            super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
            popupMenu = new TabPopupMenu();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        int index = getUI().tabForCoordinate(TabPane.this, e.getX(), e.getY());

                        Template template = null;
                        if (index >= 0) {
                            template = ((TemplateTable) getComponentAt(index)).getTemplate();
                        }

                        popupMenu.show(template, TabPane.this, e.getX(), e.getY());
                    }
                }
            });
        }

        @Override
        public void insertTab(String title, Icon icon, Component component, String tip, int index) {
            if (component instanceof TemplateTable panel) {
                Template t = panel.getTemplate();
                t.nameProperty().addListener(templateNameListener);
                panels.put(t, panel);
                super.insertTab(title, icon, component, tip, index);
            }
        }

        @Override
        public void removeTabAt(int index) {
            TemplateTable table = (TemplateTable) getComponentAt(index);
            table.getTemplate().nameProperty().removeListener(templateNameListener);

            super.removeTabAt(index);
        }


        private void setTabName(Property<? extends String> prop, String oldValue, String newValue) {
            if (prop.getOwner() instanceof Template template) {
                TemplateTable table = panels.get(template);
                int index = templatesPane.indexOfComponent(table);
                templatesPane.setTitleAt(index, template.getName());
            }
        }


        @Override
        protected ChangeListener createChangeListener() {
            return new ModelListener();
        }

        private class ModelListener extends JTabbedPane.ModelListener {
            @Override
            public void stateChanged(ChangeEvent e) {
                super.stateChanged(e);
                changeToolbar();
            }
        }
    }

    private static class TabPopupMenu extends JPopupMenu {

        private Template template;
        private JMenuItem edit;
        private JMenuItem delete;

        public TabPopupMenu() {
            add(TemplateActions.createAction());
            edit = add(TemplateActions.editAction(() -> template));
            delete = add(TemplateActions.deleteAction(() -> template));
        }

        public void show(Template template, Component component, int x, int y) {
            this.template = template;
            if (template != null) {
                edit.setEnabled(true);
                delete.setEnabled(!template.isInternalTemplate());
            } else {
                edit.setEnabled(false);
                delete.setEnabled(false);
            }
            show(component, x, y);
            repaint();
        }
    }
}

