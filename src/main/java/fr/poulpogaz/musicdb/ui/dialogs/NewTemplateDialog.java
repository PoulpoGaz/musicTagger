package fr.poulpogaz.musicdb.ui.dialogs;

import fr.poulpogaz.musicdb.MusicDBException;
import fr.poulpogaz.musicdb.model.Template;
import fr.poulpogaz.musicdb.model.Templates;

import javax.swing.*;

public class NewTemplateDialog extends TemplateDialogBase {

    public static void showDialog(JFrame parent) {
        new NewTemplateDialog(parent).setVisible(true);
    }

    public NewTemplateDialog(JFrame parent) {
        super(parent, "New template", true);
        init();
        doneButton.setText("Create");
    }

    @Override
    protected void apply() {
        Template template = new Template();
        template.setName(templateModel.getName());
        template.setFormat(templateModel.getFormat());

        for (int i = 0; i < templateModel.getRowCount(); i++) {
            TemplateModel.KeyModel key = templateModel.getKeyModel(i);
            if (key.isDeleted()) {
                break;
            }

            if (!template.addKey(key.asKey())) {
                throw new MusicDBException("Cannot create template: key " + key + " wasn't added to template " + template);
            }
        }

        Templates.addTemplate(template);
    }
}
