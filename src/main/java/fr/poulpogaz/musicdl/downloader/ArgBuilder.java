package fr.poulpogaz.musicdl.downloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArgBuilder {

    private final List<String> commands = new ArrayList<>();

    public ArgBuilder() {
        commands.add("yt-dlp");
    }

    public ArgBuilder add(String arg) {
        commands.add(arg);
        return this;
    }

    public ArgBuilder add(String... args) {
        Collections.addAll(commands, args);

        return this;
    }

    public List<String> getCommands() {
        return commands;
    }
}
