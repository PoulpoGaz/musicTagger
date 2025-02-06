package fr.poulpogaz.musicdb.ui;

import fr.poulpogaz.musicdb.downloader.*;
import fr.poulpogaz.musicdb.ui.layout.VerticalConstraint;
import fr.poulpogaz.musicdb.ui.layout.VerticalLayout;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

public class DownloadQueuePanel extends JPanel {

    private static final Insets CANCEL_SELECTED_INSETS = new Insets(2, 2, 2, 2);


    private final DownloadListener downloadListener;


    // RUNNING TASK
    private RunningTaskPanel runningTaskPanel;
    private JLabel noDownloadLabel;
    private final Timer timer;


    // QUEUE
    private JPanel queueContainer;
    private JLabel emptyQueueLabel;
    private JScrollPane tableScrollPane;
    private JTable table;
    private TableModel tableModel;
    private JButton cancelSelected;
    private boolean empty = true;

    public DownloadQueuePanel() {
        initComponents();

        addHierarchyListener(createHierarchyListener());
        downloadListener = createDownloadListener();
        timer = new Timer(200, (e) -> runningTaskPanel.updateProgressComponent());
    }


    private void initComponents() {
        runningTaskPanel = new RunningTaskPanel();
        noDownloadLabel = new JLabel("No download in progress", SwingConstants.CENTER);

        createQueueContainer();

        setLayout(new BorderLayout());
        add(queueContainer, BorderLayout.CENTER);
        add(noDownloadLabel, BorderLayout.NORTH);
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
        updateQueueContainerLayout(true, true);
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



    private HierarchyListener createHierarchyListener() {
        return e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                if (isDisplayable()) {
                    setupComponent();
                } else {
                    unSetupComponent();
                }
            }
        };
    }

    private void setupComponent() {
        setRunningTask(DownloadManager.getRunningTask());
        tableModel.setTasks(DownloadManager.getQueue());
        updateQueueContainerLayout(tableModel.contents.isEmpty(), true);
        DownloadManager.addListener(EventThread.SWING_THREAD, downloadListener);
    }

    private void unSetupComponent() {
        setRunningTask(null);
        tableModel.removeAll();
        updateQueueContainerLayout(true, true);
        DownloadManager.removeListener(EventThread.SWING_THREAD, downloadListener);
    }

    private void setRunningTask(DownloadTask task) {
        if (task == null) {
            if (noDownloadLabel.getParent() == null) {
                remove(runningTaskPanel);
                add(noDownloadLabel, BorderLayout.NORTH);
                stopTimer();
                revalidate();
                repaint();
            }
        } else {
            runningTaskPanel.set(task);
            startTimer();

            if (runningTaskPanel.getParent() == null) {
                remove(noDownloadLabel);
                add(runningTaskPanel, BorderLayout.NORTH);
                revalidate();
                repaint();
            }
        }
    }

    private void startTimer() {
        if (runningTaskPanel.task != null) {
            timer.start();
        }
    }

    private void stopTimer() {
        timer.stop();
    }





    private DownloadListener createDownloadListener() {
        return (event, task) -> {
            switch (event) {
                case DownloadListener.Event.QUEUED -> {
                    tableModel.addTask(task);
                }
                case DownloadListener.Event.STARTED -> {
                    tableModel.removeTask(task);
                    setRunningTask(task);
                }
                case DownloadListener.Event.CANCELED -> {
                    if (task == runningTaskPanel.task) {
                        setRunningTask(tableModel.contents.peekFirst());
                    } else if (task != null) {
                        tableModel.removeTask(task);
                    } else {
                        tableModel.removeCancelledTasks();
                    }
                }
                case DownloadListener.Event.FINISHED -> {
                    // set running task to the next one in the queue
                    // it avoids stopping the timer
                    setRunningTask(tableModel.contents.peekFirst());
                }
                case DownloadListener.Event.FAILED -> {
                    tableModel.removeTask(task);
                    setRunningTask(tableModel.contents.peekFirst());
                }
            }

            updateQueueContainerLayout(DownloadManager.isQueueEmpty(), false);
        };
    }

    private void updateQueueContainerLayout(boolean empty, boolean force) {
        if (!force && empty == this.empty) {
            return;
        }

        this.empty = empty;

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




    private static class RunningTaskPanel extends JPanel {

        private DownloadTask task;
        private JLabel title;
        private Component progressComponent;

        private JButton cancel;

        public RunningTaskPanel() {
            initComponents();
            cancel.addActionListener(this::cancel);
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

        public void setTasks(Collection<DownloadTask> tasks) {
            contents.clear();
            contents.addAll(tasks);
            fireTableDataChanged();
        }

        public void removeAll() {
            contents.clear();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return contents.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Status";
                case 1 -> "Position";
                case 2 -> "ID";
                case 3 -> "Description";
                default -> throw new IllegalStateException("Unexpected value: " + column);
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DownloadTask task = contents.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> task.getStateImmediately().toString();
                case 1 -> rowIndex + 1;
                case 2 -> task.getID();
                case 3 -> task.getDescription();
                default -> throw new IllegalStateException("Unexpected value: " + columnIndex);
            };
        }
    }
}
