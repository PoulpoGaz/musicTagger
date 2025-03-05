package fr.poulpogaz.musicdl.ui;

import fr.poulpogaz.musicdl.opus.OpusFile;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class MetadataFieldDocumentFilter extends DocumentFilter {

    public static void setup(JTextField field) {
        ((PlainDocument) field.getDocument()).setDocumentFilter(INSTANCE);
    }

    public static JTextField createTextField() {
        JTextField field = new JTextField();
        MetadataFieldDocumentFilter.setup(field);

        return field;
    }


    private static final MetadataFieldDocumentFilter INSTANCE = new MetadataFieldDocumentFilter();

    @Override
    public void insertString(FilterBypass fb, int offset, String string,
                             AttributeSet attr) throws BadLocationException {
        super.insertString(fb, offset, OpusFile.reduce(string), attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {
        super.replace(fb, offset, length, OpusFile.reduce(text), attrs);
    }
}
