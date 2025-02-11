package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.model.Template;

import javax.swing.*;
import java.util.Objects;

public class EditTemplateDialog extends TemplateDialogBase {

    public static int showDialog(JFrame parent, Template template) {
        EditTemplateDialog d = new EditTemplateDialog(parent, Objects.requireNonNull(template));
        d.setVisible(true);
        return d.getReturnValue();
    }

    private final Template template;

    public EditTemplateDialog(JFrame owner, Template template) {
        super(owner, "Editing template: " + template.getName(), true);
        this.template = template;
        init();
        doneButton.setText("Apply");
    }

    @Override
    protected JPopupMenu createKeyTablePopupMenu() {
        return createPopupMenuForTable(keyTable, templateModel.getKeyTableModel(),
                                       TemplateModel.KeyRow::new, "key", true, true);
    }

    @Override
    protected JPopupMenu createGeneratorTablePopupMenu() {
        return createPopupMenuForTable(generatorTable, templateModel.getMetadataGeneratorTableModel(),
                                       TemplateModel.MetadataGeneratorRow::new, "metadata generator", false, true);
    }

    @Override
    protected TemplateModel createTemplateModel() {
        TemplateModel model = new TemplateModel(template);
        model.getKeyTableModel().setOldPositionVisible(true);
        return model;
    }
}
