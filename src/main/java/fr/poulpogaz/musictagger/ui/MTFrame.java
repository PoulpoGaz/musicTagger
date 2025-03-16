package fr.poulpogaz.musictagger.ui;

import fr.poulpogaz.musictagger.downloader.DownloadListener;
import fr.poulpogaz.musictagger.downloader.DownloadManager;
import fr.poulpogaz.musictagger.model.*;
import fr.poulpogaz.musictagger.ui.dialogs.Dialogs;
import fr.poulpogaz.musictagger.ui.dialogs.ExportDialog;
import fr.poulpogaz.musictagger.ui.layout.*;
import fr.poulpogaz.musictagger.utils.AbstractLazyImage;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

public class MTFrame extends JFrame {

    private static final MTFrame INSTANCE = new MTFrame();

    public static MTFrame getInstance() {
        return INSTANCE;
    }




    private SplitPane splitPane;
    private TemplatesPanel templatesPanel;
    private JPanel downloadQueue;

    private JMenuBar menuBar;
    private JMenuItem newTemplate;
    private JMenuItem editTemplate;
    private JMenuItem deleteTemplate;


    private JPanel bottomBar;
    private JLabel loadingFilesLabel;
    private JLabel loadedMusicsLabel;
    private JLabel newMusicsLabel;
    private JLabel downloadCountLabel;

    private TemplateDataListener templateDataListener;

    private MTFrame() {
        super("musicTagger");

        initComponents();
        setupListeners();

        setJMenuBar(menuBar);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1280, 720);

        setLocationRelativeTo(null);
    }

    private void initComponents() {
        downloadQueue = createQueuePanel();
        templatesPanel = new TemplatesPanel();
        splitPane = new SplitPane(templatesPanel, downloadQueue);
        bottomBar = createBottomBar();

        menuBar = createJMenuBar();

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }


    // ==================
    // * DOWNLOAD QUEUE *
    // ==================

    private JPanel createQueuePanel() {
        DownloadQueuePanel downloadQueuePanel = new DownloadQueuePanel();

        JButton closeButton = new JButton(Icons.get("close.svg"));
        closeButton.setToolTipText("Close download queue");
        closeButton.addActionListener(_ -> setDownloadQueueVisible(!isDownloadQueueVisible()));

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





    private JPanel createBottomBar() {
        loadingFilesLabel = new JLabel("0 files loading");
        loadedMusicsLabel = new JLabel(Templates.totalMusicCount() + " musics");

        newMusicsLabel = new JLabel("New");
        downloadCountLabel = new JLabel(DownloadManager.getTaskCount() + " downloads");

        JPanel bottomBar = new JPanel();
        bottomBar.setLayout(new HorizontalLayout());

        HorizontalConstraint c = new HorizontalConstraint();
        c.orientation = HCOrientation.RIGHT;
        c.leftGap = 10;
        c.rightGap = 5;

        bottomBar.add(downloadCountLabel, c);
        bottomBar.add(newMusicsLabel, c);
        bottomBar.add(loadedMusicsLabel, c);
        bottomBar.add(loadingFilesLabel, c);

        return bottomBar;
    }

    public void setLoadingFileCount(long count) {
        loadingFilesLabel.setText(count + " files loading");
    }



    private JMenuBar createJMenuBar() {
        JMenu file = new JMenu("File");
        JMenuItem load = file.add("Load musics");
        load.addActionListener(this::loadMusics);

        JMenuItem exportJson = file.add("Export to JSON");
        exportJson.addActionListener(_ -> ExportDialog.showDialog(this));
        file.addSeparator();

        JMenuItem quit = file.add("Quit");
        quit.addActionListener(_ -> close());


        JMenu edit = new JMenu("Edit");
        edit.add(Actions.moveMetadata());
        edit.addSeparator();
        newTemplate = edit.add(TemplateActions.createAction());
        editTemplate = edit.add(TemplateActions.editAction());
        deleteTemplate = edit.add(TemplateActions.deleteAction());
        edit.add(TemplateActions.saveAction());
        edit.add(TemplateActions.loadAction());



        JCheckBoxMenuItem downloadQueueItem = new JCheckBoxMenuItem();
        downloadQueueItem.setState(isDownloadQueueVisible());
        downloadQueueItem.setText("Open download queue");
        downloadQueueItem.addActionListener(_ -> setDownloadQueueVisible(!isDownloadQueueVisible()));
        splitPane.addPropertyChangeListener(SplitPane.RIGHT_VISIBILITY, _ -> downloadQueueItem.setState(isDownloadQueueVisible()));

        JMenu view = new JMenu("View");
        view.add(downloadQueueItem);


        JMenuBar bar = new JMenuBar();
        bar.add(file);
        bar.add(edit);
        bar.add(view);

        return bar;
    }

    private void loadMusics(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new FileNameExtensionFilter("Opus (.opus)", "opus"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON (.json)", "json"));
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setCurrentDirectory(Dialogs.WORKING_DIRECTORY);

        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            MusicLoader.load(files);
        }
    }


    private void setupListeners() {
        for (Template template : Templates.getTemplates()) {
            template.getData().addTemplateDataListener(createTemplateDataListener());
        }

        Templates.addTemplatesListener(createTemplatesListener());
        DownloadManager.addListener(createDownloadListener());
        addWindowListener(createWindowListener());

        templatesPanel.addChangeListener(_ -> updateTemplateActions());
        updateTemplateActions();
    }

    private void updateTemplateActions() {
        Template t = templatesPanel.getSelectedTemplate();
        if (t != null) {
            TemplateActions.editAction().setEnabled(true);
            TemplateActions.deleteAction().setEnabled(!t.isInternalTemplate());
        } else {
            TemplateActions.editAction().setEnabled(false);
            TemplateActions.deleteAction().setEnabled(false);
        }
    }

    private TemplateDataListener createTemplateDataListener() {
        if (templateDataListener == null) {
            templateDataListener = (_, _, _, _) -> {
                int count = Templates.totalMusicCount();
                loadedMusicsLabel.setText(count + " musics");
            };
        }

        return templateDataListener;
    }

    private TemplatesListener createTemplatesListener() {
        return (_, _) -> {
            if (Templates.templateCount() == 0) {
                deleteTemplate.setEnabled(false);
                editTemplate.setEnabled(false);
            } else {
                deleteTemplate.setEnabled(true);
                editTemplate.setEnabled(true);
            }
        };
    }

    private DownloadListener createDownloadListener() {
        return (_, _) -> {
            downloadCountLabel.setText(DownloadManager.getTaskCount() + " downloads");
        };
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
            AbstractLazyImage.shutdown();
            MusicLoader.shutdown();
            DownloadManager.shutdown();
            DownloadManager.cancelAll();
            dispose();
        }
    }

    public TemplatesPanel getTemplatesPanel() {
        return templatesPanel;
    }
}
