package com.downloadTool.app;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpDownloader extends Downloader  {
    public HttpDownloader(URL url, String outputFolder, int numConnections) {
        super(url, outputFolder, numConnections);
        download();
    }

    private void error() {
        System.out.println("ERROR");
        setState(ERROR);
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        try {
//            open connection to URL
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);

//            connect to server
            connection.connect();

//            check for valid content length
            int contentLength = connection.getContentLength();
            if (contentLength < 1){
                error();
            }

            if (fileSize == -1) {
                fileSize = contentLength;
                stateChanged();
            }

//            if the state is DOWNLOADING (no error) -> start downloading
            if (state == DOWNLOADING) {
//                check whether we have list of download threads or not, if not -> init download
                if (listDownloadThread.size() == 0) {
//                    downloading size for each thread
                    int partSize = Math.round(((float)fileSize / numConnections) / BLOCK_SIZE ) * BLOCK_SIZE;

//                    start / end byte for each thread
                    int startByte = 0;
                    int endByte = partSize - 1;

                    HttpDownloadThread thread = new HttpDownloadThread(1, url, outputFolder + fileName, startByte, endByte);
                    listDownloadThread.add(thread);
                    int i = 2;
                    while (endByte < fileSize) {
                        startByte = endByte + 1;
                        endByte += partSize;
                        thread = new HttpDownloadThread(i, url, outputFolder + fileName, startByte, endByte);
                        listDownloadThread.add(thread);
                        ++ i;
                    }
                } else {
                    HttpDownloadThread thread = new HttpDownloadThread(1, url, outputFolder + fileName, 0, fileSize);
                    listDownloadThread.add(thread);
                }
            } else {
                for (int i = 0; i < listDownloadThread.size(); ++i) {
                    if (!listDownloadThread.get(i).isFinished()) {
                        listDownloadThread.get(i).download();
                    }
                }
            }

//            waiting for all thread to complete
            for (int i = 0; i < listDownloadThread.size(); ++i){
                listDownloadThread.get(i).waitFinish();
            }

//            check current state again
            if (state == DOWNLOADING) {
                setState(COMPLETED);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    protected class HttpDownloadThread extends DownloadThread {
        public HttpDownloadThread(int threadID, URL url, String outputFile, int startByte, int endByte) {
            super(threadID, url, outputFile, startByte, endByte);
        }

        @Override
        public void run() {
            BufferedInputStream inputStream = null;
            RandomAccessFile randomAccessFile = null;

            try {
//                open http connection to URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

//                set the range off byte to download
                String byteRange = startByte + "-" +endByte;
                connection.setRequestProperty("Range","byte=" + byteRange);

//                connect to server
                connection.connect();
//                make sure the response code is in 200 range
                if (connection.getResponseCode() / 100 != 2) {
                    error();
                }

//                get input stream
                inputStream = new BufferedInputStream(connection.getInputStream());

//                open the output file and seek to the start location
                randomAccessFile = new RandomAccessFile(outputFile, "rw");
                randomAccessFile.seek(startByte);

                byte data[] = new byte[BUFFFER_SIZE];
                int numRead;
                while ((state == DOWNLOADING) && (numRead = inputStream.read(data, 0, BUFFFER_SIZE)) != -1){
//                    write to buffer
                    randomAccessFile.write(data, 0, numRead);
//                    increase the startbyte for resume
                    startByte += numRead;
//                    increse the downloaded size
                    downloaded(numRead);
                }

                if (state == DOWNLOADING) {
                    isFinished = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (randomAccessFile != null){
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
