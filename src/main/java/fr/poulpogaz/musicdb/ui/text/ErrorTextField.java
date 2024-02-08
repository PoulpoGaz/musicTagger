package fr.poulpogaz.musicdb.ui.text;

import com.formdev.flatlaf.FlatClientProperties;
import fr.poulpogaz.musicdb.ui.Icons;
import fr.poulpogaz.musicdb.ui.layout.VerticalConstraint;
import fr.poulpogaz.musicdb.ui.layout.VerticalLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ErrorTextField extends JPanel {

    private static final String uiClassID = "ErrorTextFieldUI";

    private final PPTextField field;
    private final JLabel errorLabel = new JLabel();

    private Color oldTextFieldColor;

    public ErrorTextField() {
        this(null, null, 0);
    }

    public ErrorTextField(String text) {
        this(null, text, 0);
    }

    public ErrorTextField(int columns) {
        this(null, null, columns);
    }

    public ErrorTextField(String text, int columns) {
        this(null, text, columns);
    }

    public ErrorTextField(Document doc, String text, int columns) {
        field = new PPTextField(doc, text, columns);

        setLayout(new VerticalLayout(1, 1));
        VerticalConstraint constraint = new VerticalConstraint();
        constraint.fillXAxis = true;

        add(field, constraint);

        constraint.endComponent = true;
        add(errorLabel, constraint);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                clearLabel();
            }
        });

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                clearLabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                clearLabel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                clearLabel();
            }
        });
    }

    public void updateUI() {
        setUI((ErrorTextFieldUI) UIManager.getUI(this));
    }

    public void clearLabel() {
        errorLabel.setIcon(null);
        errorLabel.setText(null);
        errorLabel.setForeground(oldTextFieldColor);
        field.putClientProperty(FlatClientProperties.OUTLINE, null);

        oldTextFieldColor = null;
    }

    public void error(String error) {
        oldTextFieldColor = errorLabel.getForeground();
        field.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        errorLabel.setForeground(getUI().getErrorBorderColor());

        errorLabel.setText(error);
        errorLabel.setIcon(Icons.get("error.svg"));
    }

    public void warning(String warning) {
        oldTextFieldColor = field.getForeground();
        field.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_WARNING);
        field.setForeground(getUI().getWarningBorderColor());

        errorLabel.setText(warning);
        errorLabel.setIcon(Icons.get("warning.svg"));
    }

    public String getText() {
        return field.getText();
    }

    public void setText(String text) {
        field.setText(text);
    }

    public PPTextField getField() {
        return field;
    }

    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    protected void setUI(ErrorTextFieldUI ui) {
        super.setUI(ui);
    }

    @Override
    public ErrorTextFieldUI getUI() {
        return (ErrorTextFieldUI) ui;
    }
}