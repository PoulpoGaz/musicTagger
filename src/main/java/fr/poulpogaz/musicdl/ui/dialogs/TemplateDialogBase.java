package fr.poulpogaz.musicdl.ui.dialogs;

import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.icons.FlatOptionPaneQuestionIcon;
import fr.poulpogaz.musicdl.ui.Icons;
import fr.poulpogaz.musicdl.ui.SimpleDocumentListener;
import fr.poulpogaz.musicdl.ui.TablePopupMenuSupport;
import fr.poulpogaz.musicdl.ui.text.ErrorTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.function.Supplier;

public abstract class TemplateDialogBase extends AbstractDialog {

    public static final int CANCEL = 0;
    public static final int DONE = 1;


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

    // generators
    protected JTable generatorTable;
    protected JButton newGeneratorButton;
    protected JButton removeGeneratorButton;
    protected JToolBar generatorToolBar;

    // errors
    protected JTextArea errorTextArea;
    protected JScrollPane errorLabelScroll;

    protected JButton doneButton;


    private boolean formatAdjusting = false;
    private boolean nameAdjusting = false;

    private int returnValue = CANCEL;

    public TemplateDialogBase(JFrame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    @Override
    protected void setBestSize() {
        Rectangle max = getGraphicsConfiguration().getBounds();

        int width = 800;
        int height = 500;
        setSize(Math.min(width, max.width), Math.min(height, max.height));
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

        keyTable = createTable(templateModel.getKeyTableModel());
        JPopupMenu menu = createKeyTablePopupMenu();
        keyTable.addMouseListener(new TablePopupMenuSupport(keyTable, menu));
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

        generatorTable = createTable(templateModel.getMetadataGeneratorTableModel());
        menu = createGeneratorTablePopupMenu();
        generatorTable.addMouseListener(new TablePopupMenuSupport(generatorTable, menu));
        newGeneratorButton = createNewGeneratorButton();
        removeGeneratorButton = createRemoveGeneratorButton();

        generatorToolBar = new JToolBar();
        generatorToolBar.setFloatable(false);
        generatorToolBar.add(newGeneratorButton);
        generatorToolBar.add(removeGeneratorButton);
        generatorToolBar.setOrientation(SwingConstants.VERTICAL);

        doneButton = new JButton();
        doneButton.addActionListener(_ -> done());

        errorTextArea = new JTextArea(5, 0);
        errorTextArea.setEditable(false);
        errorTextArea.setForeground(Color.RED);
        errorLabelScroll = new JScrollPane(errorTextArea);
        errorLabelScroll.setVisible(false);
        errorLabelScroll.setMinimumSize(errorTextArea.getPreferredScrollableViewportSize());

        getRootPane().setDefaultButton(doneButton);

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

        // add labels
        c.gridx = c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 2;
        add(nameLabel, c);
        c.gridy = 1;
        add(formatLabel, c);

        // add toolbars
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 2;
        add(keyToolbar, c);

        c.gridy = 3;
        add(generatorToolBar, c);


        // add text fields
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 2;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(nameField, c);
        c.gridy = 1;
        add(formatField, c);


        // add tables
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 2;
        c.gridx = 1;
        c.gridwidth = 2;
        c.weighty = 1;
        add(new JScrollPane(keyTable), c);

        c.gridy = 3;
        add(new JScrollPane(generatorTable), c);


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

    protected JTable createTable(RevertTableModel<?> model) {
        JTable keyTable = new JTable();
        keyTable.setDefaultRenderer(Object.class, new RevertTableCellRenderer());
        keyTable.setModel(model);
        keyTable.setColumnSelectionAllowed(false);
        keyTable.setRowSelectionAllowed(true);
        keyTable.setShowVerticalLines(true);
        keyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        keyTable.setFillsViewportHeight(true);
        keyTable.setDragEnabled(false);
        keyTable.getTableHeader().setReorderingAllowed(false);

        return keyTable;
    }

    protected JButton createNewKeyButton() {
        JButton addKeyButton = new JButton(Icons.get("add.svg"));
        addKeyButton.addActionListener(_ -> templateModel.newKey());
        addKeyButton.setToolTipText("Add key");

        return addKeyButton;
    }

    protected JButton createRemoveKeyButton() {
        JButton removeKeyButton = new JButton(Icons.get("delete.svg"));
        removeKeyButton.addActionListener(_ -> {
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
        moveDownKeyButton.addActionListener(_ -> {
            int row = keyTable.getSelectedRow();
            if (row != -1 && row + 1 < keyTable.getRowCount() &&
                    templateModel.moveKeyDown(row)) {
                keyTable.setRowSelectionInterval(row + 1, row + 1);
            }
        });
        moveDownKeyButton.setToolTipText("Move down selected key");

        return moveDownKeyButton;
    }

    protected JButton createMoveUpButton() {
        JButton moveUpKeyButton = new JButton(Icons.get("move_up.svg"));
        moveUpKeyButton.addActionListener(_ -> {
            int row = keyTable.getSelectedRow();
            if (row > 0 && templateModel.moveKeyUp(row)) {
                keyTable.setRowSelectionInterval(row - 1, row - 1);
            }
        });
        moveUpKeyButton.setToolTipText("Move down selected key");

        return moveUpKeyButton;
    }

    protected JPopupMenu createKeyTablePopupMenu() {
        return createPopupMenuForTable(keyTable, templateModel.getKeyTableModel(), TemplateModel.KeyRow::new,
                                       "key", true, false);
    }

    protected JPopupMenu createGeneratorTablePopupMenu() {
        return createPopupMenuForTable(generatorTable, templateModel.getMetadataGeneratorTableModel(),
                                       TemplateModel.MetadataGeneratorRow::new, "metadata generator", false, false);
    }

    protected <R extends RevertTableModel.Row>
    JPopupMenu createPopupMenuForTable(JTable table,
                                       RevertTableModel<R> model,
                                       Supplier<R> supplier,
                                       String rowType,
                                       boolean addMoveUpDown,
                                       boolean addRevert) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem add = menu.add("Add new " + rowType);
        add.addActionListener(_ -> model.newRow(supplier.get()));
        JMenuItem remove = menu.add("Remove " + rowType);
        remove.addActionListener(_ -> model.removeRow(table.getSelectedRow()));

        JMenuItem moveUp;
        JMenuItem moveDown;
        if (addMoveUpDown) {
            moveUp = menu.add("Move up " + rowType);
            moveUp.addActionListener(_ -> model.moveUp(table.getSelectedRow()));
            moveDown = menu.add("Move down " + rowType);
            moveDown.addActionListener(_ -> model.moveDown(table.getSelectedRow()));
        } else {
            moveUp = null;
            moveDown = null;
        }

        menu.addSeparator();
        JMenuItem setNull = menu.add("Set NULL");
        setNull.addActionListener(
                _ -> model.setValueAt(null, table.getSelectedRow(), table.getSelectedColumn()));

        JMenuItem revert;
        JMenuItem restore;
        if (addRevert) {
            revert = menu.add("Revert");
            revert.addActionListener(
                    _ -> templateModel.revertKeyValue(keyTable.getSelectedRow(), keyTable.getSelectedColumn()));
            restore = menu.add("Restore " + rowType);
            restore.addActionListener(_ -> templateModel.restoreKey(keyTable.getSelectedRow()));
        } else {
            revert = null;
            restore = null;
        }

        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                int rowIndex = table.getSelectedRow();
                int colIndex = table.getSelectedColumn();

                if (rowIndex >= 0 && colIndex >= 0) {
                    R row = model.getRow(rowIndex);

                    remove.setEnabled(!row.isRemoved());
                    if (addMoveUpDown) {
                        moveUp.setEnabled(!row.isRemoved());
                        moveDown.setEnabled(!row.isRemoved());
                    }
                    setNull.setEnabled(model.isCellEditable(rowIndex, colIndex));
                    if (addRevert) {
                        revert.setEnabled(row.canRevert(colIndex));
                        restore.setEnabled(row.isRemoved());
                    }
                } else {
                    remove.setEnabled(false);
                    if (addMoveUpDown) {
                        moveUp.setEnabled(false);
                        moveDown.setEnabled(false);
                    }
                    setNull.setEnabled(false);
                    if (addRevert) {
                        revert.setEnabled(false);
                        restore.setEnabled(false);
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        return menu;
    }

    protected JButton createNewGeneratorButton() {
        JButton addGeneratorButton = new JButton(Icons.get("add.svg"));
        addGeneratorButton.addActionListener(_ -> templateModel.getMetadataGeneratorTableModel().newRow());
        addGeneratorButton.setToolTipText("Add metadata generator");

        return addGeneratorButton;
    }

    protected JButton createRemoveGeneratorButton() {
        JButton removeKeyButton = new JButton(Icons.get("delete.svg"));
        removeKeyButton.addActionListener(_ -> {
            int i = generatorTable.getSelectedRow();

            if (i != -1) {
                templateModel.getMetadataGeneratorTableModel().removeRow(i);
                int newSelection = Math.min(i, generatorTable.getRowCount() - 1);
                if (newSelection >= 0) { // when removing the last row
                    generatorTable.setRowSelectionInterval(newSelection, newSelection);
                }
            }
        });
        removeKeyButton.setToolTipText("Remove selected metadata generator");

        return removeKeyButton;
    }



    protected void done() {
        String nameError = templateModel.checkTemplateName();
        if (nameError != null) {
            nameField.error(nameError);
        }

        String keyError = templateModel.checkKeys();
        if (keyError != null) {
            errorTextArea.setText(keyError);
            errorLabelScroll.setVisible(true);
        }

        if (nameError != null && keyError != null) {
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

        returnValue = TemplateDialogBase.DONE;
        templateModel.apply();
        dispose();
    }

    public int getReturnValue() {
        return returnValue;
    }
}
