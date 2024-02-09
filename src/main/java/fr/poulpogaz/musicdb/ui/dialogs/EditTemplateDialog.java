package fr.poulpogaz.musicdb.ui.dialogs;

import fr.poulpogaz.musicdb.model.Template;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.Objects;

public class EditTemplateDialog extends TemplateDialogBase {

    public static void showDialog(JFrame parent, Template template) {
        new EditTemplateDialog(parent, Objects.requireNonNull(template))
                .setVisible(true);
    }

    private final Template template;

    public EditTemplateDialog(JFrame owner, Template template) {
        super(owner, "Editing template: " + template.getName(), true);
        this.template = template;
        init();
        doneButton.setText("Apply");
    }

    @Override
    protected JPopupMenu createTablePopupMenu() {
        JPopupMenu menu = super.createTablePopupMenu();
        JMenuItem revert = menu.add("Revert");
        JMenuItem restore = menu.add("Restore key");

        revert.addActionListener(l -> templateModel.revertKeyValue(keyTable.getSelectedRow(), keyTable.getSelectedColumn()));
        restore.addActionListener(l -> templateModel.restoreKey(keyTable.getSelectedRow()));

        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                int rowIndex = keyTable.getSelectedRow();
                int colIndex = keyTable.getSelectedColumn();

                if (rowIndex >= 0 && colIndex >= 0) {
                    TemplateModel.KeyModel row = templateModel.getKeyModel(rowIndex);

                    revert.setEnabled(templateModel.isCellEditable(rowIndex, colIndex) && row.hasChanged(colIndex));
                    restore.setEnabled(row.isDeleted());
                } else {
                    revert.setEnabled(false);
                    restore.setEnabled(false);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });

        return menu;
    }

    @Override
    protected TemplateModel createTemplateModel() {
        TemplateModel model = new TemplateModel(template);
        model.setOldPositionVisible(true);
        return model;
    }

    @Override
    protected void apply() {

    }
}
