package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.model.TemplatesListener;
import fr.poulpogaz.musicdb.model.Templates;
import fr.poulpogaz.musicdb.ui.dialogs.EditTemplateDialog;
import fr.poulpogaz.musicdb.ui.dialogs.NewTemplateDialog;

import javax.swing.*;
import java.awt.*;

public class MusicDBFrame extends JFrame {

    private TemplatesPanel templatesPanel;

    private JToolBar toolBar;
    private JButton newTemplate;
    private JButton editTemplate;
    private JButton deleteTemplate;

    public MusicDBFrame() {
        super("MusicDB");

        Templates.addTemplateListener(createTemplatesListener());
        initComponents();
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
        toolBar = createToolBar();
        templatesPanel = new TemplatesPanel();



        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(templatesPanel, BorderLayout.CENTER);
    }

    private JToolBar createToolBar() {
        newTemplate = new JButton(Icons.get("add.svg"));
        newTemplate.setToolTipText("New template");
        newTemplate.addActionListener(e -> NewTemplateDialog.showDialog(this));

        editTemplate = new JButton(Icons.get("edit.svg"));
        editTemplate.setToolTipText("Edit template");
        editTemplate.addActionListener(e -> EditTemplateDialog.showDialog(this, templatesPanel.getSelectedTemplate()));

        deleteTemplate = new JButton(Icons.get("delete.svg"));
        deleteTemplate.setToolTipText("Delete template");
        deleteTemplate.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "This operation will remove all data of template " +
                                                                templatesPanel.getSelectedTemplate().getName());
            if (r == JOptionPane.YES_OPTION) {
                Templates.removeTemplate(templatesPanel.getSelectedTemplate());
            }
        });

        JToolBar bar = new JToolBar();
        bar.add(newTemplate);
        bar.add(editTemplate);
        bar.add(deleteTemplate);

        return bar;
    }
}
