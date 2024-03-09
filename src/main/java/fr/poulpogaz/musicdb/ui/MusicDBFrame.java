package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.model.TemplatesListener;
import fr.poulpogaz.musicdb.model.Templates;
import fr.poulpogaz.musicdb.ui.dialogs.EditTemplateDialog;
import fr.poulpogaz.musicdb.ui.dialogs.NewTemplateDialog;

import javax.swing.*;
import java.awt.*;
import java.net.http.HttpClient;

public class MusicDBFrame extends JFrame {

    private TemplatesPanel templatesPanel;

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
        templatesPanel = new TemplatesPanel();

        menuBar = createJMenuBar();

        setLayout(new BorderLayout());
        add(templatesPanel, BorderLayout.CENTER);
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

        JMenuBar bar = new JMenuBar();
        bar.add(templates);

        return bar;
    }
}
