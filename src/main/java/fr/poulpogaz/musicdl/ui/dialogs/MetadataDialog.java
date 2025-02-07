package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.ImageUtils;
import fr.poulpogaz.musicdl.Utils;
import fr.poulpogaz.musicdl.Zoom;
import fr.poulpogaz.musicdl.model.Music;
import fr.poulpogaz.musicdl.opus.MetadataPicture;
import org.apache.commons.collections4.MapIterator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
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
                                       Utils.prettyPrintBytes(music.getSize()),
                                       Utils.prettyPrintSeconds((int) music.getLength()),
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
        private final List<Icon> coverArts = new ArrayList<>();

        public MetadataModel() {
            MapIterator<String, String> it = music.metadataIterator();
            while (it.hasNext()) {
                String key = it.next();

                rows.add(new Row(key, it.getValue()));
            }

            rows.sort(Comparator.comparing(Row::tag).thenComparing(Row::value));

            List<MetadataPicture> pictures = music.getPictures();
            for (int i = 0; i < pictures.size(); i++) {
                BufferedImage buffImg = pictures.get(i).createBufferedImage();

                Image img = ImageUtils.scale(buffImg, 384, 384, Zoom.Fit.INSTANCE);
                img.getHeight(onImageLoaded(i));

                coverArts.add(new ImageIcon(img));
            }
        }

        private ImageObserver onImageLoaded(int imageIndex) {
            return (img, infoflags, x, y, width, height) -> {
                if ((infoflags & HEIGHT) != 0) {
                    SwingUtilities.invokeLater(() -> {
                        table.setRowHeight(rows.size() + imageIndex, height);
                    });
                }

                return (infoflags & (ALLBITS|ABORT)) == 0;
            };
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
