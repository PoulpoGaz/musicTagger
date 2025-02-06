package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.MusicdlException;
import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.model.Templates;

import javax.swing.*;

public class NewTemplateDialog extends TemplateDialogBase {

    public static int showDialog(JFrame parent) {
        NewTemplateDialog d = new NewTemplateDialog(parent);
        d.setVisible(true);
        return d.getReturnValue();
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
                throw new MusicdlException("Cannot create template: key " + key + " wasn't added to template " + template);
            }
        }

        Templates.addTemplate(template);
    }
}
