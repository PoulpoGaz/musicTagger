package fr.poulpogaz.musicdl;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.musicdl.model.Templates;
import fr.poulpogaz.musicdl.ui.MusicdlFrame;
import fr.poulpogaz.musicdl.utils.Directories;
import fr.poulpogaz.musicdl.utils.OS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("--- music-dl ---");
        LOGGER.info("OS: {}", OS.getOS());
        LOGGER.info("Configuration directory: {}", Directories.getConfigurationDirectory());

        try {
            Templates.readTemplates();
        } catch (JsonException | IOException e) {
            LOGGER.fatal("Failed to read template file", e);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            FlatLaf.registerCustomDefaultsSource("themes");
            FlatDarculaLaf.setup();

            MusicdlFrame.getInstance().setVisible(true);
        });
    }
}
