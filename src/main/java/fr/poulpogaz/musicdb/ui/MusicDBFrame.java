package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.downloader.DownloadManager;
import fr.poulpogaz.musicdb.model.TemplatesListener;
import fr.poulpogaz.musicdb.model.Templates;
import fr.poulpogaz.musicdb.ui.dialogs.EditTemplateDialog;
import fr.poulpogaz.musicdb.ui.dialogs.NewTemplateDialog;
import fr.poulpogaz.musicdb.ui.layout.VerticalConstraint;
import fr.poulpogaz.musicdb.ui.layout.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.http.HttpClient;

public class MusicDBFrame extends JFrame {

    private MusicDBSplitPane splitPane;
    private TemplatesPanel templatesPanel;
    private JPanel downloadQueue;

    private JMenuBar menuBar;
    private JMenuItem newTemplate;
    private JMenuItem editTemplate;
    private JMenuItem deleteTemplate;

    public MusicDBFrame() {
        super("MusicDB");

        Templates.addTemplateListener(createTemplatesListener());
        initComponents();
        setJMenuBar(menuBar);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1280, 720);

        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DownloadManager.shutdown();
            }
        });
    }

    private TemplatesListener createTemplatesListener() {
        return (event, template) -> {
            if (Templates.templateCount() == 0) {
                deleteTemplate.setEnabled(false);
                editTemplate.setEnabled(false);
            } else {
                deleteTemplate.setEnabled(true);
                editTemplate.setEnabled(true);
            }
        };
    }

    private void initComponents() {
        downloadQueue = createQueuePanel();
        templatesPanel = new TemplatesPanel();
        splitPane = new MusicDBSplitPane(templatesPanel, downloadQueue);

        menuBar = createJMenuBar();

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }

    private JMenuBar createJMenuBar() {
        JMenu templates = new JMenu("Templates");
        newTemplate = templates.add("New template");
        newTemplate.setIcon(Icons.get("add.svg"));
        newTemplate.addActionListener(e -> NewTemplateDialog.showDialog(this));

        editTemplate = templates.add("Edit template");
        editTemplate.setIcon(Icons.get("edit.svg"));
        editTemplate.addActionListener(e -> EditTemplateDialog.showDialog(this, templatesPanel.getSelectedTemplate()));

        deleteTemplate = templates.add("Delete template");
        deleteTemplate.setIcon(Icons.get("delete.svg"));
        deleteTemplate.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "This operation will remove all data of template " +
                                                                templatesPanel.getSelectedTemplate().getName());
            if (r == JOptionPane.YES_OPTION) {
                Templates.removeTemplate(templatesPanel.getSelectedTemplate());
            }
        });


        JCheckBoxMenuItem downloadQueueItem = new JCheckBoxMenuItem();
        downloadQueueItem.setState(isDownloadQueueVisible());
        downloadQueueItem.setText("Open download queue");
        downloadQueueItem.addActionListener(e -> setDownloadQueueVisible(!isDownloadQueueVisible()));
        splitPane.addPropertyChangeListener(MusicDBSplitPane.RIGHT_VISIBILITY, e -> downloadQueueItem.setState(isDownloadQueueVisible()));

        JMenu view = new JMenu("View");
        view.add(downloadQueueItem);


        JMenuBar bar = new JMenuBar();
        bar.add(templates);
        bar.add(view);

        return bar;
    }


    // ==================
    // * DOWNLOAD QUEUE *
    // ==================

    private JPanel createQueuePanel() {
        DownloadQueuePanel downloadQueuePanel = new DownloadQueuePanel();

        JButton closeButton = new JButton(Icons.get("close.svg"));
        closeButton.setToolTipText("Close download queue");
        closeButton.addActionListener(e -> setDownloadQueueVisible(!isDownloadQueueVisible()));

        JToolBar bar = new JToolBar();
        bar.add(Box.createHorizontalGlue());
        bar.add(closeButton);

        JPanel right = new JPanel() {
            @Override
            public Dimension getMinimumSize() {
                return bar.getMinimumSize();
            }
        };
        right.setLayout(new VerticalLayout());
        VerticalConstraint c = new VerticalConstraint();
        c.fillXAxis = true;

        right.add(bar, c);
        c.endComponent = true;
        right.add(downloadQueuePanel, c);

        return right;
    }

    public void setDownloadQueueVisible(boolean visible) {
        splitPane.setRightVisible(visible);
    }

    public boolean isDownloadQueueVisible() {
        return splitPane.isRightVisible();
    }
}
