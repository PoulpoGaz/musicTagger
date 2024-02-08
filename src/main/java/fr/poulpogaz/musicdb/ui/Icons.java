package fr.poulpogaz.musicdb.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import java.util.HashMap;

public class Icons {

    private static final HashMap<String, FlatSVGIcon> ICONS = new HashMap<>();

    public static FlatSVGIcon get(String name) {
        FlatSVGIcon icon = ICONS.get(name);

        if (icon == null) {
            icon = new FlatSVGIcon("icons/" + name);

            ICONS.put(name, icon);
        }

        return icon;
    }

    public static FlatSVGIcon get(String name, int size) {
        String key = name + size;

        FlatSVGIcon icon = ICONS.get(key);

        if (icon == null) {
            icon = new FlatSVGIcon("icons/" + name, size, size);

            ICONS.put(key, icon);
        }

        return icon;
    }
}