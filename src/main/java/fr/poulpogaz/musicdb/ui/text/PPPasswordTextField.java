package fr.poulpogaz.musicdb.ui.text;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

public class PPPasswordTextField extends JPasswordField implements IPPTextField {

    public static final String TRAILING_CHANGED_PROPERTY = "TrailingChanged";
    public static final String LEADING_CHANGED_PROPERTY = "LeadingChanged";

    private Component trailingComponent;
    private Component leadingComponent;

    public PPPasswordTextField() {
        setLayout(new PPTextFieldLayout());
    }

    public PPPasswordTextField(String text) {
        super(text);
        setLayout(new PPTextFieldLayout());
    }

    public PPPasswordTextField(int columns) {
        super(columns);
        setLayout(new PPTextFieldLayout());
    }

    public PPPasswordTextField(String text, int columns) {
        super(text, columns);
        setLayout(new PPTextFieldLayout());
    }

    public PPPasswordTextField(Document doc, String text, int columns) {
        super(doc, text, columns);
        setLayout(new PPTextFieldLayout());
    }

    public void setPassword(char[] password) {
        if (password != null) {
            setText(String.valueOf(password)); // TODO: Create a non String-api method
        } else {
            setText(null);
        }
    }

    @Override
    public Component getTrailingComponent() {
        return trailingComponent;
    }

    @Override
    public void setTrailingComponent(Component trailingComponent) {
        if (this.trailingComponent != trailingComponent) {
            Component old  = this.trailingComponent;

            this.trailingComponent = trailingComponent;
            add(trailingComponent, PPTextFieldLayout.TRAIL);
            getUI().setTrailingComponent(trailingComponent);

            firePropertyChange(TRAILING_CHANGED_PROPERTY, old, trailingComponent);
        }
    }

    @Override
    public Component getLeadingComponent() {
        return leadingComponent;
    }

    @Override
    public void setLeadingComponent(Component leadingComponent) {
        if (this.leadingComponent != leadingComponent) {
            Component old = this.leadingComponent;

            this.leadingComponent = leadingComponent;
            add(leadingComponent, PPTextFieldLayout.LEAD);
            getUI().setLeadingComponent(leadingComponent);

            firePropertyChange(LEADING_CHANGED_PROPERTY, old, trailingComponent);
        }
    }

    @Override
    public PPPasswordTextFieldUI getUI() {
        return (PPPasswordTextFieldUI) super.getUI();
    }

    @Override
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }
}