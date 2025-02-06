package fr.poulpogaz.musicdl.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import java.util.HashMap;

public class Icons {

    private static final HashMap<String, FlatSVGIcon> ICONS = new HashMap<>();

    public static FlatSVGIcon get(String name) {
        return ICONS.computeIfAbsent(name, n -> new FlatSVGIcon("icons/" + n));
    }

    public static FlatSVGIcon get(String name, int size) {
        String key = name + size;
        return ICONS.computeIfAbsent(key, _ -> new FlatSVGIcon("icons/" + name, size, size));
    }
}