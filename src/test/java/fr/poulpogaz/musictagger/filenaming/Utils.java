package fr.poulpogaz.musictagger.filenaming;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Utils {

    public static BufferedReader readerFor(String file) {
        InputStream is = LexerTest.class.getResourceAsStream(file);
        assertNotNull(is);
        return new BufferedReader(new InputStreamReader(is));
    }
}
