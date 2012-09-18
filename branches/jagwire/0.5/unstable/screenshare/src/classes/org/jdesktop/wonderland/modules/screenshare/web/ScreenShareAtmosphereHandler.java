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
package org.jdesktop.wonderland.modules.screenshare.web;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.Broadcaster;

/**
 * Atmosphere handler for screen sharing
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class ScreenShareAtmosphereHandler
        implements AtmosphereHandler<HttpServletRequest, HttpServletResponse>
{
    private static final Logger LOGGER =
            Logger.getLogger(ScreenShareAtmosphereHandler.class.getName());

    private static final String BOUNDARY = "jnmnhyakmgs";

    public void onRequest(AtmosphereResource<HttpServletRequest, HttpServletResponse> ar)
            throws IOException 
    {
        HttpServletRequest req = ar.getRequest();
        HttpServletResponse res = ar.getResponse();
        Broadcaster bc = ar.getBroadcaster();

        // find the cellID
        String cellID = req.getParameter("cellID");
        if (cellID == null) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST,
                          "cellID parameter is required");
            return;
        }

        LOGGER.fine("On request " + req.getMethod() + " for " + cellID);

        if (req.getMethod().equals("GET")) {
            // set up a new receiver thread
            res.setContentType("multipart/x-mixed-replace;boundary=" + BOUNDARY);

            // write the initial data to the stream
            DataOutputStream dos = new DataOutputStream(res.getOutputStream());
            dos.writeBytes("--" + BOUNDARY + "\n");
            
            // suspend until the next broadcast
            ar.suspend();

            // start a heartbeat to ensure there is always some data flowing.
            // If there are no messages, the receiver thread may have trouble
            // disconnecting.
            bc.scheduleFixedBroadcast(new HeartbeatMessage(), 1, TimeUnit.SECONDS);

            LOGGER.fine("Finished handling GET request: receiver suspended");
        } else if (req.getMethod().equals("POST")) {
            // Handle posting an image by rebroadcasting it to all receivers.
            // First read the data.
            DataInputStream din = new DataInputStream(req.getInputStream());
            byte[] data = new byte[req.getContentLength()];
            din.readFully(data);

            // now broadcast it
            bc.broadcast(new DataMessage(cellID, data));

            // notify the sender they were successful
            res.getWriter().print("Success");
            res.getWriter().flush();

            LOGGER.fine("Finished handling post request: post added");
        }
    }

    public void onStateChange(AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> are)
            throws IOException
    {
        HttpServletResponse res = are.getResource().getResponse();
        DataOutputStream dos = new DataOutputStream(res.getOutputStream());

        // check for cancelled state -- nothing to do in that case
        if (are.isCancelled()) {
            return;
        }

        // check for heartbeat messages
        if (are.getMessage() instanceof HeartbeatMessage) {
            LOGGER.fine("Heartbeat");

            // if this is a heartbeat message, just write a new boundary to
            // the stream to ensure some data is sent
            synchronized (res) {
                dos.writeBytes("\n--" + BOUNDARY + "\n");
                dos.flush();
            }
            
            return;
        } else if (are.getMessage() instanceof DataMessage) {
            // this is a regular data message
            DataMessage dm = (DataMessage) are.getMessage();
            String cellID = dm.getCellID();
            byte[] data = dm.getData();

            LOGGER.fine("On data " + data.length + " bytes for cell " +
                        cellID + ".");

            // check if the message is for us
            if (!cellID.equals(are.getResource().getRequest().getParameter("cellID"))) {
                return;
            }

            try {
                // broadcast the data to the receiver
                synchronized (res) {
                    dos.writeBytes("Content-type: image/jpeg\n\n");
                    dos.write(data);
                    dos.writeBytes("\n--" + BOUNDARY + "\n");
                    dos.flush();
                }
            } catch (Throwable t) {
                // make sure to log any errors that occur, since otherwise they
                // are swallowed silently
                LOGGER.log(Level.WARNING, "Error writing", t);
                if (t instanceof IOException) {
                    throw (IOException) t;
                } else if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
            }
        }

        LOGGER.fine("Done with on respone");
    }

    /** empty message */
    private class HeartbeatMessage {}

    /** message containing data */
    private class DataMessage {
        private final String cellID;
        private final byte[] data;

        public DataMessage(String cellID, byte[] data) {
            this.cellID = cellID;
            this.data = data;
        }

        public String getCellID() {
            return cellID;
        }

        public byte[] getData() {
            return data;
        }
    }
}
