package fr.poulpogaz.musicdl;

import java.nio.file.Path;

public class Directories {

    public static final String APP_FOLDER_NAME = "musicdb2";

    private static Path config;

    public static Path getConfigurationDirectory() {
        if (config == null) {
            switch (OS.getOS()) {
                case UNIX -> {
                    String dataHome = System.getenv("XDG_DATA_HOME");

                    if (dataHome == null) {
                        config = Path.of(System.getProperty("user.home"))
                                     .resolve(".local/share/" + APP_FOLDER_NAME);
                    } else {
                        config = Path.of(dataHome).resolve(APP_FOLDER_NAME);
                    }
                }
                case WINDOWS -> {
                    config = Path.of(System.getenv("AppData"))
                                 .resolve(APP_FOLDER_NAME);
                }
                case MACOS -> {
                    config = Path.of(System.getProperty("user.home"))
                                 .resolve("Library/Application Support/" + APP_FOLDER_NAME);
                }
            };
        }

        return config;
    }




    private Directories() {}
}
