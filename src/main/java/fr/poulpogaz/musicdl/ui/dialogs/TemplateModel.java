package fr.poulpogaz.musicdl.ui.dialogs;

import fr.poulpogaz.musicdl.MusicdlException;
import fr.poulpogaz.musicdl.model.Key;
import fr.poulpogaz.musicdl.model.Template;
import fr.poulpogaz.musicdl.model.Templates;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Objects;

public class TemplateModel {

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final Template template;

    private String name;
    private String format;

    private final KeyTableModel keyTable = new KeyTableModel();
    private final MetadataGeneratorTableModel metadataGeneratorTable = new MetadataGeneratorTableModel();

    public TemplateModel() {
        this.template = null;
    }

    public TemplateModel(Template template) {
        this.template = template;

        this.name = template.getName();
        this.format = template.getFormat();

        for (int i = 0; i < template.keyCount(); i++) {
            keyTable.newRow(new KeyRow(template.getKey(i), i));
        }

        for (Template.MetadataGenerator g : template.getGenerators()) {
            metadataGeneratorTable.newRow(new MetadataGeneratorRow(g));
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

    public boolean hasNameChanged() {
        return template != null && !Objects.equals(name, template.getName());
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

    public boolean hasFormatChanged() {
        return template != null && !Objects.equals(format, template.getFormat());
    }



    public void newKey() {
        keyTable.newRow();
    }

    public void removeKey(int row) {
        keyTable.removeRow(row);
    }

    public void restoreKey(int row) {
        keyTable.restoreRow(row);
    }

    public void revertKeyValue(int row, int column) {
        keyTable.revert(row, column);
    }

    public boolean swapKeys(int rowI, int rowJ) {
        return keyTable.swapRows(rowI, rowJ);
    }

    public boolean moveKeyUp(int row) {
        return swapKeys(row - 1, row);
    }

    public boolean moveKeyDown(int row) {
        return swapKeys(row, row + 1);
    }

    public KeyTableModel getKeyTableModel() {
        return keyTable;
    }



    public MetadataGeneratorTableModel getMetadataGeneratorTableModel() {
        return metadataGeneratorTable;
    }



    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(property, listener);
    }

    public void assertValid() {
        String nameError = checkTemplateName();
        if (nameError != null) {
            throw new MusicdlException(nameError);
        }

        String keyError = checkKeys();
        if (keyError != null) {
            throw new MusicdlException(keyError);
        }
    }

    public String checkTemplateName() {
        if (name == null || name.isBlank()) {
            return "Name is required";
        }

        if (hasNameChanged() && Templates.isNameInternal(name)) {
            return "Cannot use internal name";
        }

        Template t = Templates.getTemplate(name);
        if (t != null && t != template) {
            return "Name already taken";
        }

        return null;
    }

    public String checkKeys() {
        int keyCount = keyTable.notRemovedRowCount();
        if (keyCount == 0) {
            return "A template should have at least one key";
        }

        StringBuilder sb = null;
        for (int i = 0; i < keyCount; i++) {
            KeyRow a = keyTable.getRow(i);

            if (a.getName() == null) {
                sb = Objects.requireNonNullElseGet(sb, StringBuilder::new)
                        .append("Key n°").append(i + 1).append(" is unnamed.\n");
                continue;
            }

            for (int j = i + 1; j < keyCount; j++) {
                KeyRow b = keyTable.getRow(j);

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

    public void apply() {
        if (template == null) {
            createNewTemplate();
        } else {
            applyChanges();
        }
    }

    private void createNewTemplate() {
        Template template = new Template();
        template.setName(name);
        template.setFormat(format);

        for (int i = 0; i < keyTable.getRowCount(); i++) {
            TemplateModel.KeyRow key = keyTable.getRow(i);
            if (key.isRemoved()) {
                break;
            }

            if (!template.addKey(key.asKey())) {
                throw new MusicdlException("Cannot create template: key " + key + " wasn't added to template " + template);
            }
        }

        for (int i = 0; i < metadataGeneratorTable.getRowCount(); i++) {
            MetadataGeneratorRow row = metadataGeneratorTable.getRow(i);
            template.addMetadataGenerator(new Template.MetadataGenerator(row.getKey(), row.getValue()));
        }

        Templates.addTemplate(template);
    }

    private void applyChanges() {
        template.setName(name);
        template.setFormat(format);

        // update existing keys
        int i;
        int lim = Math.min(template.keyCount(), keyTable.notRemovedRowCount());
        for (i = 0; i < lim; i++) {
            Key key = template.getKey(i);
            KeyRow row = keyTable.getRow(i);

            int i2 = template.indexOfKey(row.getName());
            if (i2 >= 0 && i2 != i) { // template already contains a key with the same name
                template.swap(i, i2);
                key = template.getKey(i);
            } else {
                key.setName(row.getName());
            }

            key.setMetadataField(row.getMetadataKey());
        }

        for (; i < keyTable.notRemovedRowCount(); i++) {
            template.addKey(keyTable.getRow(i).asKey());
        }

        // remove keys
        while (template.keyCount() > keyTable.notRemovedRowCount()) {
            template.removeKey(template.keyCount() - 1);
        }

        // update generators
        List<Template.MetadataGenerator> gens = template.getGenerators();
        lim = Math.min(gens.size(), metadataGeneratorTable.getRowCount());
        for (i = 0; i < lim; i++) {
            Template.MetadataGenerator gen = gens.get(i);
            MetadataGeneratorRow row = metadataGeneratorTable.getRow(i);

            gen.setKey(row.getKey());
            gen.setValue(row.getValue());
        }

        for (; i < metadataGeneratorTable.notRemovedRowCount(); i++) {
            MetadataGeneratorRow row = metadataGeneratorTable.getRow(i);
            gens.add(new Template.MetadataGenerator(row.getKey(), row.getValue()));
        }

        // remove generators
        while (gens.size() > metadataGeneratorTable.notRemovedRowCount()) {
            gens.removeLast();
        }


        template.fireTemplateKeysEvent();
    }


    public Template getTemplate() {
        return template;
    }


    public static class KeyTableModel extends RestoreTableModel<KeyRow> {

        private boolean oldPositionVisible;

        @Override
        protected KeyRow createRow() {
            return new KeyRow();
        }

        @Override
        public int getColumnCount() {
            return oldPositionVisible ? 4 : 3;
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

        public boolean isOldPositionVisible() {
            return oldPositionVisible;
        }

        public void setOldPositionVisible(boolean oldPositionVisible) {
            if (oldPositionVisible != this.oldPositionVisible) {
                this.oldPositionVisible = oldPositionVisible;
                fireTableStructureChanged();
            }
        }
    }

    public static class KeyRow extends RestoreTableModel.Row {

        private final Key original;
        private final int originalIndex;

        private String name;
        private String metadataKey;

        public KeyRow() {
            original = null;
            originalIndex = -1;
        }

        public KeyRow(Key original, int originalIndex) {
            this.original = Objects.requireNonNull(original);
            if (originalIndex < 0) {
                throw new IllegalArgumentException("negative index");
            }

            this.originalIndex = originalIndex;
            this.name = original.getName();
            this.metadataKey = original.getMetadataField();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            if (setName0(name)) {
                table.fireTableCellUpdated(index, 1);
            }
        }

        private boolean setName0(String name) {
            if (!isRemoved() && !Objects.equals(name, this.name)) {
                this.name = name;
                return true;
            }
            return false;
        }

        public String getMetadataKey() {
            return metadataKey;
        }

        public void setMetadataKey(String metadataKey) {
            if (setMetadataKey0(metadataKey)) {
                table.fireTableCellUpdated(index, 2);
            }
        }

        private boolean setMetadataKey0(String metadataKey) {
            if (!isRemoved() && !Objects.equals(metadataKey, this.metadataKey)) {
                this.metadataKey = metadataKey;
                return true;
            }
            return false;
        }

        public Key asKey() {
            Key key = new Key(name);
            key.setMetadataField(metadataKey);

            return key;
        }

        public void updateKey() {
            if (original == null) {
                throw new IllegalStateException();
            }

            original.setName(name);
            original.setMetadataField(metadataKey);
        }

        public Key getOriginal() {
            return original;
        }

        @Override
        public Object getValue(int column) {
            return switch (column) {
                case 0 -> isRemoved() ? "Removed" : (index + 1);
                case 1 -> name;
                case 2 -> metadataKey;
                default -> {
                    if (column == 3 && ((KeyTableModel) table).oldPositionVisible) {
                        if (hasBeenMoved()) {
                            yield originalIndex + 1;
                        } else {
                            yield "";
                        }
                    }
                    throw new IllegalStateException("Unexpected value: " + column);
                }
            };
        }

        @Override
        public void setValue(Object value, int column) {
            if (value == null) {
                value = "";
            }

            if (column == 1) {
                setName(value.toString());
            } else if (column == 2) {
                setMetadataKey(value.toString());
            }
        }

        @Override
        public boolean revert(int column) {
            if (column == 1) {
                return setName0(original.getName());
            } else if (column == 2) {
                return setMetadataKey0(original.getMetadataField()) ;
            } else {
                return false;
            }
        }

        @Override
        public boolean isNew() {
            return original == null;
        }

        @Override
        public boolean hasChanged(int column) {
            if (isNew() || isRemoved()) {
                return false;
            } if (column == 1) {
                return !Objects.equals(name, original.getName());
            } else if (column == 2) {
                return !Objects.equals(metadataKey, original.getMetadataField());
            } else {
                return false;
            }
        }

        public boolean hasBeenMoved() {
            return !isNew() && !isRemoved() && originalIndex != index;
        }
    }


    public static class MetadataGeneratorTableModel extends RestoreTableModel<MetadataGeneratorRow> {

        public MetadataGeneratorRow createRow() {
            return new MetadataGeneratorRow();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Metadata key";
                case 1 -> "Value";
                default -> throw new IllegalStateException("Unexpected value: " + column);
            };
        }
    }

    public static class MetadataGeneratorRow extends RestoreTableModel.Row {

        private final Template.MetadataGenerator original;

        private String key;
        private String value;

        public MetadataGeneratorRow() {
            this(null);
        }

        public MetadataGeneratorRow(Template.MetadataGenerator original) {
            this.original = original;

            if (original != null) {
                key = original.getKey();
                value = original.getValue();
            }
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            if (setKey0(key)) {
                table.fireTableCellUpdated(index, 0);
            }
        }

        public boolean setKey0(String key) {
            if (!isRemoved() && !Objects.equals(key, this.key)) {
                this.key = key;
                return true;
            }
            return false;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            if (setValue0(value)) {
                table.fireTableCellUpdated(index, 1);
            }
        }

        private boolean setValue0(String value) {
            if (!isRemoved() && !Objects.equals(value, this.value)) {
                this.value = value;
                return true;
            }
            return false;
        }

        @Override
        public Object getValue(int column) {
            return switch (column) {
                case 0 -> key;
                case 1 -> value;
                default -> throw new IndexOutOfBoundsException(column);
            };
        }

        @Override
        public void setValue(Object value, int column) {
            if (column == 0) {
                setKey((String) value);
            } else if (column == 1) {
                setValue((String) value);
            }
        }

        @Override
        public boolean revert(int column) {
            if (column == 0) {
                return setKey0(original.getKey());
            } else if (column == 1) {
                return setValue0(original.getValue());
            }
            return false;
        }

        public boolean isNew() {
            return original == null;
        }

        public boolean isRemoved() {
            return removed;
        }

        public boolean hasChanged(int column) {
            if (isNew() || isRemoved()) {
                return false;
            } if (column == 0) {
                return !Objects.equals(key, original.getKey());
            } else if (column == 1) {
                return !Objects.equals(value, original.getValue());
            } else {
                return false;
            }
        }
    }
}
