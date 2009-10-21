/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.pdfviewer.client;

import com.sun.pdfview.PDFFile;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A loader for PDF documents. This can be time consuming, so load in a thread
 *
 * @author nsimpson
 */
public class PDFDocumentLoader extends Thread {

    private static final Logger logger =
            Logger.getLogger(PDFDocumentLoader.class.getName());
    private URL url;
    private PDFFile pdfFile;
    boolean loadInProgress = false;
    private ConcurrentLinkedQueue<PDFDocumentLoaderListener> listeners;

    public PDFDocumentLoader() {
        listeners = new ConcurrentLinkedQueue();
    }

    public PDFDocumentLoader(URL url) {
        this.url = url;
    }

    /** 
     * Sets the URL of the PDF document to load
     * @param url the document URL
     */
    public void setDocument(URL url) {
        this.url = url;
    }

    /**
     * Get the PDF document data from a URL
     * @param docURL the URL of the PDF document to open
     * @return the PDF document data
     */
    private ByteBuffer getDocumentData(URL docURL) throws IOException {
        ByteBuffer buf = null;

        if (docURL != null) {
            // connect to the URL
            URLConnection conn = docURL.openConnection();
            conn.connect();

            // download the data into a temporary file
            File pdfTemp = File.createTempFile("wl_pdf", "tmp");
            pdfTemp.deleteOnExit();
            RandomAccessFile raf = new RandomAccessFile(pdfTemp, "rw");

            // create a buffer to load the document into
            byte[] dataBuffer = new byte[32 * 1024];
            BufferedInputStream is = null;
            int read;
            long fileSize = 0;

            // write the data to the file
            try {
                is = new BufferedInputStream(conn.getInputStream());
                while ((read = is.read(dataBuffer, 0, dataBuffer.length)) > 0) {
                    raf.write(dataBuffer, 0, read);
                    fileSize += read;
                }
            } finally {
                is.close();
            }

            // now convert the file into a byte buffer by mapping it
            buf = raf.getChannel().map(MapMode.READ_ONLY, 0, fileSize);
        }

        return buf;
    }

    @Override
    public void run() {
        if (url != null) {
            logger.info("loading PDF: " + url);
            loadInProgress = true;
            PDFFile loadingFile = null;
            pdfFile = null;

            try {
                // attempt to load the document
                Date then = new Date();
                loadingFile = new PDFFile(getDocumentData(url));
                Date now = new Date();

                logger.info("loaded PDF in: " + (now.getTime() - then.getTime()) / 1000 + " seconds");
            } catch (Exception e) {
                logger.log(Level.WARNING, "failed to open: " + url + ": " + e, e);
                notifyListeners(url, false, e);
            }
            if (loadingFile != null) {
                // document was loaded successfully
                pdfFile = loadingFile;
                notifyListeners(url, true, null);
            }
            loadInProgress = false;
        }
    }

    /**
     * Get the loaded PDF file. Note that the PDF file is not guaranteed
     * until isLoading() returns true or documentLoadStateChange(...) is
     * invoked on a PDFDocumentLoaderListener.
     *
     * @return the PDF File if the document was successfully loaded, null
     * otherwise
     */
    public PDFFile getPDFFile() {
        return pdfFile;
    }

    /**
     * Returns the status of a document load
     * @return true if a document is in the process of being loaded, false
     * otherwise
     */
    public boolean isLoading() {
        return loadInProgress;
    }

    /**
     * Add a doucment loader listener
     * @param listener a listener to be notified of document loading
     */
    public void addListener(PDFDocumentLoaderListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a doucment loader listener
     * @param listener a listener to be removed
     */
    public void removeListener(PDFDocumentLoaderListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners(URL url, boolean loaded, Exception e) {
        Iterator<PDFDocumentLoaderListener> iter = listeners.iterator();
        while (iter.hasNext()) {
            PDFDocumentLoaderListener listener = iter.next();
            listener.documentLoadStateChanged(url, loaded, e);
        }
    }

    /**
     * An interface for classes interested in document loading status
     */
    public interface PDFDocumentLoaderListener {

        public void documentLoadStateChanged(URL url, boolean loaded, Exception e);
    }
}

