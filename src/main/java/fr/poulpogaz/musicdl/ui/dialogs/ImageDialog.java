package fr.poulpogaz.musicdl.ui.dialogs;

import com.formdev.flatlaf.FlatClientProperties;
import fr.poulpogaz.musicdl.Utils;
import fr.poulpogaz.musicdl.Zoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageDialog extends AbstractDialog {

    private final BufferedImage image;

    public ImageDialog(BufferedImage image, JFrame owner, String title, boolean modal) {
        super(owner, title, modal);
        this.image = image;
        init();
    }

    public ImageDialog(BufferedImage image, Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        this.image = image;
        init();
    }

    @Override
    protected void initComponents() {
        ViewerModel model = new ViewerModel(image);

        setLayout(new BorderLayout());
        add(new ImagePanel(model), BorderLayout.CENTER);
        add(new BottomInfoPanel(model), BorderLayout.SOUTH);
    }

    @Override
    protected void setBestSize() {
        setSize(new Dimension(1024, 640));
    }

    public static class ViewerModel {

        private static final Logger LOGGER = LogManager.getLogger(ViewerModel.class);

        private final EventListenerList listenerList = new EventListenerList();
        private final BufferedImage image;

        private Zoom zoom = Zoom.Fit.INSTANCE;
        private int viewWidth;
        private int viewHeight;
        private double scaleFactor;

        public ViewerModel(BufferedImage image) {
            this.image = Objects.requireNonNull(image);
        }

        public void setZoom(Zoom zoom) {
            if (zoom != null && !zoom.equals(this.zoom)) {
                this.zoom = zoom;
                computeScaleFactor(true);
            }
        }

        public Zoom getZoom() {
            return zoom;
        }

        public double getScaleFactor() {
            return scaleFactor;
        }

        public void setViewSize(int width, int height) {
            if (width != this.viewWidth || height != this.viewHeight) {
                this.viewWidth = width;
                this.viewHeight = height;
                computeScaleFactor(true);
            }
        }

        public int getViewWidth() {
            return viewWidth;
        }

        public int getViewHeight() {
            return viewHeight;
        }

        private void computeScaleFactor(boolean forceEvent) {
            boolean fireEvent = forceEvent;

            double scaleFactor = zoom.getScaleFactor(image, viewWidth, viewHeight);

            if (scaleFactor != this.scaleFactor) {
                this.scaleFactor = scaleFactor;
                fireEvent = true;
            }

            if (fireEvent) {
                fireZoomEvent(new ZoomEvent(this, zoom, scaleFactor, viewWidth, viewHeight));
            }
        }

        private void fireZoomEvent(ZoomEvent event) {
            Object[] listeners = listenerList.getListenerList();

            for (int i = 0; i < listeners.length; i += 2) {
                if (listeners[i] == ZoomListener.class) {
                    ((ZoomListener) listeners[i + 1]).zoomChanged(event);
                }
            }
        }

        public void addZoomListener(ZoomListener listener) {
            listenerList.add(ZoomListener.class, listener);
        }

        public void removeZoomListener(ZoomListener listener) {
            listenerList.remove(ZoomListener.class, listener);
        }

        public BufferedImage getImage() {
            return image;
        }
    }

    public static class ZoomEvent extends EventObject {

        private final Zoom zoom;
        private final double scaleFactor;
        private final int viewWidth;
        private final int viewHeight;

        public ZoomEvent(Object source, Zoom zoom, double scaleFactor, int viewWidth, int viewHeight) {
            super(source);
            this.zoom = zoom;
            this.scaleFactor = scaleFactor;
            this.viewWidth = viewWidth;
            this.viewHeight = viewHeight;
        }

        public Zoom getZoom() {
            return zoom;
        }

        public double getScaleFactor() {
            return scaleFactor;
        }

        public int getViewWidth() {
            return viewWidth;
        }

        public int getViewHeight() {
            return viewHeight;
        }
    }


    public static interface ZoomListener extends EventListener {

        void zoomChanged(ZoomEvent event);
    }



    private static class ImagePanel extends JPanel {
        private final ViewerModel model;

        private double offsetX;
        private double offsetY;

        private int lastMouseX;
        private int lastMouseY;

        public ImagePanel(ViewerModel model) {
            this.model = model;

            model.addZoomListener(_ -> {
                computeOffset();
                repaint();
            });

            MouseAdapter mouseListener = createMouseListener();
            addMouseListener(mouseListener);
            addMouseMotionListener(mouseListener);
            addMouseWheelListener(mouseListener);
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    computeOffset();
                    model.setViewSize(getWidth(), getHeight());
                }
            });
            setFocusable(true);
        }

        private MouseAdapter createMouseListener() {
            return new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    requestFocus();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocus();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    requestFocus();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        int dragX = e.getX() - lastMouseX;
                        int dragY = e.getY() - lastMouseY;

                        computeOffset(dragX, dragY);
                    }

                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    super.mouseWheelMoved(e);
                }
            };
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            double scale = model.getScaleFactor();

            AffineTransform old = g2d.getTransform();
            AffineTransform base = (AffineTransform) old.clone();

            base.translate(offsetX, offsetY);
            base.scale(scale, scale);

            g2d.setTransform(base);
            if (scale >= 1) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            } else {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
            g2d.drawImage(model.getImage(), 0, 0, null);
            g2d.setTransform(old);
        }

        private void computeOffset() {
            computeOffset(0, 0);
        }

        private void computeOffset(int dragX, int dragY) {
            BufferedImage image = model.getImage();
            double scaledWidth = image.getWidth() * model.getScaleFactor();
            double scaledHeight = image.getHeight() * model.getScaleFactor();

            boolean canMoveOnXAxis = scaledWidth > getWidth();
            boolean canMoveOnYAxis = scaledHeight > getHeight();

            double oldOffsetX = offsetX;
            double olfOffsetY = offsetY;

            if (canMoveOnXAxis) {
                offsetX += dragX;

                if (offsetX > 0) {
                    offsetX = 0;
                }
                if (offsetX < getWidth() - scaledWidth) {
                    offsetX = getWidth() - scaledWidth;
                }
            } else {
                offsetX = (getWidth() - scaledWidth) / 2;
            }

            if (canMoveOnYAxis) {
                offsetY += dragY;

                if (offsetY > 0) {
                    offsetY = 0;
                }
                if (offsetY < getHeight() - scaledHeight) {
                    offsetY = getHeight() - scaledHeight;
                }
            } else {
                offsetY = (getHeight() - scaledHeight) / 2;
            }

            if (!Utils.equals(oldOffsetX, offsetX) || !Utils.equals(olfOffsetY, offsetY)) {
                repaint();
            }
        }
    }






    public static class BottomInfoPanel extends JPanel implements ZoomListener {

        private static final Logger LOGGER = LogManager.getLogger(BottomInfoPanel.class);

        private static final Pattern ZOOM_PATTERN = Pattern.compile(
                "^\\s*(\\d+)\\s*%\\s*$", Pattern.CASE_INSENSITIVE
        );

        private static final double LOG_2 = Math.log(2);

        private final ViewerModel model;

        private boolean setZoom = true;
        private JButton save;
        private JToggleButton fitButton;
        private JSlider zoomSlider;
        private JComboBox<Zoom> zoomComboBox;

        public BottomInfoPanel(ViewerModel model) {
            this.model = model;

            model.addZoomListener(this);

            initComponents();
            revalidateZoomComponents(model.getZoom(), model.getScaleFactor());
        }

        private void initComponents() {
            setLayout(new GridBagLayout());

            save = new JButton("Save");
            save.addActionListener(_ -> {
                Window parent = SwingUtilities.getWindowAncestor(this);
                Path path = Dialogs.showFileChooser(parent, JFileChooser.FILES_ONLY, new FileNameExtensionFilter("PNG image", "png"));

                if (path != null) {
                    if (!path.getFileName().toString().endsWith(".png")) {
                        path = path.resolveSibling(path.getFileName() + ".png");
                    }

                    try {
                        ImageIO.write(model.getImage(), "png", path.toFile());
                    } catch (IOException e) {
                        LOGGER.debug("Failed to save image to {}", path, e);
                        Dialogs.showError(parent, "Failed to save image", e);
                    }
                }
            });

            fitButton = new JToggleButton("Fit");
            fitButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
            fitButton.addActionListener(e -> {
                if (setZoom) {
                    if (fitButton.isSelected()) {
                        model.setZoom(Zoom.Fit.INSTANCE);
                    } else {
                        model.setZoom(new Zoom.Percent(1));
                    }
                }
            });

            zoomSlider = new JSlider();
            Dimension pref = zoomSlider.getPreferredSize();
            zoomSlider.setPreferredSize(new Dimension(pref.width / 2, pref.height));
            zoomSlider.addChangeListener((e) -> {
                if (setZoom) {
                    model.setZoom(new Zoom.Percent(sliderToScale(zoomSlider.getValue())));
                }
            });

            zoomComboBox = new JComboBox<>();
            zoomComboBox.addItem(Zoom.Fit.INSTANCE);
            zoomComboBox.addItem(Zoom.FitStretched.INSTANCE);
            zoomComboBox.addItem(Zoom.FillVertically.INSTANCE);
            zoomComboBox.addItem(Zoom.FillHorizontally.INSTANCE);
            zoomComboBox.addItem(new Zoom.Percent(1));
            zoomComboBox.addItem(new Zoom.Percent(2));
            zoomComboBox.addItem(new Zoom.Percent(4));
            zoomComboBox.addItem(new Zoom.Percent(8));
            zoomComboBox.addItem(new Zoom.Percent(16));
            zoomComboBox.setEditable(true);
            zoomComboBox.addActionListener(e -> {
                if (setZoom) {
                    Zoom z = parseZoom(zoomComboBox.getSelectedItem());

                    if (z != null) {
                        model.setZoom(z);
                    }
                }
            });

            // layout
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            add(save, c);

            c.gridx++;
            c.weightx = 1;
            add(Box.createHorizontalGlue(), c);

            c.gridx++;
            c.weightx = 1;
            add(Box.createHorizontalGlue(), c);

            c.gridx++;
            c.weightx = 0;
            add(fitButton, c);

            c.gridx++;
            c.weightx = 0;
            add(zoomSlider, c);

            c.gridx++;
            c.weightx = 0;
            add(zoomComboBox, c);
        }

        private void revalidateZoomComponents(Zoom zoom, double scale) {
            setZoomMaximumAndMinimum();

            setZoom = false;
            fitButton.setSelected(zoom == Zoom.Fit.INSTANCE);
            zoomSlider.setValue(scaleToSlider(scale));
            zoomComboBox.getEditor().setItem(zoom);

            setZoom = true;
        }

        private void setZoomMaximumAndMinimum() {
            BufferedImage img = model.getImage();

            setZoom = false;
            if (img == null) {
                zoomSlider.setMinimum(1);
                zoomSlider.setMaximum(400);
            } else {
                double maximum = 0;
                double minimum = Double.MAX_VALUE;

                int viewWidth = model.getViewWidth();
                int viewHeight = model.getViewHeight();

                for (int i = 0; i < zoomComboBox.getItemCount(); i++) {
                    Object o = zoomComboBox.getItemAt(i);

                    if (o instanceof Zoom z) {
                        double scale = z.getScaleFactor(img, viewWidth, viewHeight);
                        maximum = Math.max(maximum, scale);
                        minimum = Math.min(minimum, scale);
                    }
                }

                int v = zoomSlider.getValue();

                zoomSlider.setMinimum(scaleToSlider(minimum));
                zoomSlider.setMaximum(scaleToSlider(maximum));

                if (v < zoomSlider.getMinimum()) {
                    zoomSlider.setValue(zoomSlider.getMinimum());
                } else if (v > zoomSlider.getMaximum()) {
                    zoomSlider.setValue(zoomSlider.getMaximum());
                }
            }

            setZoom = true;
        }

        private int scaleToSlider(double scale) {
            return (int) (log2(scale) * 100);
        }

        private double sliderToScale(int slider) {
            return Math.pow(2, slider / 100d);
        }

        private static double log2(double v) {
            return Math.log(v) / LOG_2;
        }

        private static Zoom parseZoom(Object object) {
            if (object instanceof Zoom z) {
                return z;
            } else if (object instanceof CharSequence cs) {
                String s = cs.toString().trim();

                Matcher m = ZOOM_PATTERN.matcher(s);
                if (m.matches()) {
                    String g = m.group(1).toLowerCase();

                    if (g.equals("fit")) {
                        return Zoom.Fit.INSTANCE;
                    } else if (g.equals("fill")) {
                        return null;
                    } else {
                        try {
                            return new Zoom.Percent(Integer.parseInt(g) / 100d);
                        } catch (NumberFormatException ignored) {
                            return null;
                        }
                    }
                }

                return null;
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void zoomChanged(ZoomEvent event) {
            revalidateZoomComponents(event.getZoom(), event.getScaleFactor());
        }
    }
}
