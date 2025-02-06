package fr.poulpogaz.musicdl.ui.text;

import com.formdev.flatlaf.ui.FlatTextFieldUI;
import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.formdev.flatlaf.util.UIScale.scale;

public class MTextFieldUI extends FlatTextFieldUI {

    private static final boolean MINIMUM = false;
    private static final boolean PREFERRED = true;

    private static final int LEFT_TO_RIGHT = 0;
    private static final int RIGHT_TO_LEFT = 1;
    private static final int DO_NOT_MIND = 2;

    private CursorHoverListener trailCursorListener;
    private CursorHoverListener leadCursorListener;

    private Component trailingComponent;
    private Component leadingComponent;

    public static ComponentUI createUI(JComponent c) {
        return new MTextFieldUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        if (c instanceof MTextField field) {
            if (trailingComponent != null) {
                field.add(trailingComponent, MTextFieldLayout.TRAIL);
            }

            if (leadingComponent != null) {
                field.add(leadingComponent, MTextFieldLayout.LEAD);
            }
        }
    }

    @Override
    protected void installListeners() {
        super.installListeners();

        Component component = getComponent();

        if (component instanceof MTextField field) {
            setLeadingComponent(field.getLeadingComponent());
            setTrailingComponent(field.getTrailingComponent());
        }
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();

        setLeadingComponent(null);
        setTrailingComponent(null);
    }

    @Override
    protected Rectangle getVisibleEditorRect() {
        Rectangle alloc = super.getVisibleEditorRect();

        if (alloc != null) {
            int leftToRight = getOrientation(getComponent());

            Dimension trailDim = getTrailingDim(leftToRight, true);
            Dimension leadDim = getLeadingDim(leftToRight, true);

            alloc.x     = alloc.x + leadDim.width;
            alloc.width = alloc.width - leadDim.width - trailDim.width;
        }

        return alloc;
    }

    private Dimension getTrailingDim(int orientation, boolean prefOrMin) {
        Component comp;

        if (orientation == DO_NOT_MIND || orientation == LEFT_TO_RIGHT) {
            comp = trailingComponent;
        } else {
            comp = leadingComponent;
        }

        return comp == null ? new Dimension() :
                prefOrMin ? comp.getPreferredSize() : comp.getMinimumSize();
    }

    private Dimension getLeadingDim(int orientation, boolean prefOrMin) {
        Component comp;

        if (orientation == DO_NOT_MIND || orientation == LEFT_TO_RIGHT) {
            comp = leadingComponent;
        } else {
            comp = trailingComponent;
        }

        return comp == null ? new Dimension() :
                prefOrMin ? comp.getPreferredSize() : comp.getMinimumSize();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return getSize(c, PREFERRED);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return getSize(c, MINIMUM);
    }

    protected Dimension getSize(JComponent c, boolean prefOrMin) {
        Dimension dim = super.getPreferredSize(c);
        Insets insets = c.getInsets();

        if (c instanceof MTextField) {
            Dimension trailDim = getTrailingDim(DO_NOT_MIND, prefOrMin);
            Dimension leadDim = getLeadingDim(DO_NOT_MIND, prefOrMin);

            dim.width += trailDim.width + leadDim.width;

            int height = dim.height - insets.top - insets.bottom;

            height = Math.max(height, Math.max(trailDim.height, leadDim.height));

            dim.height = height + insets.top + insets.bottom;
        }

        return applyMinimumWidth(c, dim, minimumWidth);
    }

    private Dimension applyMinimumWidth( JComponent c, Dimension size, int minimumWidth ) {
        // do not apply minimum width if JTextField.columns is set
        if( c instanceof JTextField && ((JTextField)c).getColumns() > 0 )
            return size;

        // do not apply minimum width if used in combobox or spinner
        Container parent = c.getParent();
        if( parent instanceof JComboBox ||
                parent instanceof JSpinner ||
                (parent != null && parent.getParent() instanceof JSpinner) )
            return size;

        minimumWidth = FlatUIUtils.minimumWidth( c, minimumWidth );
        float focusWidth = FlatUIUtils.getBorderFocusWidth( c );
        size.width = Math.max( size.width, scale( minimumWidth ) + Math.round( focusWidth * 2 ) );
        return size;
    }

    private int getOrientation(Component c) {
        boolean leftToRight = c.getComponentOrientation().isLeftToRight();

        return leftToRight ? LEFT_TO_RIGHT : RIGHT_TO_LEFT;
    }

    public void setTrailingComponent(Component trailingComponent) {
        if (this.trailingComponent != trailingComponent && getComponent() instanceof MTextField) {

            if (this.trailingComponent != null) {
                this.trailingComponent.removeMouseListener(trailCursorListener);
            }

            this.trailingComponent = trailingComponent;

            if (trailingComponent != null) {
                trailCursorListener = new CursorHoverListener(trailingComponent);
                trailingComponent.addMouseListener(trailCursorListener);
            }
        }
    }

    public void setLeadingComponent(Component leadingComponent) {
        if (this.leadingComponent != leadingComponent && getComponent() instanceof MTextField) {

            if (this.leadingComponent != null) {
                this.leadingComponent.removeMouseListener(leadCursorListener);
            }

            this.leadingComponent = leadingComponent;

            if (leadingComponent != null) {
                leadCursorListener = new CursorHoverListener(leadingComponent);
                leadingComponent.addMouseListener(leadCursorListener);
            }
        }
    }

    private class CursorHoverListener extends MouseAdapter {

        private final Cursor textCursor = new Cursor(Cursor.TEXT_CURSOR);
        private final Component component;

        public CursorHoverListener(Component component) {
            this.component = component;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            component.setCursor(component.getCursor());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            component.setCursor((getComponent().isEditable()) ? textCursor : null);
        }
    }
}