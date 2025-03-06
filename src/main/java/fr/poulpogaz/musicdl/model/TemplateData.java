package fr.poulpogaz.musicdl.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class TemplateData implements Iterable<Music> {

    public static final boolean DEBUG = true;

    private final Template template;
    private final List<Music> musics = new ArrayList<>();

    private final List<TemplateDataListener> listeners = new ArrayList<>();

    public TemplateData(Template template) {
        this.template = template;
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
        if (music.template != null) {
            music.template.getData().removeMusic(music.index);
        }
        music.template = template;
        musics.add(index, music);
        revalidateIndex(index);
        checkIndex();
        fireEvent(TemplateDataListener.INSERT, index, index);
    }

    public void removeMusic(int index) {
        if (index < 0 || index >= musics.size()) {
            return;
        }

        Music m = musics.remove(index);
        revalidateIndex(index);
        checkIndex();
        m.index = -1;
        m.template = null;
        fireEvent(TemplateDataListener.DELETE, index, index);
    }

    public boolean removeMusic(Music music) {
        if (music != null && music.index >= 0 && music.index < musics.size()) {
            removeMusic(music.index);
            return true;
        } else {
            return false;
        }
    }

    private void revalidateIndex(int index) {
        while (index < musics.size()) {
            musics.get(index).index = index;
            index++;
        }
    }

    public void transferAllTo(TemplateData dest) {
        if (dest == this || getMusicCount() == 0) {
            return;
        }

        int prevDstSize = dest.getMusicCount();
        for (Music m : musics) {
            m.template = dest.getTemplate();
            m.index = dest.musics.size();
            dest.musics.add(m);
        }

        int srcSize = getMusicCount();
        musics.clear();

        checkIndex();

        dest.fireEvent(TemplateDataListener.INSERT,
                  prevDstSize, dest.musics.size());
        fireEvent(TemplateDataListener.DELETE,
                         0, srcSize);
    }

    public void transferMatchingTo(TemplateData dest, BiPredicate<Integer, Music> cond, int start, int end) {
        start = Math.max(start, 0);
        end = Math.min(end, musics.size());

        if (dest == this || end - start <= 0) {
            return;
        }

        int prevDstSize = dest.getMusicCount();
        int internalI = start;
        for (int i = start; i < end; i++) {
            Music m = musics.get(internalI);

            if (cond.test(i, m)) {
                m.template = dest.getTemplate();
                m.index = dest.musics.size();
                dest.musics.add(m);
                musics.remove(internalI);
            } else {
                musics.get(internalI).index = internalI;
                internalI++;
            }
        }

        revalidateIndex(internalI);
        checkIndex();

        dest.fireEvent(TemplateDataListener.INSERT,
                       prevDstSize, dest.musics.size());
        fireEvent(TemplateDataListener.DELETE,
                  start, end);
    }

    public int removeMatching(BiPredicate<Integer, Music> cond, int start, int end) {
        start = Math.max(start, 0);
        end = Math.min(end, musics.size());

        int oldSize = musics.size();
        int minI = Integer.MAX_VALUE;
        int maxI = Integer.MIN_VALUE;
        int internalI = start;
        for (int i = start; i < end; i++) {
            Music m = musics.get(internalI);

            if (cond.test(i, m)) {
                musics.remove(internalI);
                m.index = -1;
                minI = Math.min(i, minI);
                maxI = Math.max(i, maxI);
            } else {
                musics.get(internalI).index = internalI;
                internalI++;
            }
        }

        revalidateIndex(internalI);
        checkIndex();

        if (minI != Integer.MAX_VALUE) {
            fireEvent(TemplateDataListener.DELETE, minI, maxI);
            return musics.size() - oldSize;
        }
        return 0;
    }

    public int modifyMatching(BiPredicate<Integer, Music> cond, Consumer<Music> modifier, int start, int end) {
        start = Math.max(start, 0);
        end = Math.min(end, musics.size());

        int modified = 0;
        int minI = Integer.MAX_VALUE;
        int maxI = Integer.MIN_VALUE;
        for (int i = start; i < end; i++) {
            Music m = musics.get(i);

            if (cond.test(i, m)) {
                modifier.accept(m);
                modified++;
                minI = Math.min(i, minI);
                maxI = Math.max(i, maxI);
            }
        }

        if (minI != Integer.MAX_VALUE) {
            fireEvent(TemplateDataListener.UPDATE, minI, maxI);
            return modified;
        }
        return 0;
    }

    private void checkIndex() {
        if (DEBUG) {
            for (int i = 0; i < musics.size(); i++) {
                if (musics.get(i).index != i) {
                    throw new IllegalStateException();
                }
            }
        }
    }

    public void notifyChanges(Music music) {
        if (music.getTemplate() == template && music.index >= 0) {
            fireEvent(TemplateDataListener.UPDATE, music.index, music.index);
        }
    }

    public void notifyChanges() {
        fireEvent(TemplateDataListener.UPDATE, 0, getMusicCount());
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
