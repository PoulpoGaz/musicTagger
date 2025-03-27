package fr.poulpogaz.musictagger.filenaming;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

%%

%class FLexer
%state STRING, IDENTIFIER
%unicode
%cup
%line
%column

%{
    private final StringBuffer string = new StringBuffer();
    private ComplexSymbolFactory symbolFactory;
    private ErrorHandler error;

    public FLexer(Reader in, ComplexSymbolFactory sf, ErrorHandler error){
        this(in);
        symbolFactory = sf;
        this.error = error;
    }

    private Symbol symbol(String name, int sym) {
        return symbol(name, sym, null);
    }

    private Symbol symbol(String name, int sym, Object val) {
        return symbolFactory.newSymbol(name, sym,
                                       new ComplexSymbolFactory.Location(yyline + 1, yycolumn + 1),
                                       new ComplexSymbolFactory.Location(yyline + 1, yycolumn + yylength()),
                                       val);
    }

    private Symbol stringSymbol(String name, int sym) {
        int strLength = string.length();
        ComplexSymbolFactory.Location left  = new ComplexSymbolFactory.Location(yyline + 1, yycolumn + yylength() - strLength);
        ComplexSymbolFactory.Location right = new ComplexSymbolFactory.Location(yyline + 1, yycolumn + yylength());
        return symbolFactory.newSymbol(name, sym, left, right, string.toString());
    }
%}


Number = 0|[1-9][0-9]*
// without '=', '$', '\\'
x30_x7D_BetweenDollar = [ !\"#%&'()*+,\-./0-9:;<>?@A-Z\[\]\^_`a-z{\|}]

// when an identifier isn't between two dollars, use a more restrictive set of characters
x30_x7D_OutFirstChar = [_?A-Za-z]
x30_x7D_Out = {x30_x7D_OutFirstChar} | [0-9]
OutIdentifier = {x30_x7D_OutFirstChar} {x30_x7D_Out}*



WhiteSpace     = \s

LineTerminator = \r|\n|\r\n

/* comments */
Comment = {MultiLineComment} | {EndOfLineComment}

MultiLineComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
// Comment can be the last line of the file, without line terminator.
EndOfLineComment     = "//" [^\r\n]* {LineTerminator}?



%%

<YYINITIAL> {
    \"                           { string.setLength(0); yybegin(STRING); }
    "$"                          { string.setLength(0); yybegin(IDENTIFIER); }

    "{"                          { return symbol("{", FParserSym.LBRACE); }
    "}"                          { return symbol("}", FParserSym.RBRACE); }
    "["                          { return symbol("[", FParserSym.LBRACKET); }
    "]"                          { return symbol("]", FParserSym.RBRACKET); }
    "("                          { return symbol("(", FParserSym.LPAREN); }
    ")"                          { return symbol(")", FParserSym.RPAREN); }
    ","                          { return symbol(",", FParserSym.COMMA); }

    "&&"                         { return symbol("and", FParserSym.AND); }
    "||"                         { return symbol("or", FParserSym.OR); }
    "!"                          { return symbol("not", FParserSym.NOT); }

    "if"                         { return symbol("if", FParserSym.IF); }
    "else"                       { return symbol("else", FParserSym.ELSE); }
    "true"                       { return symbol("true", FParserSym.BOOLEAN, true); }
    "false"                      { return symbol("false", FParserSym.BOOLEAN, false); }

    "=="                         { return symbol("==", FParserSym.EQ); }
    "<"                          { return symbol("lt", FParserSym.LT); }
    "<="                         { return symbol("lteq", FParserSym.LTEQ); }
    ">"                          { return symbol("gt", FParserSym.GT); }
    ">="                         { return symbol("gteq", FParserSym.GTEQ); }

    {Number}                     { return symbol("int", FParserSym.INTEGER, Integer.parseInt(yytext())); }
    {OutIdentifier}              { return symbol("identifier", FParserSym.IDENTIFIER, yytext()); }

    {Comment}                    { /* ignore comment*/ }
    {WhiteSpace}                 { /* ignore whitespace */ }
}

<IDENTIFIER> {
    \$                           { yybegin(YYINITIAL); return stringSymbol("identifier", FParserSym.IDENTIFIER); }
    \\\$                         { string.append('$'); }
    \\\\                         { string.append('\\'); }
    {x30_x7D_BetweenDollar}+     { string.append(yytext()); }
    <<EOF>>                      { error.report("Unterminated identifier \"" + yytext() + '"');
                                   yybegin(YYINITIAL); }
    [^]                          { error.report("Illegal character in identifier \"" + yytext() + '"');
                                   yybegin(YYINITIAL); }
}

<STRING> {
    \"                           { yybegin(YYINITIAL); return stringSymbol("str", FParserSym.STRING); }
    [^\n\r\"\\]+                 { string.append( yytext() ); }
    \\t                          { string.append('\t'); }
    \\n                          { string.append('\n'); }
    \\r                          { string.append('\r'); }
    \\\"                         { string.append('\"'); }
    \\                           { string.append('\\'); }
    <<EOF>>                      { error.report("Unterminated string \"" + yytext() + '"');
                                   yybegin(YYINITIAL); }
    [^]                          { error.report("Illegal character in string \"" + yytext() + '"');
                                   yybegin(YYINITIAL); }
}

<<EOF>>                          { return symbol("eof", FParserSym.EOF); }

/* error fallback */
[^]                              { error.report("Illegal token \"" + yytext() + '"'); }