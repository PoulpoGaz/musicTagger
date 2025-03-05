package fr.poulpogaz.musicdl.ui.dialogs;

import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.icons.FlatOptionPaneQuestionIcon;
import fr.poulpogaz.musicdl.ui.MetadataFieldDocumentFilter;
import fr.poulpogaz.musicdl.ui.table.MTable;
import fr.poulpogaz.musicdl.ui.SimpleDocumentListener;
import fr.poulpogaz.musicdl.ui.TablePopupMenuSupport;
import fr.poulpogaz.musicdl.ui.table.MoveAction;
import fr.poulpogaz.musicdl.ui.table.NewRowAction;
import fr.poulpogaz.musicdl.ui.table.RemoveRowAction;
import fr.poulpogaz.musicdl.ui.table.SetAction;
import fr.poulpogaz.musicdl.ui.text.ErrorTextField;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;

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
    protected MTable keyTable;
    protected Action keyTableNewAction;
    protected Action keyTableRemoveAction;
    protected Action keyTableMoveUpAction;
    protected Action keyTableMoveDownAction;
    protected Action keyTableSetNullAction;
    protected JToolBar keyToolbar;

    // generators
    protected MTable generatorTable;
    protected Action generatorNewAction;
    protected Action generatorRemoveAction;
    protected Action generatorSetNullAction;
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



        keyTable = createTable(templateModel.getKeyTableModel(), keyTableMetadataFieldColumn());
        createKeyTableActions();

        keyToolbar = createKeyToolbar();
        keyToolbar.setFloatable(false);
        keyToolbar.setOrientation(SwingConstants.VERTICAL);
        JPopupMenu menu = createKeyPopupMenu();
        keyTable.addMouseListener(new TablePopupMenuSupport(keyTable, menu));



        generatorTable = createTable(templateModel.getMetadataGeneratorTableModel(), generatorTableMetadataFieldColumn());
        createGeneratorTableActions();

        generatorToolBar = createGeneratorToolbar();
        generatorToolBar.setFloatable(false);
        generatorToolBar.setOrientation(SwingConstants.VERTICAL);
        menu = createGeneratorPopupMenu();
        generatorTable.addMouseListener(new TablePopupMenuSupport(generatorTable, menu));


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

    protected MTable createTable(RestoreTableModel<?> model, int metadataColumn) {
        DefaultCellEditor metadataEditor = new DefaultCellEditor(MetadataFieldDocumentFilter.createTextField());

        MTable table = new MTable(model) {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == metadataColumn) {
                    return metadataEditor;
                }
                return super.getCellEditor(row, column);
            }
        };
        table.setDefaultRenderer(Object.class, new RevertTableCellRenderer());
        table.setModel(model);
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }

    protected abstract int keyTableMetadataFieldColumn();

    protected void createKeyTableActions() {
        keyTableNewAction = NewRowAction.create(keyTable, "key");
        keyTableRemoveAction = RemoveRowAction.create(keyTable, "value");
        keyTableMoveUpAction = MoveAction.moveUp(keyTable, "value");
        keyTableMoveDownAction = MoveAction.moveDown(keyTable, "value");
        keyTableSetNullAction = SetAction.setNull(keyTable);
    }

    protected JToolBar createKeyToolbar() {
        JToolBar bar = new JToolBar();
        bar.add(keyTableNewAction);
        bar.add(keyTableRemoveAction);
        bar.add(keyTableMoveUpAction);
        bar.add(keyTableMoveDownAction);
        return bar;
    }

    protected JPopupMenu createKeyPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(keyTableNewAction);
        menu.add(keyTableRemoveAction);
        menu.add(keyTableMoveUpAction);
        menu.add(keyTableMoveDownAction);
        menu.addSeparator();
        menu.add(keyTableSetNullAction);
        return menu;
    }


    protected abstract int generatorTableMetadataFieldColumn();

    protected void createGeneratorTableActions() {
        generatorNewAction = NewRowAction.create(generatorTable, "metadata generator");
        generatorRemoveAction = RemoveRowAction.create(generatorTable, "metadata generator");
        generatorSetNullAction = SetAction.setNull(generatorTable);
    }

    protected JToolBar createGeneratorToolbar() {
        JToolBar bar = new JToolBar();
        bar.add(generatorNewAction);
        bar.add(generatorRemoveAction);
        return bar;
    }

    protected JPopupMenu createGeneratorPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(generatorNewAction);
        menu.add(generatorRemoveAction);
        menu.addSeparator();
        menu.add(generatorSetNullAction);
        return menu;
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
