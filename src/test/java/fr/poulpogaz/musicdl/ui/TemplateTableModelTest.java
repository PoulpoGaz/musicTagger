package fr.poulpogaz.musicdl.ui;

import fr.poulpogaz.musicdl.model.Key;
import fr.poulpogaz.musicdl.model.Template;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemplateTableModelTest {

    private Template createTemplate(String name, int nKeys) {
        Template template = new Template();
        template.setName(name);

        for (int i = 0; i < nKeys; i++) {
            template.addKey(new Key(Character.toString('a' + i)));
        }

        return template;
    }


    @Test
    void addRemoveTest() {
        Template template = createTemplate("t", 5);
        TemplateTableModel model = new TemplateTableModel(template);
        Assertions.assertEquals(6, model.getColumnCount());
        Assertions.assertEquals(0, model.getRowCount());

        model.newRow();
        Assertions.assertEquals(1, model.getRowCount());
        set(model, 0, "a", "b", "c", "d", "e", "f");

        model.newRow(0);
        Assertions.assertEquals(2, model.getRowCount());
        assertContentEquals(model,
                            null, null, null, null, null, null,
                            "a", "b", "c", "d", "e", "f");

        model.removeRow(1);
        Assertions.assertEquals(1, model.getRowCount());
        assertContentEquals(model,
                            null, null, null, null, null, null);
    }


    @Test
    void removeKeyTest() {
        Template template = createTemplate("t", 4);
        TemplateTableModel model = new TemplateTableModel(template);

        model.newRow();
        model.newRow();
        model.newRow();
        model.newRow();
        model.newRow();
        set(model, 0, "a");
        set(model, 1, null, "b");
        set(model, 2, null, null, "c");
        set(model, 3, null, null, null, "d");
        set(model, 4, null, null, null, null, "e");
        assertContentEquals(model,
                            "a", null, null, null, null,
                            null, "b", null, null, null,
                            null, null, "c", null, null,
                            null, null, null, "d", null,
                            null, null, null, null, "e");

        template.removeKey(2);
        assertContentEquals(model,
                            "a", null, null, null,
                            null, "b", null, null,
                            null, null, "c", null,
                            null, null, null, null,
                            null, null, null, "e");
    }

    @Test
    void addKeyTest() {
        Template template = createTemplate("t", 4);
        TemplateTableModel model = new TemplateTableModel(template);

        model.newRow();
        model.newRow();
        model.newRow();
        model.newRow();
        model.newRow();
        set(model, 0, "a");
        set(model, 1, null, "b");
        set(model, 2, null, null, "c");
        set(model, 3, null, null, null, "d");
        set(model, 4, null, null, null, null, "e");
        assertContentEquals(model,
                            "a", null, null, null, null,
                            null, "b", null, null, null,
                            null, null, "c", null, null,
                            null, null, null, "d", null,
                            null, null, null, null, "e");

        template.addKey(1, new Key("hello"));
        assertContentEquals(model,
                            "a", null, null, null, null, null,
                            null, "b", null, null, null, null,
                            null, null, null, "c", null, null,
                            null, null, null, null, "d", null,
                            null, null, null, null, null, "e");
    }


    @Test
    void swapKeyTest() {
        Template template = createTemplate("t", 4);
        TemplateTableModel model = new TemplateTableModel(template);

        model.newRow();
        model.newRow();
        model.newRow();
        model.newRow();
        model.newRow();
        set(model, 0, "a");
        set(model, 1, null, "b");
        set(model, 2, null, null, "c");
        set(model, 3, null, null, null, "d");
        set(model, 4, null, null, null, null, "e");
        assertContentEquals(model,
                            "a", null, null, null, null,
                            null, "b", null, null, null,
                            null, null, "c", null, null,
                            null, null, null, "d", null,
                            null, null, null, null, "e");

        template.swap(1, 3);
        assertContentEquals(model,
                            "a", null, null, null, null,
                            null, "b", null, null, null,
                            null, null, null, null, "c",
                            null, null, null, "d", null,
                            null, null, "e", null, null);
    }


    private void assertContentEquals(TemplateTableModel model, String... values) {
        String[][] content = model.getContent();

        int i = 0;
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                Assertions.assertTrue(i < values.length);
                Assertions.assertEquals(values[i], content[row][col]);
                i++;
            }
        }

        Assertions.assertEquals(i, values.length);
    }

    private void set(TemplateTableModel model, int row, String... values) {
        set(model, row, 0, values.length, values);
    }

    private void set(TemplateTableModel model, int row, int min, int max, String... values) {
        min = Math.max(0, min);
        max = Math.min(model.getColumnCount(), max);
        if (max - min > values.length) {
            max = min + values.length;
        }

        for (int col = min; col < max; col++) {
            model.setValueAt(values[col], row, col);
        }
    }
}
