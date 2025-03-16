package fr.poulpogaz.musictagger.utils;

public enum OS {

    WINDOWS,
    MACOS,
    UNIX;

    private static OS os;

    public static OS getOS() {
        if (os == null) {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.startsWith("mac os x")) {
                os = MACOS;
            } else if (osName.startsWith("windows")) {
                os = WINDOWS;
            } else {
                os = UNIX;
            }
        }

        return os;
    }
}
