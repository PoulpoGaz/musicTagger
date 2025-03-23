package fr.poulpogaz.musictagger.filenaming;

import java_cup.runtime.Symbol;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LexerTest {

    @Test
    void test1() throws IOException {
        Symbol[] symbols = {
                new Symbol(FParserSym.IDENTIFIER, "?"),
                new Symbol(FParserSym.LPAREN),
                new Symbol(FParserSym.IDENTIFIER, "albumartist"),
                new Symbol(FParserSym.COMMA),
                new Symbol(FParserSym.IDENTIFIER, "artist"),
                new Symbol(FParserSym.RPAREN),
                new Symbol(FParserSym.STRING, "/"),
                new Symbol(FParserSym.IDENTIFIER, "if"),
                new Symbol(FParserSym.LPAREN),
                new Symbol(FParserSym.IDENTIFIER, "albumartist"),
                new Symbol(FParserSym.COMMA),
                new Symbol(FParserSym.IDENTIFIER, "album"),
                new Symbol(FParserSym.STRING, "num"),
                new Symbol(FParserSym.RPAREN),
                new Symbol(FParserSym.IDENTIFIER, "if"),
                new Symbol(FParserSym.LPAREN),
                new Symbol(FParserSym.IDENTIFIER, "eq"),
                new Symbol(FParserSym.LPAREN),
                new Symbol(FParserSym.IDENTIFIER, "cd"),
                new Symbol(FParserSym.COMMA),
                new Symbol(FParserSym.INTEGER, 2),
                new Symbol(FParserSym.RPAREN),
                new Symbol(FParserSym.COMMA),
                new Symbol(FParserSym.STRING, "two cds"),
                new Symbol(FParserSym.COMMA),
                new Symbol(FParserSym.IDENTIFIER, "cd count"),
                new Symbol(FParserSym.RPAREN),
        };

        try (BufferedReader br = Utils.readerFor("test1")) {
            FLexer lexer = new FLexer(br);

            check(lexer, symbols);
        }
    }

    private void check(FLexer lexer, Symbol[] symbols) throws IOException {
        int i = 0;
        while (!lexer.yyatEOF() && i < symbols.length) {
            Symbol symbol = lexer.next_token();

            Assertions.assertEquals(symbols[i].sym, symbol.sym, "Invalid token");
            Assertions.assertEquals(symbols[i].value, symbol.value, "Invalid value");

            i++;
        }

        assertEquals(i, symbols.length, "Number of symbols doesn't match");
    }
}
