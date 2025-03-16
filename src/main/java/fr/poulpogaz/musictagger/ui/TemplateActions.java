package fr.poulpogaz.musictagger.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatTreeOpenIcon;
import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.poulpogaz.musictagger.model.Template;
import fr.poulpogaz.musictagger.model.Templates;
import fr.poulpogaz.musictagger.ui.dialogs.*;
import fr.poulpogaz.musictagger.ui.text.ErrorTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class TemplateActions {

    private static final Logger LOGGER = LogManager.getLogger(TemplateActions.class);

    public static final String CREATE_TEMPLATE = "Create new template";
    public static final Icon CREATE_TEMPLATE_ICON = Icons.get("add.svg");

    public static final String EDIT_TEMPLATE = "Edit template";
    public static final Icon EDIT_TEMPLATE_ICON = Icons.get("edit.svg");

    public static final String DELETE_TEMPLATE = "Delete template";
    public static final Icon DELETE_TEMPLATE_ICON = Icons.get("delete.svg");

    public static final String LOAD_TEMPLATES = "Load templates";
    public static final String SAVE_TEMPLATES = "Save templates";

    private static Action CREATE_TEMPLATE_ACTION;
    private static Action EDIT_TEMPLATE_ACTION;
    private static Action DELETE_TEMPLATE_ACTION;
    private static Action SAVE_TEMPLATES_ACTION;
    private static Action LOAD_TEMPLATES_ACTION;

    public static Action createAction() {
        if (CREATE_TEMPLATE_ACTION == null) {
            CREATE_TEMPLATE_ACTION = new AbstractAction(CREATE_TEMPLATE, CREATE_TEMPLATE_ICON) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createTemplate(MTFrame.getInstance());
                }
            };
        }

        return CREATE_TEMPLATE_ACTION;
    }

    public static Action editAction() {
        if (EDIT_TEMPLATE_ACTION == null) {
            EDIT_TEMPLATE_ACTION = editAction(
                    () -> MTFrame.getInstance().getTemplatesPanel().getSelectedTemplate());
        }

        return EDIT_TEMPLATE_ACTION;
    }

    public static Action editAction(Supplier<Template> getTemplate) {
        return new AbstractAction(EDIT_TEMPLATE, EDIT_TEMPLATE_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editTemplate(MTFrame.getInstance(), getTemplate.get());
            }
        };
    }

    public static Action deleteAction() {
        if (DELETE_TEMPLATE_ACTION == null) {
            DELETE_TEMPLATE_ACTION = deleteAction(
                    () -> MTFrame.getInstance().getTemplatesPanel().getSelectedTemplate());
        }

        return DELETE_TEMPLATE_ACTION;
    }

    public static Action deleteAction(Supplier<Template> getTemplate) {
        return new AbstractAction(DELETE_TEMPLATE, DELETE_TEMPLATE_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTemplate(MTFrame.getInstance(), getTemplate.get());
            }
        };
    }

    public static Action saveAction() {
        if (SAVE_TEMPLATES_ACTION == null) {
            SAVE_TEMPLATES_ACTION = new AbstractAction(SAVE_TEMPLATES, Icons.get("save.svg")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveTemplates(MTFrame.getInstance());
                }
            };
        }

        return SAVE_TEMPLATES_ACTION;
    }

    public static Action loadAction() {
        if (LOAD_TEMPLATES_ACTION == null) {
            LOAD_TEMPLATES_ACTION = new AbstractAction(LOAD_TEMPLATES) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadTemplates(MTFrame.getInstance());
                }
            };
        }

        return LOAD_TEMPLATES_ACTION;
    }


    public static void createTemplate(MTFrame frame) {
        int r = NewTemplateDialog.showDialog(frame);
        if (r == TemplateDialogBase.DONE) {
            saveTemplates();
        }
    }

    public static void editTemplate(MTFrame frame, Template template) {
        int r = EditTemplateDialog.showDialog(frame, template);
        if (r == TemplateDialogBase.DONE) {
            saveTemplates();
        }
    }

    public static void deleteTemplate(MTFrame frame, Template template) {
        if (template.isInternalTemplate()) {
            return;
        }

        int r = JOptionPane.showConfirmDialog(frame,
                                              "All musics will be transferred to \"Unassigned musics\" template",
                                              "Confirm deletion",
                                              JOptionPane.YES_NO_OPTION);

        if (r == JOptionPane.YES_OPTION) {
            Templates.removeTemplate(template, true);
            saveTemplates();
        }
    }

    private static void saveTemplates() {
        try {
            Templates.saveTemplates();
        } catch (JsonException | IOException e) {
            LOGGER.warn("Failed to save templates", e);
        }
    }


    public static void loadTemplates(MTFrame frame) {
        Path path = Dialogs.showFileChooser(frame);
        if (path == null) {
            return;
        }

        try {
            Templates.readTemplates(path);
        } catch (JsonException | IOException e) {
            Dialogs.showError(frame, "Failed to load templates", e);
        }
    }

    public static void saveTemplates(MTFrame frame) {
        new SaveTemplatesDialog(frame).setVisible(true);
    }


    private static class SaveTemplatesDialog extends AbstractDialog {

        private JList<Template> templates;
        private ErrorTextField path;
        private JButton save;

        public SaveTemplatesDialog(JFrame owner) {
            super(owner, "Select templates to save", true);
            init();
        }

        @Override
        protected void setBestSize() {
            pack();
        }

        @Override
        protected void initComponents() {
            DefaultListModel<Template> model = new DefaultListModel<>();
            for (Template t : Templates.getTemplates()) {
                if (!t.isInternalTemplate()) {
                    model.addElement(t);
                }
            }
            templates = new JList<>(model);
            templates.addListSelectionListener(e -> {
                if (templates.isSelectionEmpty()) {
                    save.setEnabled(false);
                }
            });
            templates.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setText(((Template) value).getName());
                    return this;
                }
            });

            JButton open = new JButton(new FlatTreeOpenIcon());
            open.addActionListener(this::open);
            open.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
            path = new ErrorTextField();
            path.getField().setTrailingComponent(open);


            JButton selectAll = new JButton("Select all");
            selectAll.addActionListener(e -> {
                templates.setSelectionInterval(0, model.getSize());
                templates.grabFocus();
            });

            save = new JButton("Save");
            save.addActionListener(e -> save());

            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(e -> dispose());

            getRootPane().setDefaultButton(save);


            // layout
            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.gridwidth = 3;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets.set(5, 5, 5, 5);
            add(new JLabel("Select templates to save"), c);

            c.gridy = 1;
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1;
            add(new JScrollPane(templates), c);

            c.gridy = 2;
            c.weighty = 0;
            add(path, c);

            c.fill = GridBagConstraints.NONE;
            c.gridy = 3;
            c.gridwidth = 1;
            add(selectAll, c);

            c.gridx = 1;
            c.weightx = 0;
            add(save, c);

            c.gridx = 2;
            c.weightx = 0;
            add(cancel, c);
        }

        private void open(ActionEvent actionEvent) {
            Path path = Dialogs.showFileChooser(this);

            if (path != null) {
                this.path.setText(path.toString());
            }
        }

        private void save() {
            Path path = Path.of(this.path.getText());

            if (Files.isDirectory(path)) {
                this.path.error("Not a file");
            }

            try {
                saveTo(path);
            } catch (IOException | JsonException e) {
                LOGGER.warn("Failed to save templates");
                Dialogs.showError(this, "Failed to save templates", e);
                return;
            }

            dispose();
        }

        private void saveTo(Path path) throws IOException, JsonException {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            IJsonWriter jw = null;
            try {
                 jw = new JsonPrettyWriter(Files.newBufferedWriter(path));

                 jw.beginObject();
                 for (int i = templates.getMinSelectionIndex(); i <= templates.getMaxSelectionIndex(); i++) {
                    if (templates.isSelectedIndex(i)) {
                        Template template = templates.getModel().getElementAt(i);

                        jw.key(template.getName()).beginObject();
                        Templates.writeTemplate(jw, template);
                        jw.endObject();
                    }
                 }
                 jw.endObject();

            } finally {
                if (jw != null) {
                    jw.close();
                }
            }
        }
    }
}
