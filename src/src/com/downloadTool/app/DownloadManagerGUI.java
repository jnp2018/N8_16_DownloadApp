package com.downloadTool.app;

import com.sun.deploy.panel.JreTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

public class DownloadManagerGUI extends JFrame implements Observer {
    private JPanel rootPanel;
    private JTextField txtURL;
    private JButton btnAddDownload;
    private JTable tblDetail;
    private JTextField txtOutputFolder;
    private JButton btnSaveFile;
    private JButton btnPause;
    private JButton btnResume;
    private JButton btnCancel;
    private JButton btnRemove;
    private JButton btnExit;
    private JScrollPane scrollPane;

    private DownloadTableModel tableModel;

    private Downloader selectedDownloader;

    private boolean isClearing;

    private String fileName;

    public DownloadManagerGUI() {
        tableModel = new DownloadTableModel();
        initCompoment();
        initialize();
    }

    private void initialize() {
        scrollPane = new JScrollPane();
        tblDetail.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                tableSelectionChanged();
            }
        });

        tblDetail.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ProgressRenderer renderer = new ProgressRenderer(0, 100);
        renderer.setStringPainted(true);
        tblDetail.setDefaultRenderer(JProgressBar.class, renderer);

        tblDetail.setRowHeight((int) renderer.getPreferredSize().getHeight());
    }
    
    public void initCompoment() {

//        set detail for panel
        setContentPane(rootPanel);
        setTitle("Download Manager");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        tblDetail.setModel(tableModel);
//        scrollPane.setViewportView(tblDetail);

//  add ActionListener for button
        btnAddDownload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                btnAddDownloadActionPerformed(actionEvent);
            }
        });

        tblDetail.setModel(tableModel);
        

        btnPause.setEnabled(false);
        btnPause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                btnPauseActionPerformed(actionEvent);
            }
        });

        btnRemove.setEnabled(false);
        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                btnRemoveActionPerformed(actionEvent);
            }
        });

        btnCancel.setEnabled(false);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                btnCancelActionPerformed(actionEvent);
            }
        });

        btnExit.setEnabled(false);
        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                btnExitActionPerformed(actionEvent);
            }
        });

        btnResume.setEnabled(false);
        btnResume.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                btnResumeActionPerformed(actionEvent);
            }
        });

        pack();


    }

    private void btnPauseActionPerformed(ActionEvent actionEvent) {
        selectedDownloader.pause();
        updateButton();
    }

    private void btnResumeActionPerformed(ActionEvent actionEvent) {
        selectedDownloader.resume();
        updateButton();
    }

    private void btnCancelActionPerformed(ActionEvent actionEvent) {
        selectedDownloader.cancel();
        updateButton();
    }

    private void btnRemoveActionPerformed(ActionEvent actionEvent) {
        isClearing = true;

        int index = tblDetail.getSelectedRow();
        DownloadManager.getInstance().removeDownload(index);

        tableModel.clearDownload(index);
        isClearing = false;
        selectedDownloader = null;
        updateButton();
    }

    private void btnExitActionPerformed(ActionEvent actionEvent) {
        setVisible(false);
    }

    private void btnAddDownloadActionPerformed(ActionEvent actionEvent) {
        URL verifiedURL = DownloadManager.verifyURL(txtURL.getText());

        if (verifiedURL != null) {
            Downloader download = DownloadManager.getInstance().createDownload(verifiedURL, "");
            tableModel.addNewDownload(download);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Download URL", "Error!", JOptionPane.ERROR_MESSAGE );
        }
    }

    private void tableSelectionChanged() {
        if (selectedDownloader != null) {
            selectedDownloader.deleteObserver(DownloadManagerGUI.this);
        }

        if (!isClearing) {
            int index = tblDetail.getSelectedRow();
            if (index != -1) {
                selectedDownloader = DownloadManager.getInstance().getDownload(tblDetail.getSelectedRow());
                selectedDownloader.addObserver(DownloadManagerGUI.this);
            } else {
                selectedDownloader = null;
                updateButton();
            }
        }
    }

    private void updateButton() {
        if (selectedDownloader != null) {
            int state = selectedDownloader.getState();

            switch (state) {
                case Downloader.DOWNLOADING: {
                    btnPause.setEnabled(true);
                    btnResume.setEnabled(false);
                    btnCancel.setEnabled(true);
                    btnRemove.setEnabled(false);
                    break;
                }
                case Downloader.PAUSED: {
                    btnPause.setEnabled(false);
                    btnResume.setEnabled(true);
                    btnCancel.setEnabled(true);
                    btnRemove.setEnabled(false);
                    break;
                }
                case Downloader.ERROR: {
                    btnPause.setEnabled(false);
                    btnResume.setEnabled(true);
                    btnCancel.setEnabled(false);
                    btnRemove.setEnabled(true);
                    break;
                }
                default: {
                    btnPause.setEnabled(false);
                    btnResume.setEnabled(false);
                    btnCancel.setEnabled(false);
                    btnRemove.setEnabled(true);
                    break;
                }
            }
        } else {
            btnPause.setEnabled(false);
            btnResume.setEnabled(false);
            btnCancel.setEnabled(false);
            btnRemove.setEnabled(false);
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        if (selectedDownloader != null && selectedDownloader.equals(observable)) {
            updateButton();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DownloadManagerGUI().setVisible(true);
            }
        });
    }

}
