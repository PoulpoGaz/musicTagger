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

    public String setTag(String tag, String value) {
        if (value == null) {
            return tags.remove(tag);
        } else {
            return tags.put(tag, value);
        }
    }
}
