package fr.poulpogaz.musicdl.ui;

import fr.poulpogaz.musicdl.downloader.*;
import fr.poulpogaz.musicdl.model.*;
import fr.poulpogaz.musicdl.ui.table.AbstractRevertTableModel;
import fr.poulpogaz.musicdl.ui.table.MTableModel;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateTableModel extends AbstractRevertTableModel implements MTableModel {

    private final Template template;
    private final TemplateData data;

    public TemplateTableModel(Template template) {
        this.template = template;
        this.data = template.getData();
        template.addTemplateKeyListListener(_ -> fireTableStructureChanged());
        data.addTemplateDataListener(this::dataListener);
    }

    private void dataListener(TemplateData templateData, int event, int firstRow, int lastRow) {
        fireTableChanged(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS, event));
    }

    @Override
    public int getRowCount() {
        return data.getMusicCount();
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Download URL";
        } else {
            return template.getKeyName(column - 1);
        }
    }

    @Override
    public int getColumnCount() {
        return 1 + template.keyCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Music m = data.getMusic(rowIndex);
        if (columnIndex == 0) {
            return m.getDownloadURL();
        } else {
            List<String> metadata = m.getMetadata(columnIndex - 1);
            return String.join("; ", metadata);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (isCellEditable(rowIndex, columnIndex)) {
            String value = (String) aValue;
            Music m = data.getMusic(rowIndex);
            if (columnIndex == 0) {
                m.setDownloadURL(value);
            } else {
                m.putMetadata(columnIndex - 1, value);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < getRowCount() &&
                columnIndex >= 0 && columnIndex < getColumnCount()) {
            Music m = data.getMusic(rowIndex);

            return !m.isDownloading()
                    && (columnIndex == 0 || !m.hasMultipleValues(template.getKeyMetadataField(columnIndex - 1)));
        }

        return false;
    }

    public boolean canOpenTagEditor(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < getRowCount() &&
                columnIndex >= 0 && columnIndex < getColumnCount()) {
            Music m = data.getMusic(rowIndex);

            return columnIndex != 0 && !m.isDownloading();
        }

        return false;
    }


    public Template getTemplate() {
        return template;
    }

    public Music getMusic(int row) {
        if (row >= 0 && row < data.getMusicCount()) {
            return data.getMusic(row);
        } else {
            return null;
        }
    }

    public String getMetadataField(int column) {
        if (column == 0) {
            return null;
        } else {
            return template.getKeyMetadataField(column - 1);
        }
    }

    @Deprecated
    public String[][] getContent() {
        String[][] content = new String[getRowCount()][getColumnCount()];

        for (int row = 0; row < getRowCount(); row++) {
            Music m = data.getMusic(row);

            content[row][0] = m.getDownloadURL();
            for (int key = 1; key < getColumnCount(); key++) {
                content[row][key] = null; // m.getTag(key - 1);
            }
        }

        return content;
    }




    @Override
    public boolean newRow(int index) {
        return addMusic(index, new Music());
    }

    public boolean addMusic(Music music) {
        return addMusic(data.getMusicCount(), music);
    }

    public boolean addMusic(int index, Music music) {
        if (music != null && index >= 0 && index <= data.getMusicCount()) {
            data.addMusic(index, music);
            return true;
        }
        return false;
    }



    @Override
    public boolean removeRow(int index) {
        if (index >= 0 && index < data.getMusicCount()) {
            data.removeMusic(index);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeRows(ListSelectionModel selectedRows) {
        int min = selectedRows.getMinSelectionIndex();
        int max = Math.min(selectedRows.getMaxSelectionIndex() + 1, getRowCount());

        int removed = data.removeMatching((index, _) -> selectedRows.isSelectedIndex(index),
                                          min, max);
        return removed != 0;
    }

    @Override
    public boolean swapRows(int rowI, int rowJ) {
        return false;
    }

    public void transferSelectionTo(ListSelectionModel model, Template template) {
        int min = model.getMinSelectionIndex();
        int max = Math.min(model.getMaxSelectionIndex() + 1, getRowCount());

        this.template.getData()
                     .transferMatchingTo(template.getData(),
                                         (i, _) -> model.isSelectedIndex(i),
                                         min, max);
    }

    public void setNullValues(ListSelectionModel selectedRows, ListSelectionModel selectedColumns) {
        int minRow = selectedRows.getMinSelectionIndex();
        int minCol = selectedColumns.getMinSelectionIndex();

        int maxRow = selectedRows.getMaxSelectionIndex();
        int maxCol = selectedColumns.getMaxSelectionIndex();

        data.modifyMatching((index, _) -> selectedRows.isSelectedIndex(index),
                            music -> {
                                for (int col = minCol; col <= maxCol; col++) {
                                    if (selectedColumns.isSelectedIndex(col)) {
                                        if (col == 0) {
                                            music.setDownloadURL(null);
                                        } else {
                                            music.removeMetadata(col - 1);
                                        }
                                    }
                                }
                            }, minRow, maxRow + 1);
    }


    @Override
    protected boolean doRevert(int row, int column) {
        Music music = getMusic(row);
        if (music.isDownloaded()) {
            if (column == 0) {
                music.setDownloadURL(music.getOriginalDownloadURL());
            } else {
                int key = column - 1;
                List<String> metadata = music.getMetadata(key);
                metadata.clear();
                metadata.addAll(music.getOriginalMetadata(key));
            }
            return true;
        }
        return false;
    }

    public boolean hasChanged(int row, int column) {
        if (row >= 0 && row < data.getMusicCount() && column >= 0 && column < getColumnCount()) {
            Music m = data.getMusic(row);

            if (m.isDownloaded()) {
                if (column == 0) {
                    return !m.getDownloadURL().equals(m.getOriginalDownloadURL());
                } else {
                    return m.metadataHasChanged(column - 1);
                }
            }
        }
        return false;
    }


    public void downloadSelected(ListSelectionModel selectedRows) {
        int min = selectedRows.getMinSelectionIndex();
        int max = Math.min(selectedRows.getMaxSelectionIndex() + 1, getRowCount());

        for (int i = min; i <= max; i++) {
            if (selectedRows.isSelectedIndex(i)) {
                download(i, false);
            }
        }

        fireTableRowsUpdated(min, max);
    }

    public void download(int row) {
        download(row, true);
    }

    private void download(int row, boolean fireEvent) {
        Music m = data.getMusic(row);
        YTDLP ytdlp = SimpleDownloadTask.ytdlp(m.getDownloadURL());
        ytdlp.setMetadata("template", template.getName());

        Map<String, String> t = new HashMap<>();
        for (int i = 0; i < template.keyCount(); i++) {
            Key key = template.getKey(i);
            String tag = (String) getValueAt(row, i + 1);

            if (tag != null) {
                ytdlp.setMetadata(key.getMetadataField(), tag);
                t.put(key.getName(), tag);
            }
        }

        for (Template.MetadataGenerator generator : template.getGenerators()) {
            ytdlp.setMetadata(generator.getKey(), generator.getFormatter().format(t));
        }

        Formatter formatter = template.getFormatter();
        if (formatter != null) {
            ytdlp.setOutput(formatter.format(t));
        }

        SimpleDownloadTask task = new SimpleDownloadTask(m, ytdlp);
        task.addListener(EventThread.SWING_THREAD, (event, _) -> {
            if (event != DownloadListener.Event.QUEUED && event != DownloadListener.Event.STARTED) {
                m.setDownloading(false);
                m.notifyChanges();
            }
        });

        m.setDownloading(true);
        DownloadManager.offer(task);

        if (fireEvent) {
            fireTableRowsUpdated(row, row);
        }
    }
}
