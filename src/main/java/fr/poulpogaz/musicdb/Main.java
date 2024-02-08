package fr.poulpogaz.musicdb;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import fr.poulpogaz.musicdb.ui.MusicDBFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("--- MusicDB ---");

        SwingUtilities.invokeLater(() -> {
            FlatLaf.registerCustomDefaultsSource("themes");
            FlatDarculaLaf.setup();

            new MusicDBFrame().setVisible(true);
        });
    }
}
