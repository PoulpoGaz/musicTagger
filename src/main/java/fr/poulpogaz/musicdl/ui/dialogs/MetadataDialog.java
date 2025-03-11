package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.model.CoverArt;
import fr.poulpogaz.musicdl.model.ExecutionStrategy;
import fr.poulpogaz.musicdl.model.Music;
import fr.poulpogaz.musicdl.model.SoftCoverArt;
import fr.poulpogaz.musicdl.opus.CoverType;
import fr.poulpogaz.musicdl.ui.Icons;
import fr.poulpogaz.musicdl.ui.MetadataFieldDocumentFilter;
import fr.poulpogaz.musicdl.ui.layout.HCOrientation;
import fr.poulpogaz.musicdl.ui.layout.HorizontalConstraint;
import fr.poulpogaz.musicdl.ui.layout.HorizontalLayout;
import fr.poulpogaz.musicdl.ui.table.*;
import fr.poulpogaz.musicdl.utils.ImageUtils;
import fr.poulpogaz.musicdl.utils.Units;
import fr.poulpogaz.musicdl.utils.Utils;
import fr.poulpogaz.musicdl.utils.Zoom;
import org.apache.commons.collections4.MapIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.DigestInputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MetadataDialog extends AbstractDialog {

    private static final Logger LOGGER = LogManager.getLogger(MetadataDialog.class);

    public static void showDialog(JFrame parent, Music music) {
        MetadataDialog d = new MetadataDialog(parent, Objects.requireNonNull(music));
        d.setVisible(true);
    }

    private final Music music;

    private MetadataModel metadataModel;
    private MTable metadataTable;

    public MetadataDialog(JFrame owner, Music music) {
        super(owner, "Metadata", true);
        this.music = music;
        init();
    }

    @Override
    protected void initComponents() {
        JTextArea text = new JTextArea();
        text.setLineWrap(true);
        text.setEditable(false);

        if (music.getPath() == null) {
            text.setText("Not downloaded");
        } else {
            text.setText("""
                                 Filename: %s
                                 Size: %s
                                 Length: %s
                                 Channels: %s
                                 """.formatted(music.getPath().toAbsolutePath(),
                                               Units.humanReadableBytes(music.getSize()),
                                               Units.humanReadableSeconds((int) music.getLength()),
                                               music.getChannels().getName()));
        }


        // === Metadata Table ===

        metadataModel = new MetadataModel();
        metadataTable = createMetadataTable(metadataModel);
        metadataModel.set(music); // after due to cover art loading, ensure that metadataTable is not null

        Action newAction = NewRowAction.create(metadataTable, "metadata");
        Action addImage = createOpenImageAction();
        Action removeAction = new RestoreTableRemoveRowAction(metadataTable, "metadata");
        Action moveUpAction = MoveAction.moveUp(metadataTable, "metadata");
        Action moveDownAction = MoveAction.moveDown(metadataTable, "metadata");
        Action edit = createEditAction();
        Action revert = RevertAction.create(metadataTable);

        JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
        toolBar.add(newAction);
        toolBar.add(addImage);
        toolBar.add(removeAction);
        toolBar.add(moveUpAction);
        toolBar.add(moveDownAction);
        toolBar.addSeparator();
        toolBar.add(edit);
        toolBar.add(revert);

        JPopupMenu menu = new JPopupMenu();
        menu.add(newAction);
        menu.add(addImage);
        menu.add(removeAction);
        menu.add(moveUpAction);
        menu.add(moveDownAction);
        menu.addSeparator();
        menu.add(edit);
        menu.add(revert);
        menu.add(RestoreTableModel.createRestoreAction(metadataTable, "metadata"));

        TablePopupMenuSupport.install(metadataTable, menu);


        // === apply / cancel ===
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(_ -> cancel());
        JButton apply = new JButton("Apply");
        apply.addActionListener(_ -> apply());

        getRootPane().setDefaultButton(apply);

        JPanel south = new JPanel();
        south.setLayout(new HorizontalLayout(4));
        HorizontalConstraint hc = new HorizontalConstraint();
        hc.orientation = HCOrientation.RIGHT;
        south.add(cancel, hc);
        south.add(apply, hc);


        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.WEST);
        add(new JScrollPane(metadataTable), BorderLayout.CENTER);
        add(text, BorderLayout.NORTH);
        add(south, BorderLayout.SOUTH);
    }

    private void cancel() {
        dispose(); // + cancel image loaderX
    }

    private void apply() {
        music.clearMetadata();
        music.clearCoverArts();

        for (Row row : metadataModel.getRows()) {
            row.updateMusic(music);
        }

        dispose();
    }

    private MTable createMetadataTable(MetadataModel metadataModel) {
        DefaultCellEditor metadataEditor = new DefaultCellEditor(MetadataFieldDocumentFilter.createTextField());

        MTable table = new MTable(metadataModel) {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 0) {
                    return metadataEditor;
                }
                return super.getCellEditor(row, column);
            }

            @Override
            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);

                if (e.getColumn() == 1 || e.getColumn() == TableModelEvent.ALL_COLUMNS) {
                    for (int i = 0; i < getRowCount(); i++) {
                        RestoreTableModel.Row r = metadataModel.getRow(i);
                        int height = getRowHeight();
                        if ((r instanceof CovertArtRow row)) {
                            height = row.icon.getIconHeight();
                        }

                        if (getRowHeight(i) != height && height > 0) {
                            setRowHeight(i, height);
                        }
                    }
                }
            }
        };
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(500);

        JComboBox<CoverType> editorComponent = new JComboBox<>(new DefaultComboBoxModel<>(CoverType.values()));
        editorComponent.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                if (value instanceof CoverType type) {
                    value = type.getFullDescription();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        table.setDefaultEditor(CoverType.class,
                               new DefaultCellEditor(editorComponent));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int row = table.rowAtPoint(point);
                if (e.getClickCount() == 2 && row != -1) {
                    int modelRow = table.convertRowIndexToModel(row);
                    RestoreTableModel.Row r = metadataModel.getRow(modelRow);

                    if (r instanceof CovertArtRow covertArtRow) {
                        if (covertArtRow.image != null && covertArtRow.coverArt != null) {
                            CoverArt cover = covertArtRow.coverArt;
                            ImageDialog.showDialog(cover, MetadataDialog.this, "Picture", false);
                        } else if (covertArtRow.image != null) {
                            ImageDialog.showDialog(covertArtRow.image, MetadataDialog.this, "Picture", false);
                        }
                    }
                }
            }
        });

        return table;
    }

    private Action createEditAction() {
        Action action = new AbstractMAction("Edit cell", Icons.get("edit.svg"), metadataTable) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int r = metadataTable.getSelectedRow();
                int c = metadataTable.getSelectedColumn();

                if (metadataTable.isCellEditable(r, c)) {
                    metadataTable.editCellAt(r, c);
                } else if (c == 1 && metadataModel.getRow(r) instanceof CovertArtRow row) {
                    JFileChooser chooser = createFileChooser(false);

                    if (chooser.showOpenDialog(MetadataDialog.this) == JFileChooser.APPROVE_OPTION) {
                        File image = chooser.getSelectedFile();

                        try (InputStream is = new BufferedInputStream(new FileInputStream(image))) {
                            DigestInputStream dis = new DigestInputStream(is, Utils.SHA_256);
                            BufferedImage img = ImageIO.read(dis);

                            String sha256 = Utils.bytesToHex(Utils.SHA_256.digest());
                            row.setFromDisk(image, sha256, img, resize(img));
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }

            @Override
            public boolean isEnabled() {
                int r = metadataTable.getSelectedRow();
                int c = metadataTable.getSelectedColumn();
                return r >= 0 && c >= 0 && (metadataTable.isCellEditable(r, c) || c == 1);
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, "Edit selected cell");

        return action;
    }

    private Action createOpenImageAction() {
        Action action = new AbstractMAction("Add image", Icons.get("image.svg"), metadataTable) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = createFileChooser(true);

                if (chooser.showOpenDialog(MetadataDialog.this) == JFileChooser.APPROVE_OPTION) {
                    File[] images = chooser.getSelectedFiles();
                    CovertArtRow[] rows = new CovertArtRow[images.length];

                    for (int i = 0; i < images.length; i++) {
                        rows[i] = new CovertArtRow();
                        metadataModel.newRow(rows[i]);
                    }

                    new ImageLoader(rows, images).execute();
                }
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, "Add one or multiple images below selection");
        return action;
    }

    private JFileChooser createFileChooser(boolean multiSelection) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(multiSelection);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Image", "png", "jpeg", "jpg", "gif", "bmp"));
        chooser.setCurrentDirectory(Dialogs.WORKING_DIRECTORY);

        return chooser;
    }

    @Override
    protected float heightScaleFactor() {
        return 1.75f;
    }

    private static class CellRenderer extends RevertTableCellRenderer {

        @Override
        protected void setValue(Object value) {
            if (value instanceof Icon icon) {
                setHorizontalAlignment(JLabel.CENTER);
                setIcon(icon);
                setText(null);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
                setIcon(null);
                if (value instanceof CoverType type) {
                    super.setValue(type.getFullDescription());
                } else {
                    super.setValue(value);
                }
            }
        }
    }


    private static class MetadataModel extends RestoreTableModel<Row> {

        public void set(Music music) {
            MapIterator<String, String> it = music.metadataIterator();
            while (it.hasNext()) {
                String key = it.next();

                newRow(new MetadataRow(key, it.getValue()));
            }

            for (CoverArt art : music.getCoverArts()) {
                CovertArtRow row = new CovertArtRow(art);
                newRow(row);
                row.setFromCoverArt();
            }
        }

        @Override
        protected MetadataRow createRow() {
            return new MetadataRow();
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Field";
                case 1 -> "Value";
                case 2 -> "Type";
                case 3 -> "Description";
                default -> "";
            };
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            RestoreTableModel.Row row = getRow(rowIndex);
            if (row instanceof MetadataRow) {
                return columnIndex <= 1;
            } else {
                return columnIndex >= 2;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 2) {
                return CoverType.class;
            } else {
                return super.getColumnClass(columnIndex);
            }
        }
    }


    private static abstract class Row extends RestoreTableModel.Row {

        public abstract void updateMusic(Music music);
    }

    private static class MetadataRow extends Row {

        private String originalField;
        private String originalValue;

        private String field;
        private String value;

        public MetadataRow() {

        }

        public MetadataRow(String originalField, String originalValue) {
            this.originalField = originalField;
            this.originalValue = originalValue;

            field = originalField;
            value = originalValue;
        }

        @Override
        public Object getValue(int column) {
            return switch (column) {
                case 0 -> field;
                case 1 -> value;
                default -> null;
            };
        }

        @Override
        public void setValue(Object value, int column) {
            String v = (String) value;
            if (column == 0) {
                this.field = v;
                table.fireTableCellUpdated(index, 0);
            } else if (column == 1) {
                this.value = v;
                table.fireTableCellUpdated(index, 1);
            }
        }

        @Override
        public boolean revert(int column) {
            return false;
        }

        @Override
        public boolean isNew() {
            return originalField == null && originalValue == null;
        }

        @Override
        public boolean hasChanged(int column) {
            if (!isNew()) {
                if (column == 0) return !Objects.equals(originalField, field);
                if (column == 1) return !Objects.equals(originalValue, value);
            }
            return false;
        }

        @Override
        public void updateMusic(Music music) {
            music.addMetadata(originalField, originalValue);
        }
    }


    private static class CovertArtRow extends Row {

        private CoverArt coverArt;

        private File location;
        private String sha256;
        private BufferedImage image;

        private final ImageIcon icon = new ImageIcon();
        private CoverType type;
        private String description;

        public CovertArtRow() {

        }

        public CovertArtRow(CoverArt coverArt) {
            this.coverArt = coverArt;
            type = coverArt.getType();
            description = coverArt.getDescription();
        }

        public void setFromDisk(File location, String sha256, BufferedImage image, Image resizedImage) {
            this.location = location;
            this.sha256 = sha256;
            this.image = image;
            icon.setImage(resizedImage);
            table.fireTableCellUpdated(index, 1);
        }

        public void setFromCoverArt() {
            coverArt.getImageLater((image, _) -> {
                if (image != null) {
                    if (SwingUtilities.isEventDispatchThread()) {
                        new SwingWorker<Image, Void>() {

                            @Override
                            protected Image doInBackground() {
                                return resize(image);
                            }

                            @Override
                            protected void done() {
                                try {
                                    icon.setImage(get());
                                } catch (InterruptedException | ExecutionException e) {
                                    throw new RuntimeException(e);
                                }
                                table.fireTableCellUpdated(index, 1);
                            }
                        }.execute();
                    } else {
                        Image img = resize(image);
                        SwingUtilities.invokeLater(() -> {
                            icon.setImage(img);
                            table.fireTableCellUpdated(index, 1);
                        });
                    }
                }
            }, ExecutionStrategy.sameThread());
        }

        @Override
        public Object getValue(int column) {
            return switch (column) {
                case 0 -> "METADATA_BLOCK_PICTURE";
                case 1 -> icon;
                case 2 -> type;
                case 3 -> description;
                default -> null;
            };
        }

        @Override
        public void setValue(Object value, int column) {
            if (column == 2) {
                type = (CoverType) value;
            } else if (column == 3) {
                description = (String) value;
            }
        }

        @Override
        public boolean revert(int column) {
            return false;
        }

        @Override
        public boolean isNew() {
            return coverArt == null;
        }

        @Override
        public boolean hasChanged(int column) {
            if (isNew()) {
                return false;
            }

            return switch (column) {
                case 1 -> image != null;
                case 2 -> !Objects.equals(coverArt.getType(), type);
                case 3 -> !Objects.equals(coverArt.getDescription(), description);
                default -> false;
            };
        }

        @Override
        public void updateMusic(Music music) {
            CoverArt cover = coverArt;
            if (image != null) {
                cover = SoftCoverArt.createFromFile(location, sha256, image);
            }
            if (cover != null) {
                cover.setDescription(description);
                cover.setType(type);
                music.addCoverArt(cover);
            }
        }
    }

    private static Image resize(BufferedImage image) {
        Image img = ImageUtils.scale(image, 384, 384, Zoom.Fit.INSTANCE);
        img.getHeight(null);
        return img;
    }

    private static class ImageLoader extends SwingWorker<Void, ImageLoader.Chunk> {

        private final CovertArtRow[] rows;
        private int i = 0;

        private final File[] images;

        public ImageLoader(CovertArtRow[] rows, File[] images) {
            this.rows = rows;
            this.images = images;
        }

        @Override
        protected Void doInBackground() throws Exception {
            for (File image : images) {
                LOGGER.debug("Loading image at {}", image);

                try (InputStream is = new BufferedInputStream(new FileInputStream(image))) {
                    DigestInputStream dis = new DigestInputStream(is, Utils.SHA_256);
                    BufferedImage img = ImageIO.read(dis);

                    String sha256 = Utils.bytesToHex(Utils.SHA_256.digest());
                    publish(new Chunk(sha256, img, resize(img)));
                }
            }

            return null;
        }

        @Override
        protected void process(List<Chunk> chunks) {
            for (Chunk chunk : chunks) {
                rows[i].setFromDisk(images[i], chunk.sha256, chunk.img, chunk.resizedImage);
                i++;
            }
        }

        @Override
        protected void done() {
            try {
                get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Failed to load images", e);
            }
        }


        private record Chunk(String sha256, BufferedImage img, Image resizedImage) {}
    }

    // TODO: replace image
    // TODO: image are not saved when loading image and quickly applying before the image finished loading
}
