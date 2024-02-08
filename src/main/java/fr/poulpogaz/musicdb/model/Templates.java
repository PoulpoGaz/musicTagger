package fr.poulpogaz.musicdb.model;

import fr.poulpogaz.json.IJsonWriter;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.JsonPrettyWriter;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonElement;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Templates {

    private static final Map<String, Template> templates = new HashMap<>();
    private static final List<TemplateListener> listeners = new ArrayList<>();


    private Templates() {}

    public static void readTemplates(Path out) throws JsonException, IOException {
        if (Files.notExists(out) || Files.isDirectory(out)) {
            return;
        }

        JsonObject o = (JsonObject) JsonTreeReader.read(Files.newBufferedReader(out));

        for (Map.Entry<String, JsonElement> entry : o.entrySet()) {
            Template template = new Template();
            template.setName(entry.getKey());

            JsonObject templateO = (JsonObject) entry.getValue();
            template.setFormat(templateO.getAsString("format"));
            JsonArray keys = templateO.getAsArray("keys");

            for (JsonElement e : keys) {
                JsonObject keyO = (JsonObject) e;

                Key key = new Key();
                key.setName(keyO.getAsString("name"));
                key.setMetadataKey(keyO.getAsString("metadataKey"));
                template.addKey(key);
            }

            addTemplate(template);
        }
    }





    public static void saveTemplates(Path in) throws JsonException, IOException {
        IJsonWriter jw = new JsonPrettyWriter(Files.newBufferedWriter(in));

        jw.beginObject();
        for (Map.Entry<String, Template> entry : templates.entrySet()) {
            jw.key(entry.getKey()).beginObject();
            saveTemplate(jw, entry.getValue());
            jw.endObject();
        }
        jw.endObject();

        jw.close();
    }

    private static void saveTemplate(IJsonWriter jw, Template template) throws JsonException, IOException {
        jw.field("format", template.getFormat());
        jw.key("keys").beginArray();

        for (Key key : template.getKeys()) {
            jw.beginObject();
            jw.field("name", key.getName());
            jw.field("metadataKey", key.getMetadataKey());
            jw.endObject();
        }

        jw.endArray();
    }



    public static Template getTemplate(String name) {
        return templates.get(name);
    }

    public static void addTemplate(Template template) {
        if (templates.containsKey(template.getName())) {
            return;
        }

        templates.put(template.getName(), template);
        fireEvent(new TemplateEvent(Templates.class, template, EnumSet.of(TemplateEvent.Flags.TEMPLATE_CREATED)));
    }

    public static void removeTemplate(Template template) {
        templates.remove(template.getName());

        fireEvent(new TemplateEvent(Templates.class, template, EnumSet.of(TemplateEvent.Flags.TEMPLATE_DELETED)));
    }

    public static Collection<Template> getTemplates() {
        return templates.values();
    }

    public static int templateCount() {
        return templates.size();
    }


    private static void fireEvent(TemplateEvent event) {
        for (TemplateListener listener : listeners) {
            listener.onTemplateModification(event);
        }
    }


    public static void addTemplateListener(TemplateListener listener) {
        listeners.add(listener);
    }

    public static void removeTemplateListener(TemplateListener listener) {
        listeners.remove(listener);
    }
}
