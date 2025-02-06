package fr.poulpogaz.musicdl.ui.text;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MLabelLink extends JLabel {

    private Color linkColor;

    private boolean underlined = false;
    private Rectangle textRectangle;

    private boolean active = true;

    public MLabelLink(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
        init();
    }

    public MLabelLink(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        init();
    }

    public MLabelLink(String text) {
        super(text);
        init();
    }

    public MLabelLink(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
        init();
    }

    public MLabelLink(Icon image) {
        super(image);
        init();
    }

    public MLabelLink() {
        init();
    }

    private void init() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isActiveP() && getTextBounds().contains(e.getPoint())) {
                    fireActionListener();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                updateCursorAndText(e.getPoint());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateCursorAndText(e.getPoint());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateCursorAndText(e.getPoint());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateCursorAndText(e.getPoint());
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);

        addPropertyChangeListener("active", evt -> {
            Point p = getMousePosition();
            if (p != null) {
                updateCursorAndText(p);
            }
            repaint();
        });
    }

    @Override
    public void updateUI() {
        super.updateUI();
        linkColor = UIManager.getColor("Component.linkColor");
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isActiveP()) {
            setForeground(linkColor);
            super.paintComponent(g);

            if (getText() != null && underlined) {
                g.setColor(linkColor);

                Rectangle bounds = getTextBounds();

                int lineY = getUI().getBaseline(this, getWidth(), getHeight()) + 1;
                g.drawLine(bounds.x, lineY, bounds.x + bounds.width, lineY);
            }
        } else {
            setForeground(null);
            super.paintComponent(g);
        }
    }

    private Rectangle getTextBounds() {
        if (textRectangle == null) {
            computeLayoutRectangle();
        }

        return textRectangle;
    }

    private void computeLayoutRectangle() {
        final Insets insets = getInsets(null);

        textRectangle = new Rectangle();

        Rectangle viewRectangle = new Rectangle();
        viewRectangle.x = insets.left;
        viewRectangle.y = insets.top;
        viewRectangle.width = getWidth() - (insets.left + insets.right);
        viewRectangle.height = getHeight() - (insets.top + insets.bottom);

        SwingUtilities.layoutCompoundLabel(this,
                getFontMetrics(getFont()),
                getText(),
                isEnabled() ? getIcon() : getDisabledIcon(),
                getVerticalAlignment(),
                getHorizontalAlignment(),
                getVerticalTextPosition(),
                getHorizontalTextPosition(),
                viewRectangle,
                new Rectangle(),
                textRectangle,
                getIconTextGap());
    }

    private void fireActionListener() {
        ActionListener[] listeners = listenerList.getListeners(ActionListener.class);

        ActionEvent event = null;
        for (ActionListener listener : listeners) {
            if (event == null) {
                event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getText());
            }

            listener.actionPerformed(event);
        }
    }

    private void updateCursorAndText(Point mouse) {
        if (isActiveP()) {
            boolean old = underlined;
            underlined = getTextBounds().contains(mouse);

            if (old != underlined) {
                repaint();
            }
        } else {
            underlined = false;
        }

        if (underlined) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean isActiveP() {
        return active && isEnabled();
    }

    @Override
    public void setText(String text) {
        textRectangle = null;
        super.setText(text);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (active != this.active) {
            boolean old = this.active;
            this.active = active;

            firePropertyChange("active", old, active);
        }
    }

    public void addActionListener(ActionListener listener) {
        listenerList.add(ActionListener.class, listener);
    }

    public void removeActionListener(ActionListener listener) {
        listenerList.remove(ActionListener.class, listener);
    }
}