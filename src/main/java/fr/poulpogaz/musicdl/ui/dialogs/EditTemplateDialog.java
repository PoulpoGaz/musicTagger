package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.ui.table.MTable;
import fr.poulpogaz.musicdl.ui.table.RevertAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class EditTemplateDialog extends TemplateDialogBase {

    public static int showDialog(JFrame parent, Template template) {
        EditTemplateDialog d = new EditTemplateDialog(parent, Objects.requireNonNull(template));
        d.setVisible(true);
        return d.getReturnValue();
    }

    private final Template template;

    private Action keyRevertAction;
    private Action keyRestoreAction;

    private Action generatorRevertAction;
    private Action generatorRestoreAction;

    public EditTemplateDialog(JFrame owner, Template template) {
        super(owner, "Editing template: " + template.getName(), true);
        this.template = template;
        init();
        doneButton.setText("Apply");
    }

    @Override
    protected int keyTableMetadataFieldColumn() {
        return 2;
    }

    @Override
    protected void createKeyTableActions() {
        super.createKeyTableActions();
        keyRevertAction = RevertAction.create(keyTable);
        keyRestoreAction = RestoreTableModel.createRestoreAction(keyTable, "key");
    }

    @Override
    protected JPopupMenu createKeyPopupMenu() {
        JPopupMenu menu = super.createKeyPopupMenu();
        menu.add(keyRevertAction);
        menu.add(keyRestoreAction);
        return menu;
    }

    @Override
    protected int generatorTableMetadataFieldColumn() {
        return 0;
    }

    @Override
    protected void createGeneratorTableActions() {
        super.createGeneratorTableActions();
        generatorRevertAction = RevertAction.create(generatorTable);
        generatorRestoreAction = RestoreTableModel.createRestoreAction(generatorTable, "generator");
    }

    @Override
    protected JPopupMenu createGeneratorPopupMenu() {
        JPopupMenu menu = super.createGeneratorPopupMenu();
        menu.add(generatorRevertAction);
        menu.add(generatorRestoreAction);
        return menu;
    }


    @Override
    protected TemplateModel createTemplateModel() {
        TemplateModel model = new TemplateModel(template);
        model.getKeyTableModel().setOldPositionVisible(true);
        return model;
    }
}
