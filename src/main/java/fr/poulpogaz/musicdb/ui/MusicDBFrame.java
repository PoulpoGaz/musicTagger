package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.downloader.DownloadManager;
import fr.poulpogaz.musicdb.model.Template;
import fr.poulpogaz.musicdb.model.Templates;
import fr.poulpogaz.musicdb.model.TemplatesListener;
import fr.poulpogaz.musicdb.ui.dialogs.EditTemplateDialog;
import fr.poulpogaz.musicdb.ui.dialogs.NewTemplateDialog;
import fr.poulpogaz.musicdb.ui.layout.VerticalConstraint;
import fr.poulpogaz.musicdb.ui.layout.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MusicDBFrame extends JFrame {

    private static final MusicDBFrame INSTANCE = new MusicDBFrame();

    public static MusicDBFrame getInstance() {
        return INSTANCE;
    }




    private MusicDBSplitPane splitPane;
    private TemplatesPanel templatesPanel;
    private JPanel downloadQueue;

    private JMenuBar menuBar;
    private JMenuItem newTemplate;
    private JMenuItem editTemplate;
    private JMenuItem deleteTemplate;

    private MusicDBFrame() {
        super("MusicDB");

        Templates.addTemplateListener(createTemplatesListener());
        initComponents();
        setJMenuBar(menuBar);
        addWindowListener(createWindowListener());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1280, 720);

        setLocationRelativeTo(null);
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







    private JMenuBar createJMenuBar() {
        JMenu file = new JMenu("File");

        JMenuItem quit = file.add("Quit");
        quit.addActionListener((e) -> close());


        JMenu templates = new JMenu("Templates");
        newTemplate = templates.add(TemplateHelper.createCreateTemplateAction());
        editTemplate = templates.add(TemplateHelper.createEditTemplateAction(templatesPanel::getSelectedTemplate));
        deleteTemplate = templates.add(TemplateHelper.createDeleteTemplateAction(templatesPanel::getSelectedTemplate));

        JCheckBoxMenuItem downloadQueueItem = new JCheckBoxMenuItem();
        downloadQueueItem.setState(isDownloadQueueVisible());
        downloadQueueItem.setText("Open download queue");
        downloadQueueItem.addActionListener(e -> setDownloadQueueVisible(!isDownloadQueueVisible()));
        splitPane.addPropertyChangeListener(MusicDBSplitPane.RIGHT_VISIBILITY, e -> downloadQueueItem.setState(isDownloadQueueVisible()));

        JMenu view = new JMenu("View");
        view.add(downloadQueueItem);


        JMenuBar bar = new JMenuBar();
        bar.add(file);
        bar.add(templates);
        bar.add(view);

        return bar;
    }





    private WindowListener createWindowListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        };
    }

    public void close() {
        boolean close = true;
        if (DownloadManager.isDownloading()) {
            int ret = JOptionPane.showConfirmDialog(this, "Musics are being downloaded. Closing the window will cancel these download. Quit anyway ?", "Cancel downloads ?", JOptionPane.YES_NO_OPTION);
            close = ret == JOptionPane.YES_OPTION;
        }

        if (close) {
            DownloadManager.shutdown();
            DownloadManager.cancelAll();
            dispose();
        }
    }
}
