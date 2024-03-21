package fr.poulpogaz.musicdb.model;

import java.util.HashMap;
import java.util.Map;

public class Music {

    private Template template;
    private String downloadURL;
    private final Map<String, String> tags = new HashMap<>();

    public Music() {

    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
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
