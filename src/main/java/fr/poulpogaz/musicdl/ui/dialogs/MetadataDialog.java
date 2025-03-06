package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.utils.ImageUtils;
import fr.poulpogaz.musicdl.utils.Units;
import fr.poulpogaz.musicdl.utils.Zoom;
import fr.poulpogaz.musicdl.model.CoverArt;
import fr.poulpogaz.musicdl.model.ExecutionStrategy;
import fr.poulpogaz.musicdl.model.Music;
import org.apache.commons.collections4.MapIterator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MetadataDialog extends AbstractDialog {

    public static void showDialog(JFrame parent, Music music) {
        MetadataDialog d = new MetadataDialog(parent, Objects.requireNonNull(music));
        d.setVisible(true);
    }

    private final Music music;
    private JTable table;

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
                                       music.getChannels()));
        }

        MetadataModel model = new MetadataModel();

        table = new JTable();
        table.setModel(model);
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(500);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int row = table.rowAtPoint(point);
                if (e.getClickCount() == 2 && row != -1) {
                    int modelRow = table.convertRowIndexToModel(row);

                    if (modelRow >= model.rows.size()) {
                        int i = modelRow - model.rows.size();
                        CoverArt cover = music.getCovers().get(i);

                        ImageDialog.showDialog(cover, MetadataDialog.this, "Picture", false);
                    }
                }
            }
        });

        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(text, BorderLayout.NORTH);
    }

    @Override
    protected float heightScaleFactor() {
        return 1.75f;
    }

    private class MetadataModel extends AbstractTableModel {

        private final List<Row> rows = new ArrayList<>();
        private final List<ImageIcon> coverArts = new ArrayList<>();

        public MetadataModel() {
            MapIterator<String, String> it = music.metadataIterator();
            while (it.hasNext()) {
                String key = it.next();

                rows.add(new Row(key, it.getValue()));
            }

            rows.sort(Comparator.comparing(Row::tag).thenComparing(Row::value));

            List<CoverArt> covers = music.getCovers();
            for (int i = 0; i < covers.size(); i++) {
                coverArts.add(new ImageIcon());

                CoverArt cover = covers.get(i);
                fetchCover(cover, i);
            }
        }

        private void fetchCover(CoverArt cover, int index) {
            cover.getImageLater((image, _) -> {
                if (image != null) {
                    Image img = ImageUtils.scale(image, 384, 384, Zoom.Fit.INSTANCE);
                    int height = img.getHeight(null); // call is blocking, because BufferedImage uses an OffScreenImageProducer

                    int n = getRowCount();
                    coverArts.get(index).setImage(img);
                    table.setRowHeight(rows.size() + index, height);
                    fireTableCellUpdated(n, 1);
                }
            }, ExecutionStrategy.eventQueue());
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "Tag";
            } else if (column == 1) {
                return "Value";
            } else {
                return "";
            }
        }

        @Override
        public int getRowCount() {
            return rows.size() + coverArts.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex >= getRowCount()
                    || columnIndex < 0 || columnIndex >= getColumnCount()) {
                return null;
            }

            if (rowIndex < rows.size()) {
                if (columnIndex == 0) {
                    return rows.get(rowIndex).tag;
                } else {
                    return rows.get(rowIndex).value;
                }
            } else {
                if (columnIndex == 0) {
                    return "Covert art";
                } else {
                    return coverArts.get(rowIndex - rows.size());
                }
            }
        }
    }

    private static class CellRenderer extends DefaultTableCellRenderer {

        @Override
        protected void setValue(Object value) {
            if (value instanceof Icon icon) {
                setHorizontalAlignment(JLabel.CENTER);
                setIcon(icon);
                setText(null);
            } else {
                setHorizontalAlignment(JLabel.LEFT);
                setIcon(null);
                super.setValue(value);
            }
        }
    }

    private record Row(String tag, String value) {}
}
