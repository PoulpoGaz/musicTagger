package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.model.Template;
import fr.poulpogaz.musicdb.model.Templates;
import fr.poulpogaz.musicdb.ui.dialogs.EditTemplateDialog;
import fr.poulpogaz.musicdb.ui.dialogs.NewTemplateDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public class TemplateHelper {

    public static final String CREATE_TEMPLATE = "Create new template";
    public static final Icon CREATE_TEMPLATE_ICON = Icons.get("add.svg");

    public static final String EDIT_TEMPLATE = "Edit template";
    public static final Icon EDIT_TEMPLATE_ICON = Icons.get("edit.svg");

    public static final String DELETE_TEMPLATE = "Delete template";
    public static final Icon DELETE_TEMPLATE_ICON = Icons.get("delete.svg");


    private static Action CREATE_TEMPLATE_ACTION;

    public static Action createCreateTemplateAction() {
        if (CREATE_TEMPLATE_ACTION == null) {
            CREATE_TEMPLATE_ACTION = new AbstractAction(CREATE_TEMPLATE, CREATE_TEMPLATE_ICON) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createTemplate(MusicDBFrame.getInstance());
                }
            };
        }

        return CREATE_TEMPLATE_ACTION;
    }


    public static Action createEditTemplateAction(Supplier<Template> getTemplate) {
        return new AbstractAction(EDIT_TEMPLATE, EDIT_TEMPLATE_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editTemplate(MusicDBFrame.getInstance(), getTemplate.get());
            }
        };
    }


    public static Action createDeleteTemplateAction(Supplier<Template> getTemplate) {
        return new AbstractAction(DELETE_TEMPLATE, DELETE_TEMPLATE_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTemplate(MusicDBFrame.getInstance(), getTemplate.get());
            }
        };
    }



    public static void createTemplate(MusicDBFrame frame) {
        NewTemplateDialog.showDialog(frame);
    }

    public static void editTemplate(MusicDBFrame frame, Template template) {
        EditTemplateDialog.showDialog(frame, template);
    }

    public static void deleteTemplate(MusicDBFrame frame, Template template) {
        int r = JOptionPane.showConfirmDialog(frame,
                                              "This operation will remove all data of template " + template.getName(),
                                              "Confirm deletion",
                                              JOptionPane.YES_NO_OPTION);

        if (r == JOptionPane.YES_OPTION) {
            Templates.removeTemplate(template);
        }
    }
}
