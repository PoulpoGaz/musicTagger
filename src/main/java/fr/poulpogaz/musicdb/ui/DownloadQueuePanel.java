package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.downloader.*;
import fr.poulpogaz.musicdb.ui.layout.VerticalConstraint;
import fr.poulpogaz.musicdb.ui.layout.VerticalLayout;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DownloadQueuePanel extends JPanel {

    private static final Insets CANCEL_SELECTED_INSETS = new Insets(2, 2, 2, 2);


    private JPanel runningTaskContainer;
    private JLabel noDownloadLabel;
    private final Timer timer;
    private final List<RunningTaskPanel> runningTaskPanels = new ArrayList<>();
    private int usedPanels = 0;


    private JPanel queueContainer;
    private JLabel emptyQueueLabel;
    private JScrollPane tableScrollPane;
    private JTable table;
    private TableModel tableModel;
    private JButton cancelSelected;

    public DownloadQueuePanel() {
        initComponents();
        DownloadManager.addListener(EventThread.SWING_THREAD, createDownloadListener());
        timer = new Timer(200, (e) -> updateProgressComponents());
    }


    private void initComponents() {
        runningTaskContainer = new JPanel();
        runningTaskContainer.setLayout(new VerticalLayout());
        noDownloadLabel = new JLabel("No download in progress", SwingConstants.CENTER);
        runningTaskContainer.add(noDownloadLabel);

        createQueueContainer();

        setLayout(new BorderLayout());
        add(queueContainer, BorderLayout.CENTER);
        add(runningTaskContainer, BorderLayout.NORTH);
    }


    private void createQueueContainer() {
        emptyQueueLabel = new JLabel("Download queue is empty", SwingConstants.CENTER);

        tableModel = new TableModel();
        table = new JTable();
        table.setModel(tableModel);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumn status = table.getColumnModel().getColumn(0);
        status.setMaxWidth(status.getPreferredWidth());

        TableColumn position = table.getColumnModel().getColumn(1);
        position.setMaxWidth(position.getPreferredWidth());

        tableScrollPane = new JScrollPane(table);

        cancelSelected = new JButton("Cancel selected");
        cancelSelected.addActionListener(this::cancelSelected);

        queueContainer = new JPanel();
        queueContainer.setLayout(new GridBagLayout());
        queueContainer.setBorder(BorderFactory.createTitledBorder("Download queue"));
        updateLayout(true);
    }


    private void cancelSelected(ActionEvent actionEvent) {
        if (table.getSelectedColumnCount() == 0) {
            return;
        }

        ListSelectionModel m = table.getSelectionModel();

        int min = m.getMinSelectionIndex();
        int max = m.getMaxSelectionIndex();

        for (int i = min; i <= max; i++) {
            if (m.isSelectedIndex(i)) {
                tableModel.contents.get(i).cancel();
            }
        }

        m.clearSelection();
    }



    private DownloadListener createDownloadListener() {
        return (event, task) -> {
            switch (event) {
                case DownloadListener.Event.QUEUED -> {
                    tableModel.addTask(task);
                }
                case DownloadListener.Event.STARTED -> {
                    tableModel.removeTask(task);
                    addRunningTask(task);
                }
                case DownloadListener.Event.FINISHED -> {
                    removeRunningTask(task);
                }
                case DownloadListener.Event.CANCELED -> {
                    if (task != null) {
                        tableModel.removeTask(task);
                    } else {
                        tableModel.removeCancelledTasks();
                    }
                }
                case DownloadListener.Event.FAILED -> {
                    tableModel.removeTask(task);
                    removeRunningTask(task);
                }
            }

            if (DownloadManager.isQueueEmpty()) {
                if (!emptyQueueLabel.isShowing()) {
                    updateLayout(true);
                }
            } else if (!tableScrollPane.isShowing()) {
                updateLayout(false);
            }
        };
    }

    private void addRunningTask(DownloadTask task) {
        RunningTaskPanel panel;
        if (usedPanels < runningTaskPanels.size()) {
            panel = runningTaskPanels.get(usedPanels);
            panel.set(task);
        } else {
            panel = new RunningTaskPanel(task);
            runningTaskPanels.add(panel);
        }

        usedPanels++;

        if (usedPanels == 1) {
            timer.start();
            runningTaskContainer.remove(noDownloadLabel);
        }

        VerticalConstraint c = new VerticalConstraint();
        c.fillXAxis = true;
        c.topGap = 2;
        c.bottomGap = 2;

        runningTaskContainer.add(panel, c);
        runningTaskContainer.revalidate();
        runningTaskContainer.repaint();
    }

    private void removeRunningTask(DownloadTask task) {
        for (int i = 0; i < runningTaskPanels.size(); i++) {
            if (task == runningTaskPanels.get(i).task) {
                RunningTaskPanel panel = runningTaskPanels.remove(i);
                runningTaskPanels.add(panel);
                runningTaskContainer.remove(panel);
                usedPanels--;
                break;
            }
        }

        if (usedPanels == 0) {
            runningTaskContainer.add(emptyQueueLabel);
            timer.stop();
        }

        runningTaskContainer.revalidate();
        runningTaskContainer.repaint();
    }

    private void updateLayout(boolean empty) {
        queueContainer.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        if (empty) {
            c.weightx = 1;
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;

            queueContainer.add(emptyQueueLabel, c);
        } else {
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;

            queueContainer.add(tableScrollPane, c);

            c.weightx = 0;
            c.weighty = 0;
            c.gridy = 1;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.SOUTHEAST;
            c.insets = CANCEL_SELECTED_INSETS;
            queueContainer.add(cancelSelected, c);
        }

        queueContainer.revalidate();
        queueContainer.repaint();
    }



    private void updateProgressComponents() {
        for (int i = 0; i < usedPanels; i++) {
            runningTaskPanels.get(i).updateProgressComponent();
        }
    }


    private static class RunningTaskPanel extends JPanel {

        private DownloadTask task;
        private JLabel title;
        private Component progressComponent;

        private JButton cancel;

        public RunningTaskPanel(DownloadTask task) {
            initComponents();
            cancel.addActionListener(this::cancel);
            set(task);
        }

        private void initComponents() {
            setLayout(new VerticalLayout());

            title = new JLabel();
            title.setHorizontalAlignment(SwingConstants.CENTER);
            cancel = new JButton("Cancel");

            VerticalConstraint c = new VerticalConstraint();
            c.fillXAxis = true;
            c.topGap = 2;
            c.bottomGap = 2;
            add(title, c);
            c.fillXAxis = false;
            c.xAlignment = 1;
            add(cancel, c);
        }



        private void set(DownloadTask task) {
            this.task = Objects.requireNonNull(task);

            if (progressComponent != null) {
                remove(progressComponent);
            }
            title.setText("Downloading: " + task.getDescription());
            createProgressComponent();
        }


        private void createProgressComponent() {
            Progress progress = task.getProgress();

            if (progress != null) {
                progressComponent = progress.createProgressComponent();

                VerticalConstraint c = new VerticalConstraint();
                c.fillXAxis = true;
                c.topGap = 2;
                c.bottomGap = 2;
                add(progressComponent, c, 1);
                revalidate();
                repaint();
            }
        }

        private void updateProgressComponent() {
            Progress progress = task.getProgress();
            if (progress != null) {
                progress.updateProgressComponent(progressComponent);
            }
        }

        private void cancel(ActionEvent e) {
            task.cancel();
        }
    }



    private static class TableModel extends AbstractTableModel {

        private final LinkedList<DownloadTask> contents = new LinkedList<>();

        public TableModel() {

        }

        public void addTask(DownloadTask task) {
            int i = contents.size();
            contents.add(task);
            fireTableRowsInserted(i, i);
        }

        public void removeTask(DownloadTask task) {
            if (contents.remove(task)) {
                fireTableDataChanged();
            }
        }

        public void removeCancelledTasks() {
            boolean removed = contents.removeIf(DownloadTask::isCanceled);
            if (removed) {
                fireTableDataChanged();
            }
        }

        @Override
        public int getRowCount() {
            return contents.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Status";
                case 1 -> "Position";
                case 2 -> "Description";
                default -> throw new IllegalStateException("Unexpected value: " + column);
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DownloadTask task = contents.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> {
                    if (task.isCanceled()) {
                        yield "Canceled";
                    } else {
                        yield "Pending";
                    }
                }
                case 1 -> rowIndex + 1;
                case 2 -> task.getDescription();
                default -> throw new IllegalStateException("Unexpected value: " + columnIndex);
            };
        }
    }
}
