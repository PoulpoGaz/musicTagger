package fr.poulpogaz.musictagger.downloader;

import fr.poulpogaz.musictagger.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class YTDLP {

    private final String url;

    private final BooleanOption windowsFilenames = new BooleanOption(false, "--windows-filenames", "--no-windows-filenames");
    private final BooleanOption embedThumbnail   = new BooleanOption(true, "--embed-thumbnail", "--no-embed-thumbnail");
    private final BooleanOption playlistDownload = new BooleanOption(false, "--yes-playlist", "--no-playlist");
    private final BooleanOption abortOnError     = new BooleanOption(true, "--abort-on-error", "--no-abort-on-error");
    private final WriteThumbnail writeThumbnail = new WriteThumbnail();
    private final Overwrites overwrites = new Overwrites();
    private final StringOption output = new StringOption("--output");

    private List<String> options;

    public YTDLP(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.url = url;
    }

    public YTDLP useWindowsFilenames(boolean enable) {
        windowsFilenames.enable(enable);
        return this;
    }

    public YTDLP embedThumbnail(boolean enable) {
        embedThumbnail.enable(enable);
        return this;
    }

    public YTDLP writeThumbnails(boolean enable, String path, String format) {
        writeThumbnail.set(enable, path, format);
        return this;
    }

    public YTDLP allowPlaylistDownload(boolean allowed) {
        playlistDownload.enable(allowed);
        return this;
    }

    public YTDLP noOverwrites() {
        overwrites.set(Overwrites.NO_OVERWRITES);
        return this;
    }

    public YTDLP forceOverwrites() {
        overwrites.set(Overwrites.FORCE_OVERWRITES);
        return this;
    }

    public YTDLP noForceOverwrites() {
        overwrites.set(Overwrites.NO_FORCE_OVERWRITES);
        return this;
    }

    public YTDLP abortOnError(boolean enabled) {
        abortOnError.enable(enabled);
        return this;
    }

    public YTDLP setOutput(String output) {
        this.output.set(output);
        return this;
    }

    public String getOutput() {
        return output.value;
    }



    public YTDLP setMetadata(String key, String value) {
        // " {value}: %(meta_{key})s"
        // This tries to copy the left part into the right part
        // therefore the first space is copied into the second space
        // and {value} is copied into %(meta_{key})s".
        // Without the space, yt-dlp thinks that {key} is a
        // field (ie translates {value} into %({value})s) but
        // %({value})s) doesn't refer to any string, so metadata
        // isn't written...
        String valueE = Utils.escapeCharacter(value, ':', '\\');
        addOption("--parse-metadata", " " + valueE + ": %(meta_" + key + ")s");
        return this;
    }




    public YTDLP addOption(String option) {
        if (options == null) {
            options = new ArrayList<>();
        }

        options.add(option);
        return this;
    }

    public YTDLP addOption(String option, String value) {
        if (options == null) {
            options = new ArrayList<>();
        }

        options.add(option);
        options.add(value);
        return this;
    }

    public List<String> getOptions() {
        List<String> options = new ArrayList<>();
        options.add("yt-dlp");
        options.add(url);
        windowsFilenames.populate(options);
        embedThumbnail.populate(options);
        playlistDownload.populate(options);
        abortOnError.populate(options);
        writeThumbnail.populate(options);
        overwrites.populate(options);
        output.populate(options);

        if (this.options != null) {
            options.addAll(this.options);
        }

        return options;
    }

    public ProcessBuilder createProcess() {
        return new ProcessBuilder(getOptions());
    }

    public String getURL() {
        return url;
    }


    public YTDLP copy() {
        YTDLP ytdlp = new YTDLP(url);
        ytdlp.windowsFilenames.enable(windowsFilenames.enabled);
        ytdlp.embedThumbnail.enable(embedThumbnail.enabled);
        ytdlp.playlistDownload.enable(playlistDownload.enabled);
        ytdlp.abortOnError.enable(abortOnError.enabled);

        ytdlp.writeThumbnail.set(writeThumbnail.enabled, writeThumbnail.path, writeThumbnail.format);
        ytdlp.overwrites.set(overwrites.value);
        ytdlp.output.set(output.value);

        if (options != null) {
            ytdlp.options = new ArrayList<>();
            ytdlp.options.addAll(options);
        }

        return ytdlp;
    }


    private interface Option {

        void populate(List<String> options);
    }


    private static class StringOption implements Option {

        private final String name;
        private String value;

        public StringOption(String name) {
            this.name = name;
        }

        public void set(String value) {
            this.value = value;
        }

        @Override
        public void populate(List<String> options) {
            if (value != null && !value.isEmpty()) {
                options.add(name);
                options.add(value);
            }
        }
    }

    private static class BooleanOption implements Option {

        private boolean enabled;
        private final String enabledOption;
        private final String disabledOption;

        public BooleanOption(boolean enabled, String enabledOption, String disabledOption) {
            this.enabled = enabled;
            this.enabledOption = enabledOption;
            this.disabledOption = disabledOption;
        }

        public void enable(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public void populate(List<String> options) {
            options.add(enabled ? enabledOption : disabledOption);
        }
    }


    private static class WriteThumbnail implements Option {

        private boolean enabled = false;
        private String path = null;
        private String format = null;

        public void set(boolean enabled, String path, String format) {
            this.enabled = enabled;
            this.path = path;
            this.format = format;
        }

        @Override
        public void populate(List<String> options) {
            if (enabled) {
                options.add("--write-thumbnail");

                if (path != null) {
                    options.add("--output");
                    options.add("thumbnail:" + path);
                }

                if (format != null) {
                    options.add("--convert-thumbnail");
                    options.add(format);
                }
            }
        }
    }

    private static class Overwrites implements Option {

        private static final int NO_OVERWRITES = 0;
        private static final int FORCE_OVERWRITES = 1;
        private static final int NO_FORCE_OVERWRITES = 2;

        private int value = NO_OVERWRITES;

        public void set(int value) {
            this.value = value;
        }

        @Override
        public void populate(List<String> options) {
            switch (value) {
                case NO_OVERWRITES -> options.add("--no-overwrites");
                case FORCE_OVERWRITES -> options.add("--force-overwrites");
                case NO_FORCE_OVERWRITES -> options.add("--no-force-overwrites");
            }
        }
    }
}
