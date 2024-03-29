package fr.poulpogaz.musicdb.ui.dialogs;

import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.icons.FlatOptionPaneQuestionIcon;
import fr.poulpogaz.musicdb.ui.CellRendererBase;
import fr.poulpogaz.musicdb.ui.Icons;
import fr.poulpogaz.musicdb.ui.SimpleDocumentListener;
import fr.poulpogaz.musicdb.ui.TablePopupMenuSupport;
import fr.poulpogaz.musicdb.ui.text.ErrorTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;

public abstract class TemplateDialogBase extends AbstractDialog {

    protected TemplateModel templateModel;

    // label and format
    protected JLabel nameLabel;
    protected ErrorTextField nameField;
    protected JLabel formatLabel;
    protected ErrorTextField formatField;

    // key edition
    protected JTable keyTable;
    protected JButton newKeyButton;
    protected JButton removeKeyButton;
    protected JButton moveDownKeyButton;
    protected JButton moveUpKeyButton;
    protected JToolBar keyToolbar;

    // errors
    protected JTextArea errorTextArea;
    protected JScrollPane errorLabelScroll;

    protected JButton doneButton;


    private boolean formatAdjusting = false;
    private boolean nameAdjusting = false;

    public TemplateDialogBase(JFrame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    @Override
    protected void initComponents() {
        templateModel = createTemplateModel();



        nameField = new ErrorTextField();
        nameField.setText(templateModel.getName());
        nameField.getField().getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                nameAdjusting = true;
                templateModel.setName(nameField.getText());
                nameAdjusting = false;
            }
        });
        templateModel.addPropertyChangeListener("name", evt -> {
            if (!nameAdjusting) {
                nameField.setText((String) evt.getNewValue());
            }
        });


        formatField = new ErrorTextField();
        formatField.setText(templateModel.getFormat());
        formatField.getField().setTrailingComponent(createFormatTutorialButton());
        formatField.getField().getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                formatAdjusting = true;
                templateModel.setFormat(formatField.getText());
                formatAdjusting = false;
            }
        });
        templateModel.addPropertyChangeListener("format", evt -> {
            if (!formatAdjusting) {
                formatField.setText((String) evt.getNewValue());
            }
        });


        nameLabel = new JLabel("Name: ");
        formatLabel = new JLabel("Format (optional): ");

        keyTable = createKeyTable();
        newKeyButton = createNewKeyButton();
        removeKeyButton = createRemoveKeyButton();
        moveDownKeyButton = createMoveDownButton();
        moveUpKeyButton = createMoveUpButton();

        keyToolbar = new JToolBar();
        keyToolbar.setFloatable(false);
        keyToolbar.add(newKeyButton);
        keyToolbar.add(removeKeyButton);
        keyToolbar.add(moveUpKeyButton);
        keyToolbar.add(moveDownKeyButton);
        keyToolbar.setOrientation(SwingConstants.VERTICAL);

        doneButton = new JButton();
        doneButton.addActionListener(e -> done());

        errorTextArea = new JTextArea(5, 0);
        errorTextArea.setEditable(false);
        errorTextArea.setForeground(Color.RED);
        errorLabelScroll = new JScrollPane(errorTextArea);
        errorLabelScroll.setVisible(false);
        errorLabelScroll.setMinimumSize(errorTextArea.getPreferredScrollableViewportSize());

        layoutComponent();
    }

    /**
     * ---------------------
     * |Name Lab|el  |Field|
     * ---------------------
     * |Format L|abel|Field|
     * ---------------------
     * | Toolbar|   K|eys  |
     * ---------------------
     */
    protected void layoutComponent() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);

        c.gridx = c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 2;
        add(nameLabel, c);
        c.gridy = 1;
        add(formatLabel, c);

        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 2;
        add(keyToolbar, c);

        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 2;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(nameField, c);
        c.gridy = 1;
        add(formatField, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridy = 2;
        c.gridx = 1;
        c.gridwidth = 2;
        c.weighty = 1;
        add(new JScrollPane(keyTable), c);

        c.gridx = 0;
        c.gridwidth = 3;
        c.weighty = 0;
        c.gridy++;
        add(errorLabelScroll, c);

        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.gridy++;
        add(doneButton, c);
    }

    protected TemplateModel createTemplateModel() {
        return new TemplateModel();
    }

    protected Component createFormatTutorialButton() {
        JButton b = new JButton(new FlatOptionPaneQuestionIcon());
        b.putClientProperty("JButton.buttonType", FlatButton.ButtonType.toolBarButton);

        return b;
    }

    protected JTable createKeyTable() {
        JTable keyTable = new JTable();
        keyTable.setDefaultRenderer(Object.class, new KeyTableCellRenderer());
        keyTable.setModel(templateModel);
        keyTable.setColumnSelectionAllowed(false);
        keyTable.setRowSelectionAllowed(true);
        keyTable.setShowVerticalLines(true);
        keyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        keyTable.setFillsViewportHeight(true);
        keyTable.setDragEnabled(false);

        JPopupMenu menu = createTablePopupMenu();
        keyTable.addMouseListener(new TablePopupMenuSupport(keyTable, menu));

        return keyTable;
    }

    protected JPopupMenu createTablePopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem add = menu.add("Add new key");
        JMenuItem remove = menu.add("Remove key");
        JMenuItem moveUp = menu.add("Move up key");
        JMenuItem moveDown = menu.add("Move down key");
        menu.addSeparator();
        JMenuItem setNull = menu.add("Set NULL");

        add.addActionListener(l -> templateModel.newKey());
        remove.addActionListener(l -> templateModel.removeKey(keyTable.getSelectedRow()));
        moveUp.addActionListener(l -> templateModel.moveUp(keyTable.getSelectedRow()));
        moveDown.addActionListener(l -> templateModel.moveDown(keyTable.getSelectedRow()));
        setNull.addActionListener(l -> templateModel.setValueAt(null, keyTable.getSelectedRow(), keyTable.getSelectedColumn()));

        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                int rowIndex = keyTable.getSelectedRow();
                int colIndex = keyTable.getSelectedColumn();

                if (rowIndex >= 0 && colIndex >= 0) {
                    TemplateModel.KeyModel row = templateModel.getKeyModel(rowIndex);

                    remove.setEnabled(!row.isDeleted());
                    moveUp.setEnabled(!row.isDeleted());
                    moveDown.setEnabled(!row.isDeleted());
                    setNull.setEnabled(templateModel.isCellEditable(rowIndex, colIndex));
                } else {
                    remove.setEnabled(false);
                    moveUp.setEnabled(false);
                    moveDown.setEnabled(false);
                    setNull.setEnabled(false);
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

    protected JButton createNewKeyButton() {
        JButton addKeyButton = new JButton(Icons.get("add.svg"));
        addKeyButton.addActionListener(e -> templateModel.newKey());
        addKeyButton.setToolTipText("Add key");

        return addKeyButton;
    }

    protected JButton createRemoveKeyButton() {
        JButton removeKeyButton = new JButton(Icons.get("delete.svg"));
        removeKeyButton.addActionListener(e -> {
            int i = keyTable.getSelectedRow();

            if (i != -1) {
                templateModel.removeKey(i);
                int newSelection = Math.min(i, keyTable.getRowCount() - 1);
                if (newSelection >= 0) { // when removing the last row
                    keyTable.setRowSelectionInterval(newSelection, newSelection);
                }
            }
        });
        removeKeyButton.setToolTipText("Remove selected key");

        return removeKeyButton;
    }

    protected JButton createMoveDownButton() {
        JButton moveDownKeyButton = new JButton(Icons.get("move_down.svg"));
        moveDownKeyButton.addActionListener(e -> {
            int row = keyTable.getSelectedRow();
            if (row != -1 && row + 1 < keyTable.getRowCount() &&
                    templateModel.moveDown(row)) {
                keyTable.setRowSelectionInterval(row + 1, row + 1);
            }
        });
        moveDownKeyButton.setToolTipText("Move down selected key");

        return moveDownKeyButton;
    }

    protected JButton createMoveUpButton() {
        JButton moveUpKeyButton = new JButton(Icons.get("move_up.svg"));
        moveUpKeyButton.addActionListener(e -> {
            int row = keyTable.getSelectedRow();
            if (row > 0 && templateModel.moveUp(row)) {
                keyTable.setRowSelectionInterval(row - 1, row - 1);
            }
        });
        moveUpKeyButton.setToolTipText("Move down selected key");

        return moveUpKeyButton;
    }

    protected void done() {
        String[] errors = templateModel.checkValid();

        if (errors != null) {
            if (errors[0] != null) {
                nameField.error(errors[0]);
            }
            if (errors[1] != null) {
                errorTextArea.setText(errors[1]);
                errorLabelScroll.setVisible(true);
            }
            revalidate();
            repaint();
            return;
        } else {
            errorLabelScroll.setVisible(false);
            doneButton.setEnabled(false);
            keyTable.setEnabled(false);
            nameField.setEnabled(false);
            formatField.setEnabled(false);
            revalidate();
            repaint();
        }

        apply();
        dispose();
    }

    protected abstract void apply();


    private static class KeyTableCellRenderer extends CellRendererBase {

        @Override
        protected int getStatusOfCell(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TemplateModel model = (TemplateModel) jTable.getModel();
            TemplateModel.KeyModel keyModel = model.getKeyModel(row);

            if (keyModel.isNew()) {
                return NEW;
            } else if (keyModel.isDeleted()) {
                return DELETED;
            } else if (model.isCellEditable(row, column) && keyModel.hasChanged(column)) {
                return CHANGED;
            } else {
                return DEFAULT;
            }
        }
    }
}
