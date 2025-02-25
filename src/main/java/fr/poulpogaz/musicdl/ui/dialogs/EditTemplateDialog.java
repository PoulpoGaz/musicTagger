package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.ui.MTable;

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
    private Action keyTableRestore;
    private Action generatorTableRestore;

    public EditTemplateDialog(JFrame owner, Template template) {
        super(owner, "Editing template: " + template.getName(), true);
        this.template = template;
        init();
        doneButton.setText("Apply");
    }

    @Override
    protected JPopupMenu createKeyPopupMenu() {
        JPopupMenu menu = super.createKeyPopupMenu();
        menu.add(keyTable.getRevertAction());
        menu.add(getKeyTableRestore());
        return menu;
    }

    @Override
    protected JPopupMenu createGeneratorPopupMenu() {
        JPopupMenu menu = super.createGeneratorPopupMenu();
        menu.add(generatorTable.getRevertAction());
        menu.add(getGeneratorTableRestore());
        return menu;
    }


    private Action getKeyTableRestore() {
        if (keyTableRestore == null) {
            keyTableRestore = createRestoreAction(keyTable,
                                                  templateModel.getKeyTableModel(),
                                                  "key");
        }

        return keyTableRestore;
    }

    private Action getGeneratorTableRestore() {
        if (generatorTableRestore == null) {
            generatorTableRestore = createRestoreAction(generatorTable,
                                                        templateModel.getMetadataGeneratorTableModel(),
                                                        "generator");
        }

        return generatorTableRestore;
    }

    private Action createRestoreAction(MTable table, RestoreTableModel<?> model, String name) {
        String n = "Restore " + name;
        Action action = new AbstractAction(n, null) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRow() != -1) {
                    model.restoreRow(table.getSelectedRow());
                }
            }

            @Override
            public boolean isEnabled() {
                int r = table.getSelectedRow();
                return r >= model.notRemovedRowCount();
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, n);

        table.addAction(action);

        return action;
    }


    @Override
    protected TemplateModel createTemplateModel() {
        TemplateModel model = new TemplateModel(template);
        model.getKeyTableModel().setOldPositionVisible(true);
        return model;
    }
}
