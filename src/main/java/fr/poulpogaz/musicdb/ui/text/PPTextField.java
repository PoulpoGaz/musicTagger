package fr.poulpogaz.musicdb.ui.text;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

public class PPTextField extends JTextField implements IPPTextField {

    public static final String TRAILING_CHANGED_PROPERTY = "TrailingChanged";
    public static final String LEADING_CHANGED_PROPERTY = "LeadingChanged";

    private Component trailingComponent;
    private Component leadingComponent;

    public PPTextField() {
        this(null, null, 0);
    }

    public PPTextField(String text) {
        this(null, text, 0);
    }

    public PPTextField(int columns) {
        this(null, null, columns);
    }

    public PPTextField(String text, int columns) {
        this(null, text, columns);
    }

    public PPTextField(Document doc, String text, int columns) {
        super(doc, text, columns);
        setLayout(new PPTextFieldLayout());
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
    public PPTextFieldUI getUI() {
        return (PPTextFieldUI) super.getUI();
    }

    @Override
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }
}