package com.downloadTool.app;

import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;

public abstract class Downloader extends Observable implements Runnable {
//    the URL to download file
    protected URL url;
//    output folder for download file
    protected String outputFolder;
//    number connection (threads) to download file
    protected int numConnections;
//    file name, extracted from URL
    protected String fileName;
//    size of download file (in Bytes)
    protected int fileSize;
//    the state of the download
    protected int state;
//    downloaded of file download (int Bytes)
    protected int downloaded;
//    list of download threads
    protected ArrayList<DownloadThread> listDownloadThread;

//    contants for block and buffer size
    protected static final int BLOCK_SIZE = 4096;
    protected static final int BUFFFER_SIZE = 4096;
    protected static final int MIN_DOWNLOAD_SIZE = BLOCK_SIZE * 100;

//    status name
    public static final String STATUS[] = {"Downloading", "Paused", "Complete", "Cancelled", "ERROR"};

//    contants for download's state
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETED = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;

    public Downloader(URL url, String outputFolder, int numConnections) {
        this.url = url;
        this.outputFolder = outputFolder;
        this.numConnections = numConnections;

        //        Get file name from URL path
        String fileURL = splitURL(url);
        System.out.println(fileURL);

        setFileName(fileURL.substring(fileURL.lastIndexOf("/")));

        fileSize = -1;
        state = DOWNLOADING;
        downloaded = 0;

        listDownloadThread = new ArrayList<DownloadThread>();
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    //    split the URL
    public String splitURL(URL url) {
        String urlStr = url.toString();

        urlStr = urlStr.replaceAll("%2E", ".");
        urlStr = urlStr.replaceAll("%2F", "/");
        urlStr = urlStr.replaceAll("%3A", ":");

        return urlStr;
    }

//    pause the download
    public void pause(){
        setState(PAUSED);
    }

//    resume the download
    public void resume(){
        setState(DOWNLOADING);
        download();
    }

//    cancel the download
    public void cancel(){
        setState(CANCELLED);
    }

//    get the URL (to String)
    public String getURL() {
        return url.toString();
    }

//    get the download file's size
    public int getFileSize() {
        return fileSize;
    }

//    get the current progress of the download
    public float getProgress() {
        return (((float) downloaded / fileSize) *100);
    }

//    get current state of the downloader
    protected void setState(int value){
        state = value;
        stateChanged();
    }

//    start or resume download
    protected void download() {
        Thread t = new Thread(this);
        t.start();
    }
    
//    increase the download size
    protected synchronized void downloaded(int value) {
        downloaded += value;
        stateChanged();
    }

//    set the state has changed an notify the observers
    protected void stateChanged() {
        setChanged();
        notifyObservers();
    }

    public int getState() {
        return state;
    }

    //  thread to download part of file
    protected abstract class DownloadThread implements Runnable {
        protected int threadID;
        protected URL url;
        protected String outputFile;
        protected int startByte;
        protected int endByte;
        protected boolean isFinished;
        protected Thread thread;

        public DownloadThread(int threadID, URL url, String outputFile, int startByte, int endByte) {
            this.threadID = threadID;
            this.url = url;
            this.outputFile = outputFile;
            this.startByte = startByte;
            this.endByte = endByte;

            download();
        }

        //    get whether the thread is finished download the part of file
        public boolean isFinished() {
            return isFinished;
        }

        //    start or resume the download
        public void download() {
            thread = new Thread(this);
            thread.start();
        }

        //    waiting the thread to finish
        public void waitFinish() throws InterruptedException {
            thread.join();
        }
    }
}
