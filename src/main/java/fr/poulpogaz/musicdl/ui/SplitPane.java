package fr.poulpogaz.musicdl.ui;

import javax.swing.*;
import java.awt.*;

public class SplitPane extends JPanel {

    public static final String LEFT_VISIBILITY = "left_visibility";
    public static final String RIGHT_VISIBILITY = "right_visibility";

    public static final String LEFT_CHANGED = "right_visibility";
    public static final String RIGHT_CHANGED = "right_visibility";

    private static final int NOTHING_VISIBLE = 0;
    private static final int LEFT_VISIBLE    = 1;
    private static final int RIGHT_VISIBLE   = 2;
    private static final int BOTH_VISIBLE    = 3;

    private JSplitPane splitPane;
    private Component left;
    private Component right;

    private int status;

    public SplitPane(Component left, Component right) {
        this.splitPane = new JSplitPane();
        this.left = left;
        this.right = right;

        setLayout(new BorderLayout());
        init();
    }

    private void init() {
        if (left == null && right == null) {
            status = NOTHING_VISIBLE;
        } else if (left == null) {
            status = RIGHT_VISIBLE;
            add(right, BorderLayout.CENTER);
        } else if (right == null) {
            status = LEFT_VISIBLE;
            add(left, BorderLayout.CENTER);
        } else {
            status = BOTH_VISIBLE;
            splitPane.setLeftComponent(left);
            splitPane.setRightComponent(right);
            add(splitPane, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    public void setLeftVisible(boolean leftVisible) {
        if (leftVisible != isLeftVisible()) {
            // need to show left component
            if (status == NOTHING_VISIBLE) {
                add(left, BorderLayout.CENTER);
                status = LEFT_VISIBLE;
            } else if (status == RIGHT_VISIBLE) {
                splitPane.setLeftComponent(left);
                splitPane.setRightComponent(right);
                if (splitPane.getLastDividerLocation() > 0) {
                    splitPane.setDividerLocation(splitPane.getLastDividerLocation());
                }
                add(splitPane, BorderLayout.CENTER);
                status = BOTH_VISIBLE;
            } else if (status == LEFT_VISIBLE) { // need to hide left component
                remove(left);
                status = NOTHING_VISIBLE;
            } else if (status == BOTH_VISIBLE) {
                remove(splitPane);
                add(right, BorderLayout.CENTER);
                status = RIGHT_VISIBLE;
            }

            revalidate();
            repaint();

            firePropertyChange(LEFT_VISIBILITY, !leftVisible, leftVisible);
        }
    }

    public void setRightVisible(boolean rightVisible) {
        if (rightVisible != isRightVisible()) {
            // need to show right component
            if (status == NOTHING_VISIBLE) {
                add(right, BorderLayout.CENTER);
                status = RIGHT_VISIBLE;
            } else if (status == LEFT_VISIBLE) {
                splitPane.setLeftComponent(left);
                splitPane.setRightComponent(right);
                if (splitPane.getLastDividerLocation() > 0) {
                    splitPane.setDividerLocation(splitPane.getLastDividerLocation());
                }
                add(splitPane, BorderLayout.CENTER);
                status = BOTH_VISIBLE;
            } else if (status == RIGHT_VISIBLE) { // need to hide right component
                remove(right);
                status = NOTHING_VISIBLE;
            } else if (status == BOTH_VISIBLE) {
                remove(splitPane);
                add(left, BorderLayout.CENTER);
                status = LEFT_VISIBLE;
            }

            revalidate();
            repaint();

            firePropertyChange(RIGHT_VISIBILITY, !rightVisible, rightVisible);
        }
    }

    public boolean isLeftVisible() {
        return status == LEFT_VISIBLE || status == BOTH_VISIBLE;
    }

    public boolean isRightVisible() {
        return status == RIGHT_VISIBLE || status == BOTH_VISIBLE;
    }

    public Component getLeft() {
        return left;
    }

    public void setLeft(Component left) {
        if (left != this.left) {
            Component old = this.left;
            this.left = left;

            if (status == LEFT_VISIBLE) {
                remove(old);
                add(left, BorderLayout.CENTER);
            } else if (status == BOTH_VISIBLE) {
                splitPane.setLeftComponent(left);
            }

            revalidate();
            repaint();

            firePropertyChange(LEFT_CHANGED, old, left);
        }
    }

    public Component getRight() {
        return right;
    }

    public void setRight(Component right) {
        if (right != this.right) {
            Component old = this.right;
            this.right = right;

            if (status == RIGHT_VISIBLE) {
                remove(old);
                add(right, BorderLayout.CENTER);
            } else if (status == BOTH_VISIBLE) {
                splitPane.setRightComponent(right);
            }

            revalidate();
            repaint();

            firePropertyChange(RIGHT_CHANGED, old, right);
        }
    }

    public JSplitPane getSplitPane() {
        return splitPane;
    }
}
