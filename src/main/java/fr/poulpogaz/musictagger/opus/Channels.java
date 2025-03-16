package fr.poulpogaz.musictagger.opus;

public enum Channels {

    MONO("Mono"),
    STEREO("Stereo"),
    LINEAR_SURROUND("Linear surround"),
    QUADRAPHONIC("Quadraphonic"),
    SURROUND_5_0("5.0 surround"),
    SURROUND_5_1("5.1 surround"),
    SURROUND_6_1("6.1 surround"),
    SURROUND_7_1("7.1 surround"),
    UNKNOWN("Unknown");

    private final String name;

    Channels(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
