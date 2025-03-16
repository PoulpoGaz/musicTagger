package fr.poulpogaz.musictagger.ui.text;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

public class MTextField extends JTextField implements IMTextField {

    public static final String TRAILING_CHANGED_PROPERTY = "TrailingChanged";
    public static final String LEADING_CHANGED_PROPERTY = "LeadingChanged";

    private Component trailingComponent;
    private Component leadingComponent;

    public MTextField() {
        this(null, null, 0);
    }

    public MTextField(String text) {
        this(null, text, 0);
    }

    public MTextField(int columns) {
        this(null, null, columns);
    }

    public MTextField(String text, int columns) {
        this(null, text, columns);
    }

    public MTextField(Document doc, String text, int columns) {
        super(doc, text, columns);
        setLayout(new MTextFieldLayout());
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
            add(trailingComponent, MTextFieldLayout.TRAIL);
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
            add(leadingComponent, MTextFieldLayout.LEAD);
            getUI().setLeadingComponent(leadingComponent);

            firePropertyChange(LEADING_CHANGED_PROPERTY, old, trailingComponent);
        }
    }

    @Override
    public MTextFieldUI getUI() {
        return (MTextFieldUI) super.getUI();
    }

    @Override
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }
}