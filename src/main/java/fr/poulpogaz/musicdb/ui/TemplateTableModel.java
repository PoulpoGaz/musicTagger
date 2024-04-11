package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.downloader.DownloadManager;
import fr.poulpogaz.musicdb.downloader.YTDLP;
import fr.poulpogaz.musicdb.downloader.YTDLPDownloadTask;
import fr.poulpogaz.musicdb.model.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.util.*;

public class TemplateTableModel extends AbstractTableModel {

    private final Template template;
    private final TemplateData data;

    public TemplateTableModel(Template template) {
        this.template = template;
        this.data = template.getData();
        template.addTemplateKeyListListener(this::updateTable);
        data.addTemplateDataListener(this::dataListener);

        String[] musics = new String[] {
                "https://www.youtube.com/watch?v=Urcnqat6P0s",
                "https://music.youtube.com/watch?v=yKPka2qGRh8",
                "https://music.youtube.com/watch?v=oSgGnmh4sQo",
                "https://music.youtube.com/watch?v=GtPLYvYeZ_4",
                "https://music.youtube.com/watch?v=DNCN1II0G-4",
                "https://music.youtube.com/watch?v=qiLO9YDOxYg",
                "https://music.youtube.com/watch?v=qXoghQAzQF0",
                "https://music.youtube.com/watch?v=7ddebyXI8-Q",
                "https://music.youtube.com/watch?v=SxhJ6pVGaio",
                "https://music.youtube.com/watch?v=XIwtX5aNO4w",
                "https://music.youtube.com/watch?v=VQIv_2249Sc",
                "https://music.youtube.com/watch?v=Sdb4nZgcETI",
                "https://music.youtube.com/watch?v=zoi6ofeC4rY",
                "https://music.youtube.com/watch?v=fhZMRwAs2Ys",
                "https://music.youtube.com/watch?v=26W7rVonsEs",
                "https://music.youtube.com/watch?v=jHVy3kkYiFY",
                "https://music.youtube.com/watch?v=lyzjJYugE3o",
                "https://music.youtube.com/watch?v=YYlcR-hBuXY",
                "https://music.youtube.com/watch?v=kKEkoeUHeyM",
                "https://music.youtube.com/watch?v=QnF41gLkuuE",
                "https://music.youtube.com/watch?v=gegcg4hVN0A",
        };

        int i = 0;
        for (String s : musics) {
            Music m = new Music(template);
            m.setDownloadURL(s);
            m.putTag(0, Integer.toString(i));
            i++;
            data.addMusic(m);
        }
    }

    private void updateTable(int eventType, int index0, int index1) {
        fireTableStructureChanged();
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
            return m.getTag(columnIndex - 1);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (isCellEditable(rowIndex, columnIndex)) {
            Music m = data.getMusic(rowIndex);
            if (columnIndex == 0) {
                m.setDownloadURL((String) aValue);
            } else {
                m.putTag(columnIndex - 1, (String) aValue);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return rowIndex >= 0 && rowIndex < getRowCount() &&
                columnIndex >= 0 && columnIndex < getColumnCount();
    }

    public void addRow() {
        addRow(data.getMusicCount());
    }

    public void addRow(int index) {
        addMusic(index, new Music());
    }

    public void addMusic(Music music) {
        addMusic(data.getMusicCount(), music);
    }

    public void addMusic(int index, Music music) {
        if (music.getTemplate() != null && music.getTemplate() != template) {
            return;
        }
        data.addMusic(index, music);
    }

    public void deleteRow(int rowIndex) {
        data.removeMusic(rowIndex);
    }

    public void deleteSelectedRows(ListSelectionModel model) {
        int min = model.getMinSelectionIndex();
        int max = Math.min(model.getMaxSelectionIndex() + 1, getRowCount());

        data.bulkExecute((index, music) -> {
            if (model.isSelectedIndex(index)) {
                return null;
            } else {
                return music;
            }
        }, min, max + 1);
    }

    public void setNullValues(ListSelectionModel selectedRows, ListSelectionModel selectedColumns) {
        int minRow = selectedRows.getMinSelectionIndex();
        int minCol = selectedColumns.getMinSelectionIndex();

        int maxRow = selectedRows.getMaxSelectionIndex();
        int maxCol = selectedColumns.getMaxSelectionIndex();

        data.bulkExecute((index, music) -> {
            if (selectedRows.isSelectedIndex(index)) {
                for (int col = minCol; col <= maxCol; col++) {
                    if (selectedColumns.isSelectedIndex(col)) {
                        if (col == 0) {
                            music.setDownloadURL(null);
                        } else {
                            music.removeTag(col - 1);
                        }
                    }
                }
            }
            return music;
        }, minRow, maxRow + 1);
    }


    public void download(int row) {
        Music m = data.getMusic(row);
        YTDLP ytdlp = YTDLPDownloadTask.ytdlp(m.getDownloadURL());
        ytdlp.setMetadata("template", template.getName());

        Map<String, String> t = new HashMap<>();
        for (int i = 0; i < template.keyCount(); i++) {
            Key key = template.getKey(i);
            String tag = m.getTag(i);

            if (tag != null) {
                ytdlp.setMetadata(key.getMetadataKey(), tag);
                t.put(key.getName(), tag);
            }
        }

        ytdlp.setOutput(template.getFormatter().format(t));


        DownloadManager.offer(new YTDLPDownloadTask(ytdlp));
    }



    public Template getTemplate() {
        return template;
    }

    public String[][] getContent() {
        String[][] content = new String[getRowCount()][getColumnCount()];

        for (int row = 0; row < getRowCount(); row++) {
            Music m = data.getMusic(row);

            content[row][0] = m.getDownloadURL();
            for (int key = 1; key < getColumnCount(); key++) {
                content[row][key] = m.getTag(key - 1);
            }
        }

        return content;
    }
}
