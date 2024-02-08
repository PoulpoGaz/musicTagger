package fr.poulpogaz.musicdb.model;

import java.util.EnumSet;
import java.util.EventObject;

public class TemplateEvent extends EventObject {

    public enum Flags {
        TEMPLATE_CREATED, // new template
        TEMPLATE_DELETED, // template deleted
        TEMPLATE_MODIFIED, // template name or format modified
        TEMPLATE_STRUCTURE_CHANGED, // new key, deleted key or moved key
        TEMPLATE_KEY_MODIFIED // at least one key is modified
    }

    private final Template template;
    private final EnumSet<Flags> flags;

    public TemplateEvent(Object source, Template template, EnumSet<Flags> flags) {
        super(source);
        this.template = template;
        this.flags = flags;
    }

    public Template getTemplate() {
        return template;
    }

    public boolean isNewTemplate() {
        return flags.contains(Flags.TEMPLATE_CREATED);
    }

    public boolean isTemplateDeleted() {
        return flags.contains(Flags.TEMPLATE_DELETED);
    }

    public boolean isTemplateModified() {
        return flags.contains(Flags.TEMPLATE_MODIFIED);
    }

    public boolean isTemplateStructureChanged() {
        return flags.contains(Flags.TEMPLATE_STRUCTURE_CHANGED);
    }

    public boolean isAKeyModified() {
        return flags.contains(Flags.TEMPLATE_KEY_MODIFIED);
    }
}
