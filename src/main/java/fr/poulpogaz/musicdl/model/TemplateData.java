package fr.poulpogaz.musicdl.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class TemplateData implements Iterable<Music> {

    private final Template template;
    private final List<Music> musics = new ArrayList<>();

    private final List<TemplateDataListener> listeners = new ArrayList<>();

    public TemplateData(Template template) {
        this.template = template;
    }

    public int getMusicCount() {
        return musics.size();
    }

    public void transferAllTo(TemplateData dest) {
        if (dest == this || getMusicCount() == 0) {
            return;
        }

        int prevDstSize = dest.getMusicCount();
        for (Music m : musics) {
            m.template = dest.getTemplate();
            dest.musics.add(m);
        }

        int srcSize = getMusicCount();
        musics.clear();

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
                dest.musics.add(m);
                musics.remove(internalI);
            } else {
                internalI++;
            }
        }

        dest.fireEvent(TemplateDataListener.INSERT,
                       prevDstSize, dest.musics.size());
        fireEvent(TemplateDataListener.DELETE,
                  start, end);
    }

    public void removeMatching(BiPredicate<Integer, Music> cond, int start, int end) {
        start = Math.max(start, 0);
        end = Math.min(end, musics.size());

        int minI = Integer.MAX_VALUE;
        int maxI = Integer.MIN_VALUE;
        int internalI = start;
        for (int i = start; i < end; i++) {
            Music m = musics.get(internalI);

            if (cond.test(i, m)) {
                musics.remove(internalI);
                minI = Math.min(i, minI);
                maxI = Math.max(i, maxI);
            } else {
                internalI++;
            }
        }

        if (minI != Integer.MAX_VALUE) {
            fireEvent(TemplateDataListener.DELETE, minI, maxI);
        }
    }

    public void modifyMatching(BiPredicate<Integer, Music> cond, Consumer<Music> modifier, int start, int end) {
        start = Math.max(start, 0);
        end = Math.min(end, musics.size());

        int minI = Integer.MAX_VALUE;
        int maxI = Integer.MIN_VALUE;
        for (int i = start; i < end; i++) {
            Music m = musics.get(i);

            if (cond.test(i, m)) {
                modifier.accept(m);
                minI = Math.min(i, minI);
                maxI = Math.max(i, maxI);
            }
        }

        if (minI != Integer.MAX_VALUE) {
            fireEvent(TemplateDataListener.UPDATE, minI, maxI);
        }
    }

    public void addMusic(Music music) {
        addMusic(musics.size(), music);
    }

    public void addMusic(int index, Music music) {
        if (music == null || index < 0 || index > musics.size()) {
            return;
        }
        if (music.template != null) {
            music.template.getData().removeMusic(music);
        }
        music.template = template;
        musics.add(index, music);
        fireEvent(TemplateDataListener.INSERT, index, index);
    }

    public void removeMusic(int index) {
        if (index < 0 || index >= musics.size()) {
            return;
        }

        Music m = musics.remove(index);
        m.template = null;
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
