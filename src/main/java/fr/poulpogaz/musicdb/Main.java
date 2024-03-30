package fr.poulpogaz.musicdb;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.musicdb.model.Templates;
import fr.poulpogaz.musicdb.ui.MusicDBFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("--- MusicDB ---");

        try {
            Templates.readTemplates(Path.of("templates.json"));
        } catch (JsonException | IOException e) {
            LOGGER.fatal("Failed to read template file", e);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            FlatLaf.registerCustomDefaultsSource("themes");
            FlatDarculaLaf.setup();

            MusicDBFrame.getInstance().setVisible(true);
        });
    }
}
