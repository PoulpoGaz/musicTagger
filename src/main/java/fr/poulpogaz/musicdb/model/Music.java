package fr.poulpogaz.musicdb.model;

import java.util.HashMap;
import java.util.Map;

public class Music {

    private String downloadURL;
    private final Map<String, String> tags = new HashMap<>();

    public Music() {

    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getTag(String tag) {
        return tags.get(tag);
    }

    public String putTag(String tag, String value) {
        return tags.put(tag, value);
    }
}
