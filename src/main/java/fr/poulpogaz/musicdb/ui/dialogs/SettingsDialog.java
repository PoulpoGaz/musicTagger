package fr.poulpogaz.musicdb.ui.dialogs;

import fr.poulpogaz.musicdb.downloader.DownloadManager;
import fr.poulpogaz.musicdb.ui.layout.VerticalConstraint;
import fr.poulpogaz.musicdb.ui.layout.VerticalLayout;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends AbstractDialog {

    public static void showDialog(JFrame owner) {
        new SettingsDialog(owner).setVisible(true);
    }

    public SettingsDialog(JFrame owner) {
        super(owner, "Settings", true);
        init();
    }

    @Override
    protected void setBestSize() {
        pack();
    }

    @Override
    protected void initComponents() {
        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        content.setLayout(new VerticalLayout());

        VerticalConstraint c = new VerticalConstraint();
        c.fillXAxis = true;
        content.add(createDownloaderSettings(), c);

        // JScrollPane scrollPane = new JScrollPane(content);
        setContentPane(content);
    }

    private JPanel createDownloaderSettings() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Downloader"));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;

        {
            panel.add(new JLabel("Maximum number of simultaneous download"), c);
        }

        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        {
            JSpinner downloaderSpinner = new JSpinner();

            int current = DownloadManager.getMaxThreadCount();
            int max = Math.max(current, Runtime.getRuntime().availableProcessors());

            downloaderSpinner.setModel(new SpinnerNumberModel(current, 1, max, 1));
            panel.add(downloaderSpinner, c);
        }


        return panel;
    }
}
