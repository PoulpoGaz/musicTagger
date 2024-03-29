package fr.poulpogaz.musicdb.downloader;

import java.awt.*;

public interface Progress {

    void addOptions(YTDLP ytdlp);

    boolean parse(String line);

    void setCanceled();

    void reset();

    Component createProgressComponent();

    Component updateProgressComponent(Component component);

    @Override
    String toString();
}
