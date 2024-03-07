package fr.poulpogaz.musicdb.ui.dialogs;

import fr.poulpogaz.musicdb.MusicDBException;
import fr.poulpogaz.musicdb.model.Key;
import fr.poulpogaz.musicdb.model.Template;
import fr.poulpogaz.musicdb.model.Templates;

import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TemplateModel extends AbstractTableModel {

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final Template template;

    private String name;
    private String format;

    private final List<KeyModel> keys = new ArrayList<>();
    private final List<KeyModel> removedKeys = new ArrayList<>();

    private boolean oldPositionVisible;

    public TemplateModel() {
        this.template = null;
    }

    public TemplateModel(Template template) {
        this.template = template;

        this.name = template.getName();
        this.format = template.getFormat();

        for (int i = 0; i < template.keyCount(); i++) {
            keys.add(new KeyModel(template.getKey(i), i));
        }
    }

    public KeyModel newKey() {
        int index = keys.size();
        KeyModel key = new KeyModel(index);
        keys.add(key);
        fireTableRowsInserted(index, index);
        return key;
    }

    public void removeKey(int index) {
        KeyModel key = keys.remove(index);
        if (key.isDeleted()) {
            return;
        }

        if (key.isNew()) {
            fireTableRowsDeleted(index, index);
        } else {
            key.deleted = true;
            key.index = -1;
            keys.remove(key);
            removedKeys.add(key);

            resetIndex(index);
            fireTableDataChanged();
        }
    }

    public KeyModel restoreKey(int keyRow) {
        int index = keyRow - keys.size();

        if (index >= 0 && index < removedKeys.size()) {
            KeyModel key = removedKeys.get(index);

            key.deleted = false;
            key.index = keys.size();
            removedKeys.remove(keyRow - keys.size());
            keys.add(key);
            fireTableDataChanged();
            return key;
        }

        return null;
    }

    public void revertKeyValue(int keyRow, int column) {
        if (0 <= keyRow && keyRow < getRowCount()
                && 1 <= column && column < getColumnCount()) {
            KeyModel key = keys.get(keyRow);

            if (key.original == null) {
                return;
            }

            if (column == 1) {
                key.setName(key.original.getName());
            } else if (column == 2) {
                key.setName(key.original.getMetadataKey());
            }
        }
    }

    public boolean swap(int i, int j) {
        if (i < 0 || i >= keys.size() || j < 0 || j >= keys.size() || i == j) {
            return false; // Swapping is only allowed between keys that aren't deleted
        }

        KeyModel keyI = keys.get(i);
        KeyModel keyJ = keys.get(j);
        keys.set(i, keyJ);
        keys.set(j, keyI);
        keyI.index = j;
        keyJ.index = i;

        if (i < j) {
            fireTableRowsUpdated(i, j);
        } else {
            fireTableRowsUpdated(j, i);
        }

        return true;
    }

    public boolean moveUp(int index) {
        return swap(index - 1, index);
    }

    public boolean moveDown(int index) {
        return swap(index, index + 1);
    }

    private void resetIndex(int startInclusive) {
        for (int i = startInclusive; i < keys.size(); i++) {
            keys.get(i).index = i;
        }
    }

    @Override
    public int getRowCount() {
        return keys.size() + removedKeys.size();
    }

    @Override
    public int getColumnCount() {
        return oldPositionVisible ? 4 : 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        KeyModel row = getKeyModel(rowIndex);

        return switch (columnIndex) {
            case 0 -> {
                if (row.isDeleted()) {
                    yield "Removed";
                } else {
                    yield rowIndex + 1;
                }
            }
            case 1 -> row.getName();
            case 2 -> row.getMetadataKey();
            default -> {
                if (columnIndex == 3 && oldPositionVisible) {
                    if (row.hasBeenMoved()) {
                        yield row.originalIndex + 1;
                    } else {
                        yield "";
                    }
                }
                throw new IllegalStateException("Unexpected value: " + columnIndex);
            }
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 1 && aValue != null) {
            getKeyModel(rowIndex).setName(aValue.toString());
        } else if (columnIndex == 2) {
            getKeyModel(rowIndex).setMetadataKey(aValue == null ? null : aValue.toString());
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1 || columnIndex == 2;
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> "Order";
            case 1 -> "Name";
            case 2 -> "Metadata key";
            default -> {
                if (column == 3 && oldPositionVisible) {
                    yield "Old position";
                }
                throw new IllegalStateException("Unexpected value: " + column);
            }
        };
    }

    public KeyModel getKeyModel(int index) {
        if (index < keys.size()) {
            return keys.get(index);
        } else {
            return removedKeys.get(index - keys.size());
        }
    }

    public int getFirstDeletedKeyIndex() {
        return keys.size();
    }

    public boolean isOldPositionVisible() {
        return oldPositionVisible;
    }

    public void setOldPositionVisible(boolean oldPositionVisible) {
        if (oldPositionVisible != this.oldPositionVisible) {
            this.oldPositionVisible = oldPositionVisible;
            fireTableStructureChanged();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!Objects.equals(name, this.name)) {
            String old = this.name;
            this.name = name;
            propertyChangeSupport.firePropertyChange("name", old, name);
        }
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        if (!Objects.equals(format, this.format)) {
            String old = this.format;
            this.format = format;
            propertyChangeSupport.firePropertyChange("format", old, format);
        }
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(property, listener);
    }

    public String[] checkValid() {
        String nameError = checkTemplateName();
        String keyError = checkKeys();

        if (nameError != null || keyError != null) {
            return new String[] {nameError, keyError};
        } else {
            return null;
        }
    }

    public void assertValid() {
        String nameError = checkTemplateName();
        if (nameError != null) {
            throw new MusicDBException(nameError);
        }

        String keyError = checkKeys();
        if (keyError != null) {
            throw new MusicDBException(keyError);
        }
    }

    protected String checkTemplateName() {
        if (name == null || name.isBlank()) {
            return "Name is required";
        }

        Template t = Templates.getTemplate(name);
        if (t != null && t != template) {
            return "Name already taken";
        }

        return null;
    }

    protected String checkKeys() {
        if (keys.isEmpty()) {
            return "A template should have at least one key";
        }

        StringBuilder sb = null;
        for (int i = 0; i < keys.size(); i++) {
            KeyModel a = keys.get(i);

            if (a.getName() == null) {
                sb = Objects.requireNonNullElseGet(sb, StringBuilder::new)
                        .append("Key n°").append(i + 1).append(" is unnamed.\n");
                continue;
            }

            for (int j = i + 1; j < keys.size(); j++) {
                KeyModel b = keys.get(j);

                if (b.getName() != null && a.getName().equals(b.getName())) {
                    sb = Objects.requireNonNullElseGet(sb, StringBuilder::new)
                            .append("Keys n°").append(i + 1).append(" and n°").append(j + 1)
                            .append(" have the same name.\n");
                }
            }
        }

        if (sb == null) {
            return null;
        } else {
            return sb.toString();
        }
    }

    public void applyChanges() {
        if (template == null) {
            throw new IllegalStateException("New template");
        }

        template.setName(name);
        template.setFormat(format);

        // remove keys
        for (KeyModel k : removedKeys) {
            template.removeKey(k.originalIndex);
        }

        // insert keys or update keys
        for (KeyModel k : keys) {
            if (k.isNew()) {
                template.addKey(k.index, k.asKey());
            } else {
                k.updateKey();
            }
        }

        // swap keys
        for (int i = 0; i < template.keyCount(); i++) {
            KeyModel kModel = keys.get(i);
            Key key = template.getKey(i);

            if (kModel.isNew() || kModel.getOriginal() == key) {
                continue;
            }

            template.swap(i, kModel.index);
        }
    }


    public Template getTemplate() {
        return template;
    }

    public int getId() {
        return template == null ? -1 : 0;
    }

    public boolean hasNameChanged() {
        return template != null && !Objects.equals(name, template.getName());
    }

    public boolean hasFormatChanged() {
        return template != null && !Objects.equals(format, template.getFormat());
    }

    public class KeyModel {

        private final Key original;
        private final int originalIndex;

        private int index;
        private String name;
        private String metadataKey;

        private boolean deleted;

        public KeyModel(int index) {
            original = null;
            originalIndex = -1;

            if (index < 0) {
                throw new IllegalArgumentException("negative index");
            }

            this.index = index;
        }

        public KeyModel(Key original, int originalIndex) {
            this.original = Objects.requireNonNull(original);
            if (originalIndex < 0) {
                throw new IllegalArgumentException("negative index");
            }

            this.originalIndex = originalIndex;
            this.index = originalIndex;
            this.name = original.getName();
            this.metadataKey = original.getMetadataKey();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            if (!isDeleted() && !Objects.equals(name, this.name)) {
                this.name = name;
                fireTableCellUpdated(index, 1);
            }
        }

        public String getMetadataKey() {
            return metadataKey;
        }

        public void setMetadataKey(String metadataKey) {
            if (!isDeleted() && !Objects.equals(metadataKey, this.metadataKey)) {
                this.metadataKey = metadataKey;
                fireTableCellUpdated(index, 2);
            }
        }

        public Key asKey() {
            Key key = new Key(name);
            key.setMetadataKey(metadataKey);

            return key;
        }

        public Key updateKey() {
            if (original == null) {
                throw new IllegalStateException();
            }


            original.setName(name);
            original.setMetadataKey(metadataKey);

            return original;
        }

        public Key getOriginal() {
            return original;
        }

        public boolean isNew() {
            return original == null;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public boolean hasBeenMoved() {
            return !isNew() && !isDeleted() && originalIndex != index;
        }

        public boolean hasChanged(int column) {
            if (isNew() || isDeleted()) {
                return false;
            } if (column == 1) {
                return !Objects.equals(name, original.getName());
            } else if (column == 2) {
                return !Objects.equals(metadataKey, original.getMetadataKey());
            } else {
                return false;
            }
        }

        public boolean hasChanges() {
            return !isNew() && !isDeleted() && (hasBeenMoved() || hasChanged(1) || hasChanged(2));
        }
    }
}
