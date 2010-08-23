/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.screenshare.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jkaplan
 */
public class Uploader implements Runnable {
    private static final Logger LOGGER =
            Logger.getLogger(Uploader.class.getName());

    private static final String SCREENSHARE_URL = "screenshare/screenshare/ScreenShareAtmosphereHandler";
    private static final ScheduledExecutorService executor = 
            Executors.newScheduledThreadPool(2, new ServerThreadFactory());

    private final ImageReader reader;
    private final String uploadURL;
    private final String cellID;

    private ScheduledFuture scheduled = null;

    public Uploader(ImageReader reader, String uploadURL, String cellID) {
        this.reader = reader;
        this.uploadURL = uploadURL;
        this.cellID = cellID;
    }

    public synchronized void start() {
        if (scheduled == null) {
            scheduled = executor.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
        }
    }

    public void run() {
        try {
            byte[] nextImage = getReader().getImage();
            if (nextImage == null) {
                return;
            }

            HttpURLConnection huc =
                    (HttpURLConnection) new URL(uploadURL).openConnection();
            huc.setDoInput(true);
            huc.setDoOutput(true);
            huc.setRequestMethod("POST");
            huc.setRequestProperty("Content-type", "image/jpeg");
            huc.setRequestProperty("Content-length",
                                   String.valueOf(nextImage.length));

            LOGGER.fine("Post " + nextImage.length + " bytes to " +
                        uploadURL);

            huc.getOutputStream().write(nextImage);
            huc.getOutputStream().flush();

            int res = huc.getResponseCode();
            String msg = huc.getResponseMessage();

            LOGGER.fine("Result: " + res + " " + msg);
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error posting image", ioe);
            stop();
        }
    }

    public synchronized void stop() {
        if (scheduled != null) {
            scheduled.cancel(true);
            scheduled = null;
        }
    }

    private ImageReader getReader() {
        return reader;
    }

    private static class ServerThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setName("ScreenShare Uploader Thread");
            return t;
        }
    }
}
