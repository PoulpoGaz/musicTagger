package fr.poulpogaz.musicdb.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

public class TemplateData implements Iterable<Music> {

    private final Template template;
    private final List<Music> musics = new ArrayList<>();

    private final List<TemplateDataListener> listeners = new ArrayList<>();

    public TemplateData(Template template) {
        this.template = template;
    }


    void addKey(int index, Key key) {}

    void removeKey(int index, Key key) {
        for (Music m : musics) {
            m.putTag(key.getName(), null);
        }
    }

    void swapKey(int indexA, Key keyA, int indexB, Key keyB) {}

    void removeAllKeys() {
        // TODO: remove all tags
    }

    void addAllKeys(int index, List<Key> keys) {}



    public void bulkExecute(BiFunction<Integer, Music, Music> bulkFunction) {
        bulkExecute(bulkFunction, 0, musics.size());
    }

    /**
     * Iterates over the music between 'start' (inclusive) and 'end' (exclusive) allowing
     * removing and insertion of new music. The first parameter of the bulkFunction is the index
     * of the music <strong>before any changes were done</strong>.
     *
     * If the bulkFunction returns null, then the music is removed,
     * if it returns the same music, the music is not removed,
     * if it returns a new music, it is inserted before the current music and
     * the next iteration will the same music as before.
     */
    public void bulkExecute(BiFunction<Integer, Music, Music> bulkFunction, int start, int end) {
        start = Math.max(start, 0);
        end = Math.min(end, musics.size());

        int funcI = start;
        int listI = start;
        for (; funcI < end; listI++, funcI++) {
            Music m = musics.get(listI);
            Music m2 = bulkFunction.apply(funcI, m);

            if (m2 == null) {
                musics.remove(listI);
                listI--;
            } else if (m2 != m) {
                m2.setTemplate(template);
                musics.add(listI, m2);
                funcI--;
                end++;
            }
        }

        fireEvent(TemplateDataListener.UPDATE, start, end);
    }

    public int getMusicCount() {
        return musics.size();
    }

    public void addMusic(Music music) {
        addMusic(musics.size(), music);
    }

    public void addMusic(int index, Music music) {
        if (music == null || index < 0 || index > musics.size()) {
            return;
        }
        music.setTemplate(template);
        musics.add(index, music);
        fireEvent(TemplateDataListener.INSERT, index, index);
    }

    public void removeMusic(int index) {
        if (index < 0 || index >= musics.size()) {
            return;
        }

        Music m = musics.remove(index);
        m.setTemplate(null);
        fireEvent(TemplateDataListener.DELETE, index, index);
    }

    public boolean removeMusic(Music music) {
        int index = musics.indexOf(music);
        if (index >= 0) {
            removeMusic(index);
            return true;
        } else {
            return false;
        }
    }

    public Music getMusic(int index) {
        return musics.get(index);
    }

    public Template getTemplate() {
        return template;
    }

    @Override
    public Iterator<Music> iterator() {
        return musics.iterator();
    }



    private void fireEvent(int event, int firstRow, int lastRow) {
        for (TemplateDataListener listener : listeners) {
            listener.dataChanged(this, event, firstRow, lastRow);
        }
    }

    public void addTemplateDataListener(TemplateDataListener listener) {
        listeners.add(listener);
    }

    public void removeTemplateDataListener(TemplateDataListener listener) {
        listeners.remove(listener);
    }
}
