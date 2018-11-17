package com.downloadTool.app;

import java.net.URL;
import java.util.ArrayList;

public class DownloadManager {

//    the unique instance of this class
    private static DownloadManager instane = null;

//    constant variables
    private static final int DEFAULT_NUM_CONNECT_PER_DOWNLOAD = 8;
    public static final String DEFAULT_OUTPUT_FOLDER = "";

//    member variables
    private int numConnPerDownload;
    private ArrayList<Downloader> downloadList;

    protected DownloadManager() {
        numConnPerDownload = DEFAULT_NUM_CONNECT_PER_DOWNLOAD;
        downloadList = new ArrayList<Downloader>();
    }

    public int getNumConnPerDownload() {
        return numConnPerDownload;
    }

    public void setNumConnPerDownload(int numConnPerDownload) {
        this.numConnPerDownload = numConnPerDownload;
    }

    public Downloader getDownload(int index) {
        return downloadList.get(index);
    }

    public void removeDownload(int index) {
        downloadList.remove(index);
    }

    public ArrayList<Downloader> getDownloadList() {
        return downloadList;
    }

    public Downloader createDownload(URL verifiedURL, String outputFolder)  {
        HttpDownloader fileDownload = new HttpDownloader(verifiedURL, outputFolder, numConnPerDownload);
        downloadList.add(fileDownload);

        return fileDownload;
    }

    public static DownloadManager getInstance() {
        if (instane == null) {
            instane = new DownloadManager();
        }

        return instane;
    }

    public static URL verifyURL(String fileURL) {
        if (!fileURL.toLowerCase().startsWith("http://")) {
            return null;
        }

        URL verifiedURL = null;
        try {
            verifiedURL = new URL(fileURL);
        } catch (Exception e) {
            return  null;
        }

        if (verifiedURL.getFile().length() < -2) {
            return null;
        }

        return verifiedURL;
    }
}
