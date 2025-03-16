package fr.poulpogaz.musictagger.ui.dialogs;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;

public class Dialogs {

    public static final File WORKING_DIRECTORY = new File(System.getProperty("user.dir"));

    public static Path showFileChooser(Component parent) {
        return showFileChooser(parent, JFileChooser.FILES_ONLY, null, false);
    }

    public static Path showFileChooser(Component parent, int mode) {
        return showFileChooser(parent, mode, null, false);
    }

    public static Path showFileChooser(Component parent, int mode, FileFilter fileFilter) {
        return showFileChooser(parent, mode, fileFilter, false);
    }

    public static Path showFileChooser(Component parent, int mode, FileFilter fileFilter, boolean allFilter) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(mode);
        chooser.setFileFilter(fileFilter);
        chooser.setAcceptAllFileFilterUsed(allFilter);
        chooser.setCurrentDirectory(WORKING_DIRECTORY);

        int result = chooser.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            return file.toPath();
        }

        return null;
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showError(Component parent, String message, Throwable throwable) {
        JOptionPane.showMessageDialog(parent, message + "\n\n" + throwable, "Error", JOptionPane.ERROR_MESSAGE);
    }
}