package fr.poulpogaz.musicdl.model;

import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;
import fr.poulpogaz.musicdl.Directories;
import fr.poulpogaz.musicdl.properties.PropertyListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Templates {

    private static final boolean DEBUG = false;
    private static final String[] URLS = new String[] {
        "https://www.youtube.com/watch?v=yDVEuISEr1Q",
        "https://www.youtube.com/watch?v=Urcnqat6P0s",
        "https://music.youtube.com/watch?v=yKPka2qGRh8",
        "https://music.youtube.com/watch?v=oSgGnmh4sQo",
        "https://music.youtube.com/watch?v=GtPLYvYeZ_4",
        "https://music.youtube.com/watch?v=DNCN1II0G-4",
        "https://music.youtube.com/watch?v=qiLO9YDOxYg",
        "https://music.youtube.com/watch?v=qXoghQAzQF0",
        "https://music.youtube.com/watch?v=7ddebyXI8-Q",
        "https://music.youtube.com/watch?v=SxhJ6pVGaio",
        "https://music.youtube.com/watch?v=XIwtX5aNO4w",
        "https://music.youtube.com/watch?v=VQIv_2249Sc",
        "https://music.youtube.com/watch?v=Sdb4nZgcETI",
        "https://music.youtube.com/watch?v=zoi6ofeC4rY",
        "https://music.youtube.com/watch?v=fhZMRwAs2Ys",
        "https://music.youtube.com/watch?v=26W7rVonsEs",
        "https://music.youtube.com/watch?v=jHVy3kkYiFY",
        "https://music.youtube.com/watch?v=lyzjJYugE3o",
        "https://music.youtube.com/watch?v=YYlcR-hBuXY",
        "https://music.youtube.com/watch?v=kKEkoeUHeyM",
        "https://music.youtube.com/watch?v=QnF41gLkuuE",
        "https://music.youtube.com/watch?v=gegcg4hVN0A"
    };

    private static final String UNASSIGNED_MUSIC_TEMPLATE_NAME = "Unassigned musics";

    private static final Logger LOGGER = LogManager.getLogger(Templates.class);

    private static final Map<String, Template> templates = new HashMap<>();
    private static final List<TemplatesListener> listeners = new ArrayList<>();

    private static final PropertyListener<String> templateNameListener = (_, oldValue, newValue) -> {
        Template t = templates.remove(oldValue);
        if (t != null) {
            templates.put(newValue, t);
        }
    };


    static {
        Template template = new Template();
        template.setName(UNASSIGNED_MUSIC_TEMPLATE_NAME);

        Key title = new Key("Title");
        title.setMetadataKey("title");
        template.addKey(title);

        Key artist = new Key("Artist");
        artist.setMetadataKey("artist");
        template.addKey(artist);


        if (DEBUG) {
            for (int i = 0; i < URLS.length; i++) {
                Music m = new Music();
                template.getData().addMusic(m);

                m.setDownloadURL(URLS[i]);
                m.putTag(0, "Title " + i);
                m.putTag(1, "Artist " + i);
            }
        }


        templates.put(UNASSIGNED_MUSIC_TEMPLATE_NAME, template);
    }

    private Templates() {}

    public static void readTemplates() throws JsonException, IOException {
        readTemplates(Directories.getConfigurationDirectory().resolve("templates.json"));
    }

    public static void saveTemplates() throws JsonException, IOException {
        saveTemplates(Directories.getConfigurationDirectory().resolve("templates.json"));
    }

    public static void readTemplates(Path out) throws JsonException, IOException {
        if (Files.notExists(out) || Files.isDirectory(out)) {
            return;
        }

        JsonObject o = (JsonObject) JsonTreeReader.read(Files.newBufferedReader(out));

        for (Map.Entry<String, JsonElement> entry : o.entrySet()) {
            Template template = new Template();
            template.setName(entry.getKey());

            JsonObject templateObj = (JsonObject) entry.getValue();
            template.setFormat(templateObj.getOptionalString("format").orElse(null));

            JsonArray keys = templateObj.getAsArray("keys");
            for (JsonElement e : keys) {
                JsonObject keyO = (JsonObject) e;

                Key key = new Key();
                key.setName(keyO.getAsString("name"));
                key.setMetadataKey(keyO.getOptionalString("metadataKey").orElse(null));
                template.addKey(key);
            }

            JsonArray generators = templateObj.getAsArray("generators");
            if (generators != null) {
                for (JsonElement e : generators) {
                    JsonObject g = (JsonObject) e;

                    Template.MetadataGenerator gen = new Template.MetadataGenerator();
                    gen.setKey(g.getAsString("key"));
                    gen.setValue(g.getAsString("value"));
                    template.addMetadataGenerator(gen);
                }
            }

            addTemplate(template);
        }
    }

    public static void saveTemplates(Path in) throws JsonException, IOException {
        LOGGER.info("Saving templates to {}", in);
        Files.createDirectories(in.getParent());

        IJsonWriter jw = new JsonPrettyWriter(Files.newBufferedWriter(in));

        jw.beginObject();
        for (Template template : templates.values()) {
            if (template.isInternalTemplate()) {
                continue;
            }

            jw.key(template.getName()).beginObject();
            writeTemplate(jw, template);
            jw.endObject();
        }
        jw.endObject();

        jw.close();
    }

    public static void writeTemplate(IJsonWriter jw, Template template) throws JsonException, IOException {
        if (template.getFormat() != null) {
            jw.field("format", template.getFormat());
        }

        jw.key("keys").beginArray();
        for (Key key : template.getKeys()) {
            jw.beginObject();
            jw.field("name", key.getName());
            if (key.isMetadataKeySet()) {
                jw.field("metadataKey", key.getMetadataKey());
            }
            jw.endObject();
        }
        jw.endArray();

        jw.key("generators").beginArray();
        for (Template.MetadataGenerator gen : template.getGenerators()) {
            jw.beginObject()
              .field("key", gen.getKey())
              .field("value", gen.getValue())
              .endObject();
        }
        jw.endArray();
    }



    public static Template getTemplate(String name) {
        return templates.get(name);
    }

    public static void addTemplate(Template template) {
        if (template.isInternalTemplate() || templates.containsKey(template.getName())) {
            return;
        }

        templates.put(template.getName(), template);
        template.nameProperty().addListener(templateNameListener);
        fireEvent(TemplatesListener.TEMPLATE_ADDED, template);
    }

    public static void removeTemplate(Template template, boolean moveToUnassignedMusics) {
        if (!template.isInternalTemplate() && templates.remove(template.getName(), template)) {
            if (moveToUnassignedMusics) {
                Template unassignedMusics = templates.get(UNASSIGNED_MUSIC_TEMPLATE_NAME);
                template.getData().transferAllTo(unassignedMusics.getData());
            }

            template.nameProperty().removeListener(templateNameListener);
            fireEvent(TemplatesListener.TEMPLATE_REMOVED, template);
        }
    }

    public static boolean isNameInternal(String templateName) {
        return UNASSIGNED_MUSIC_TEMPLATE_NAME.equals(templateName);
    }

    public static Template getDefaultTemplate() {
        return templates.get(UNASSIGNED_MUSIC_TEMPLATE_NAME);
    }

    public static Collection<Template> getTemplates() {
        return templates.values();
    }

    public static int templateCount() {
        return templates.size();
    }

    public static int totalMusicCount() {
        int sum = 0;
        for (Template t : templates.values()) {
            sum += t.getData().getMusicCount();
        }
        return sum;
    }

    private static void fireEvent(int event, Template template) {
        for (TemplatesListener listener : listeners) {
            listener.templatesChanged(event, template);
        }
    }


    public static void addTemplatesListener(TemplatesListener listener) {
        listeners.add(listener);
    }

    public static void removeTemplatesListener(TemplatesListener listener) {
        listeners.remove(listener);
    }
}
