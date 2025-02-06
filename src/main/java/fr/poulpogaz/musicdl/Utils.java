package fr.poulpogaz.musicdl;

public class Utils {

    public static String escapeCharacter(String str, char quote, char escape) {
        return addQuote(str, quote, escape, false);
    }

    public static String addQuote(String str, char quote) {
        return addQuote(str, quote, quote, true);
    }

    public static String addQuote(String str, char quote, char escape, boolean outerQuote) {
        int quoteCount = count(str, quote);
        int len = str.length();

        char[] chars = new char[len + quoteCount + (outerQuote ? 2 : 0)];
        int j = 0;

        if (outerQuote) {
            chars[j++] = quote;
        }
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);

            if (c == quote) {
                chars[j++] = escape;
            }

            chars[j++] = c;
        }
        if (outerQuote) {
            chars[j] = quote;
        }

        return new String(chars);
    }

    public static int count(String str, char c) {
        int i = 0;
        int n = 0;

        while ((i = str.indexOf(c, i) + 1) > 0) {
            n++;
        }

        return n;
    }
}
