package com.downloadTool.app;

import javax.swing.table.AbstractTableModel;
import java.util.Observable;
import java.util.Observer;

public class DownloadTableModel extends AbstractTableModel implements Observer {

    public static final String[] columnNames = {"URL", "Size (KB)", "Progress", "Status"};

    public static final Class[] columnClasses = {String.class, String.class, String.class, String.class};

    @Override
    public void update(Observable observable, Object o) {
        int index = DownloadManager.getInstance().getDownloadList().indexOf(observable);

//        fire table row update notification to table
        fireTableRowsUpdated(index, index);
    }

    @Override
    public int getRowCount() {
        return DownloadManager.getInstance().getDownloadList().size();
    }

    @Override
    public int getColumnCount() {
        return columnClasses.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Downloader download = DownloadManager.getInstance().getDownloadList().get(row);

        switch (col) {
            case 0: return download.getURL();
            case 1: {
                int size = download.getFileSize();
                return (size == -1) ? "" : (Integer.toString(size / 1024));
            }
            case 2: {
                return new Float(download.getProgress());
            }
            case 3: {
                return Downloader.STATUS[download.getState()];
            }
        }
        return "";
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Class getColumnClass(int col) {
        return columnClasses[col];
    }

    public void clearDownload(int row) {
        fireTableRowsDeleted(row, row);
    }

    public void addNewDownload(Downloader downloader) {
        downloader.addObserver(this);

        fireTableRowsInserted(getRowCount() -1, getRowCount() -1);
    }
}
