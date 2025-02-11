package fr.poulpogaz.musicdl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Formatter {

    private final StringBuilder sb = new StringBuilder();
    private final List<FormatString> formatString = new ArrayList<>();
    private boolean compiled = false;
    private String format;

    public Formatter() {

    }

    public Formatter(String format) {
        setFormat(format);
    }

    public void compile() {
        if (compiled) {
            return;
        }

        formatString.clear();

        int i = 0;
        while (i < format.length()) {
            int j = readUntil(i, '{');

            if (i < j) {
                formatString.add(new FixedString(sb.toString()));
            }
            if (j < format.length()) {
                j = readFormat(j);
            }

            i = j;
        }

        compiled = true;
    }

    private int readFormat(int index) {
        index++; // skip '{'
        int colon = format.indexOf(':', index);
        if (colon < 0) {
            throw new IllegalFormatException("No colon found after { at index " + index);
        }

        String formatFunction = format.substring(index, colon);
        if (!formatFunction.equals("key")) {
            throw new IllegalFormatException("Unknown format function: " + formatFunction);
        }

        int j = readUntil(colon + 1, '}');
        if (j == format.length()) {
            throw new IllegalFormatException("No curly bracket found after colon at index " + colon);
        }
        if (colon + 1 == j) {
            throw new IllegalFormatException("Empty key at index " + colon);
        }

        formatString.add(new KeyString(sb.toString()));

        return j + 1;
    }

    private int readUntil(int index, char stopChar) {
        sb.setLength(0);

        boolean escaped = false;
        while (index < format.length()) {
            char c = format.charAt(index);

            if (!escaped && c == '\\') {
                escaped = true;
                index++;
            } else if (!escaped && c == stopChar) {
                break;
            } else {
                sb.append(c);

                escaped = false;
                index++;
            }
        }

        return index;
    }

    public String format(Map<String, String> music) {
        if (!compiled) {
            try {
                compile();
            } catch (IllegalFormatException e) {
                return null;
            }
        }
        sb.setLength(0);

        for (FormatString str : formatString) {
            str.print(this, music);
        }

        return sb.toString();
    }

    public void setFormat(String format) {
        if (!Objects.equals(format, this.format)) {
            formatString.clear();
            this.format = format;
            compiled = false;
        }
    }

    public String getFormat() {
        return format;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Formatter formatter)) return false;

        return Objects.equals(format, formatter.format);
    }

    @Override
    public int hashCode() {
        return format != null ? format.hashCode() : 0;
    }

    private interface FormatString {

        void print(Formatter formatter, Map<String, String> music);
    }

    private record FixedString(String string) implements FormatString {

        @Override
        public void print(Formatter formatter, Map<String, String> music) {
            formatter.sb.append(string);
        }
    }

    private record KeyString(String key) implements FormatString {

        @Override
        public void print(Formatter formatter, Map<String, String> music) {
            String tag = music.get(key);

            if (tag == null) {
                formatter.sb.append("null");
            } else {
                formatter.sb.append(tag.replace('/', '⧸'));
            }
        }
    }
}
