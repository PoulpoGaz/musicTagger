package fr.poulpogaz.musictagger.model;

import javax.swing.event.TableModelEvent;

public interface TemplateDataListener {

    int INSERT = TableModelEvent.INSERT;
    int UPDATE = TableModelEvent.UPDATE;
    int DELETE = TableModelEvent.DELETE;

    void dataChanged(TemplateData templateData, int event, int firstRow, int lastRow);
}
