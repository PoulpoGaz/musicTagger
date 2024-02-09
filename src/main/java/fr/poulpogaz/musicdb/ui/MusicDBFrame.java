package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.model.Templates;
import fr.poulpogaz.musicdb.ui.dialogs.NewTemplateDialog;

import javax.swing.*;
import java.awt.*;

public class MusicDBFrame extends JFrame {

    private TemplatesPanel templatesPanel;

    public MusicDBFrame() {
        super("MusicDB");

        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1280, 720);

        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JToolBar toolBar = createToolBar();
        templatesPanel = new TemplatesPanel();



        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(templatesPanel, BorderLayout.CENTER);
    }

    private JToolBar createToolBar() {
        JButton newTemplate = new JButton(Icons.get("add.svg"));
        newTemplate.setToolTipText("New template");
        newTemplate.addActionListener(e -> NewTemplateDialog.showDialog(this));

        JButton editTemplate = new JButton(Icons.get("edit.svg"));
        newTemplate.setToolTipText("Edit template");

        JButton deleteTemplate = new JButton(Icons.get("delete.svg"));
        newTemplate.setToolTipText("Delete template");

        JToolBar bar = new JToolBar();
        bar.add(newTemplate);
        bar.add(editTemplate);
        bar.add(deleteTemplate);

        return bar;
    }
}
