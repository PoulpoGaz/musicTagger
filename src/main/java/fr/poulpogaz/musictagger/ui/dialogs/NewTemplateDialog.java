package fr.poulpogaz.musictagger.ui.dialogs;

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
    protected int keyTableMetadataFieldColumn() {
        return 2;
    }

    @Override
    protected int generatorTableMetadataFieldColumn() {
        return 0;
    }
}
