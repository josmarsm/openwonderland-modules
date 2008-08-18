/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.videomodule.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 * HTTPDownloader downloads a file from an HTTP server to a local file.
 * It can be run in a thread and can provide alerts when a specified
 * number of bytes have been transferred.
 * 
 * @author nsimpson
 */
public class HTTPDownloader extends Thread {

    private static final Logger logger =
            Logger.getLogger(HTTPDownloader.class.getName());
    private static final int DEFAULT_ALERT_LIMIT = 2048; // bytes
    private String fromURL;
    private String toFile;
    private InputStream httpIS;
    private OutputStream fileOS;
    private long downloaded;
    private boolean downloadRequired;
    private boolean downloadStarted;
    private boolean downloadComplete;
    private URLConnection connection;
    private Calendar started;
    private long downloadAlert = DEFAULT_ALERT_LIMIT;
    private boolean alertTriggered = false;
    private long remoteSize = 0;
    private long localSize = 0;
    private long remaining = 0;
    private boolean stopDownloading = false;
    
    public enum DOWNLOAD_STATUS {

        NOT_STARTED, STARTED, COMPLETE, FAILED
    };

    public HTTPDownloader() {
        this(null, null, 0);
    }

    public HTTPDownloader(String fromURL, String toFile) {
        this(fromURL, toFile, 0);
    }

    public HTTPDownloader(String fromURL, String toFile, long alertInterval) {
        this.fromURL = fromURL;
        this.toFile = toFile;
        this.downloadAlert = alertInterval;
    }

    /**
     * Gets the size of the remote file
     * @return the size of the remote file in bytes
     */
    public long getDownloadSize() {
        return remoteSize;
    }

    /**
     * Gets the number of bytes downloaded from the HTTP server
     * @return the number of bytes downloaded
     */
    public long getDownloaded() {
        return downloaded;
    }

    /**
     * Gets the number of bytes remaining to be downloaded
     * @return the number of bytes remaining
     */
    public long getDownloadRemaining() {
        return remaining;
    }

    /** 
     * Gets whether the download has started
     * @return true if the download has started, false otherwise
     */
    public boolean downloadStarted() {
        return downloadStarted;
    }

    /**
     * Gets whether the download has completed
     * @return true if the download has completed, false otherwise
     */
    public boolean downloadComplete() {
        return downloadComplete;
    }

    public String getLocalFile() {
        return toFile;
    }

    /**
     * Gets the bandwidth of the current file transfer
     * @return the bandwidth in KB/s (kilobytes per second)
     */
    public double getBandwidth() {
        double bandwidth = 0;
        if (downloadStarted() && !downloadComplete()) {
            Calendar now = Calendar.getInstance();
            double a = (downloaded / 1024d); // KB
            double b = (now.getTimeInMillis() - started.getTimeInMillis()) / 1000; //s
            bandwidth = (b > 0) ? a / b : 0;
        }
        return bandwidth;
    }

    /**
     * Sets an alert which will be triggered when the specified number of bytes
     * have been transferred
     * @param downloadAlert the alert point in bytes transferred
     */
    public void setAlert(long downloadAlert) {
        logger.fine("setting alert to: " + downloadAlert);
        this.downloadAlert = downloadAlert;
        alertTriggered = false;
    }

    /**
     * Trigger an alert to any waiting thread that a specified number of bytes
     * have been transferred
     */
    private void triggerAlert() {
        synchronized (this) {
            logger.fine("download alert triggered at " + downloaded + " bytes");
            downloadAlert = 0;
            alertTriggered = true;
            notifyAll();
        }
    }

    /**
     * Gets whether the alert has been triggered
     * @return true if the alert has been triggered, false otherwise
     */
    public boolean alertTriggered() {
        return alertTriggered;
    }

    /**
     * Gets an estimate of the amount of time remaining to transfer the file
     * @return the time in seconds to complete the transfer
     */
    public int getRemainingTime() {
        int time = 0;
        if (!downloadComplete) {
            time = (int) (remaining / 1024d / getBandwidth());
        }
        return time;
    }

    /**
     * Reset the state of the downloader
     */
    public void reset() {
        httpIS = null;
        fileOS = null;
        downloadAlert = 0;
        alertTriggered = false;
        downloadRequired = true;
        downloadStarted = false;
        downloadComplete = false;
        downloaded = 0;
        remoteSize = 0;
        localSize = 0;
        remaining = 0;
    }

    public boolean downloadRequired() {
        return downloadRequired(fromURL, toFile);
    }
    
    public boolean downloadRequired(String remote, String local) {
        boolean required = true;
        if ((remote != null) && (local != null)) {
            try {
                // check for presence of local file
                File localFile = new File(toFile);
                connection = new URL(remote).openConnection();
                remoteSize = connection.getContentLength();

                if (localFile.exists()) {
                    // file already exists, check the size to make sure it's
                    // the same as the remote
                    localSize = localFile.length();
                    if (localSize == remoteSize) {
                        required = false;
                        logger.fine("file already downloaded");
                    }
                }
            } catch (Exception e) {
                logger.fine("failed to check status of file: " + remote + ": " + e);
            }
        }
        return required;
    }

    /**
     * Download a previous specified file
     */
    public void download() {
        download(fromURL, toFile, downloadAlert);
    }

    /**
     * Download a file from an HTTP server to a local file
     * @param fromURL the URL for the file to download
     * @param toFile the fully qualified path of the destination file
     * @param downloadAlert how many bytes to transfer before notifying
     */
    public void download(String from, String to, long alert) {
        reset();
        this.fromURL = from;
        this.toFile = to;
        this.downloadAlert = alert;
        stopDownloading = false;
        
        try {
            if (downloadRequired(fromURL, toFile)) {
                logger.fine("download starting");
                fileOS = new BufferedOutputStream(new FileOutputStream(toFile));
                httpIS = connection.getInputStream();
                byte[] buffer = new byte[1024];
                int read = 0;

                started = Calendar.getInstance();
                downloadStarted = true;

                while (((read = httpIS.read(buffer)) != -1) && (!stopDownloading)) {
                    fileOS.write(buffer, 0, read);
                    downloaded += read;
                    if ((downloadAlert > 0) && (downloaded >= downloadAlert)) {
                        triggerAlert();
                    }
                    remaining = remoteSize - downloaded;
                }
            }
        } catch (Exception e) {
            logger.fine("failed to retrieve file: " + e);
            reset();
        } finally {
            try {
                if (httpIS != null) {
                    httpIS.close();
                }
                if (fileOS != null) {
                    fileOS.close();
                }
            } catch (IOException e) {
            }
        }
        synchronized (this) {
            downloadComplete = true;
            stopDownloading = false;
            this.notifyAll();
        }
    }

    public void abortDownload() {
        logger.info("downloader: aborting download");
        stopDownloading = true;
    }
    
    /** 
     * Download a file in a separate thread
     */
    @Override
    public void run() {
        if ((fromURL != null) && (toFile != null)) {
            logger.info("starting download of: " + fromURL + " to: " + toFile);
            download(fromURL, toFile, downloadAlert);
            logger.info("completed downloading: " + fromURL + " to: " + toFile);
        }
    }

    /**
     * Get a fully qualified path to a temporary file based on an HTTP URL
     * @param address the HTTP URL for the file
     * @return the path to a file in the system's temporary directory
     */
    public static String getTempFilename(String address) {
        String filename = null;     // the local filename

        if (address != null) {
            try {
                // The file will be downloaded to the system's temporary directory.
                // Since we want to avoid downloading the file multiple times, we
                // use the name of the actual file in the temporary directory. This
                // way we can detect if the file has already been downloaded. We
                // use the size of file to determine if the copy of file needs to be 
                // updated by re-downloading the file from the server.

                // extract the name of the file from a URL string
                int lastPath = address.lastIndexOf('/');
                if (lastPath != -1) {
                    filename = address.substring(lastPath + 1, address.length());
                } else {
                    filename = address;
                }

                // create a temporary file to determine the path of the temporary 
                // directory on this system
                File temp = File.createTempFile(filename, null);
                String path = temp.getPath();
                lastPath = path.lastIndexOf(File.separator);

                if (lastPath != -1) {
                    path = path.substring(0, lastPath);
                }

                // append name of file to download to create a copy in the 
                // system's temporary directory
                filename = path + File.separatorChar + filename;
                temp.delete();
            } catch (IOException e) {
                logger.fine("failed to create temporary file: " + e);
            }
        }
        return filename;
    }
}

