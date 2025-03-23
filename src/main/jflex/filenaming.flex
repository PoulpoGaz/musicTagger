package fr.poulpogaz.musictagger.filenaming;

import java_cup.runtime.Symbol;

%%

%class FLexer
%state STRING, IDENTIFIER
%unicode
%cup
%line
%column

%{
  StringBuffer string = new StringBuffer();

  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
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
    {Number}                     { return symbol(FParserSym.INTEGER, Integer.parseInt(yytext())); }
    {OutIdentifier}              { return symbol(FParserSym.IDENTIFIER, yytext()); }
    "$"                          { string.setLength(0); yybegin(IDENTIFIER); }

    "("                          { return symbol(FParserSym.LPAREN); }
    ")"                          { return symbol(FParserSym.RPAREN); }
    ","                          { return symbol(FParserSym.COMMA); }

    {Comment}                    { /* ignore comment*/ }
    {WhiteSpace}                 { /* ignore whitespace */ }
}

<IDENTIFIER> {
    \$                           { yybegin(YYINITIAL); return symbol(FParserSym.IDENTIFIER, string.toString()); }
    \\\$                         { string.append('$'); }
    \\\\                         { string.append('\\'); }
    {x30_x7D_BetweenDollar}+     { string.append(yytext()); }
    <<EOF>>                      { throw new RuntimeException("Unterminated identifier \""+yytext()+
                                                              "\" at line "+yyline+", column "+yycolumn); }
}

<STRING> {
    \"                           { yybegin(YYINITIAL);
                                   return symbol(FParserSym.STRING,
                                   string.toString()); }
    [^\n\r\"\\]+                 { string.append( yytext() ); }
    \\t                          { string.append('\t'); }
    \\n                          { string.append('\n'); }
    \\r                          { string.append('\r'); }
    \\\"                         { string.append('\"'); }
    \\                           { string.append('\\'); }
    <<EOF>>                      { throw new RuntimeException("Unterminated string \""+yytext()+
                                                              "\" at line "+yyline+", column "+yycolumn); }
}

<<EOF>>                          { return symbol(FParserSym.EOF); }

/* error fallback */
[^]                              { throw new RuntimeException("Illegal character \""+yytext()+
                                                              "\" at line "+yyline+", column "+yycolumn); }