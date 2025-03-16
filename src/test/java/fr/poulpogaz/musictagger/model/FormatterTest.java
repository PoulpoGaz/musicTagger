package fr.poulpogaz.musictagger.model;

public class FormatterTest {

    /*@Test
    void simpleTest() {
        Music m = new Music();
        m.setTag("world", "world");

        Formatter f = new Formatter();
        f.setFormat("Hello {key:world}!");
        Assertions.assertEquals("Hello world!", f.format(m));
    }

    @Test
    void adjacentTest() {
        Music m = new Music();
        m.setTag("cu", "cu");

        Formatter f = new Formatter();
        f.setFormat("{key:cu}{key:cu}mber!");
        Assertions.assertEquals("cucumber!", f.format(m));
    }

    @Test
    void escapeTest() {
        Music m = new Music();
        m.setTag("{}", "between curly bracket");

        Formatter f = new Formatter();
        f.setFormat("~\\{Text {key:{\\}}\\}~");
        Assertions.assertEquals("~{Text between curly bracket}~", f.format(m));
    }

    @Test
    void valueNullTest() {
        Music m = new Music();

        Formatter f = new Formatter();
        f.setFormat("This value is {key:k}");
        Assertions.assertEquals("This value is null", f.format(m));
    }

    @Test
    void illegalFormatTest() {
        Formatter f = new Formatter();
        f.setFormat("{func:v}");
        Assertions.assertThrows(IllegalFormatException.class, f::compile);
        f.setFormat("{key:ff");
        Assertions.assertThrows(IllegalFormatException.class, f::compile);
        f.setFormat("{key");
        Assertions.assertThrows(IllegalFormatException.class, f::compile);
        f.setFormat("{");
        Assertions.assertThrows(IllegalFormatException.class, f::compile);
        f.setFormat("{key:}");
        Assertions.assertThrows(IllegalFormatException.class, f::compile);
    }*/
}
