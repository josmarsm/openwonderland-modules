/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.eventplayer.web.resources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.modules.eventplayer.server.RecordingRoot;
import org.jdesktop.wonderland.web.wfs.WFSManager;
import org.jdesktop.wonderland.web.wfs.WFSRecording;

/**
 * The RecordingsResource class is a Jersey RESTful resource that allows clients
 * to query for the WFS recording names by using a URI.
 * <p>
 * The format of the URI is: http://<machine>:<port>eventplayer/eventplayer/resources/getrecording.
 * <p>
 * The recordings information returned is the JAXB serialization of the recording name
 * information (the WFSRecordingList class). The getRecordings() method handles the
 * HTTP GET request
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@Path(value = "/getrecording")
public class RecordingsResource {

    /**
     * Returns the JAXB XML serialization of the RecordingRoot for the given
     * recording. Returns
     * the XML via an HTTP GET request. The format of the URI is:
     * <p>
     * http://<machine>:<port>/eventplayer/eventplayer/resources/getrecording/{recording_name}
     * <p>
     * Returns BAD_REQUEST to the HTTP connection upon error
     *
     * @param encodedName the encoded name of the recording
     * @return The XML serialization of the RecordingRoot of the recording via HTTP GET
     */

    @GET
    @Path("{recording}")
    @Produces({"text/plain", "application/xml", "application/json"})
    public Response getRecording(@PathParam("recording") String encodedName) {
        Logger logger = Logger.getLogger(RecordingsResource.class.getName());
        //logger.info("encodedName: " + encodedName);
        String tapeName = null;
        try {
            tapeName = URLDecoder.decode(encodedName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, "[EventPlayer] Failed to decode tapeName", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        //logger.info("tapeName: " + tapeName);

        if (tapeName == null) {
            logger.severe("[EventPlayer] No tape name");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        WFSManager wfsManager = WFSManager.getWFSManager();
        WFSRecording recording = wfsManager.getWFSRecording(tapeName);
        if (recording == null) {
            logger.severe("[EventPlayer] Unable to identify recording " + tapeName);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        // Form the root path of the wfs: "recordings/<name>/world-wfs"
        RecordingRoot recordingRoot = new RecordingRoot(recording.getRootPath());

        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(recording.getPositionFile()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "[EventPlayer] Failed to read position info", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "[EventPlayer] failed to close reader", ex);
            }
        }

        String positionInfo = sb.toString();
        recordingRoot.setPositionInfo(positionInfo);

        /* Send the serialized recording to the client */
        return Response.ok(recordingRoot).build();
    }


    /**
     * Returns a stream containing the content of the given recording's
     * changes file via an HTTP GET request. The format of the URI is:
     * <p>
     * http://<machine>:<port>eventplayer/eventplayer/resources/getrecording/{recording_name}/changes
     * <p>
     * Returns BAD_REQUEST to the HTTP connection upon error
     *
     * @param encodedName the encoded name of the recording
     * @return stream of the changes file of the recording via HTTP GET
     */
    @GET
    @Path("{recording}/changes")
    @Produces({"text/plain"})
    public Response getChanges(@PathParam("recording") String encodedName) {
        Logger logger = Logger.getLogger(RecordingsResource.class.getName());
        //logger.info("encodedName: " + encodedName);
        String tapeName = null;
        try {
            tapeName = URLDecoder.decode(encodedName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, "[Eventplayer] Failed to decode tapeName", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        //logger.info("tapeName: " + tapeName);
        if (tapeName == null) {
            logger.severe("[EventPlayer] No tape name");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        WFSManager wfsManager = WFSManager.getWFSManager();
        WFSRecording recording = wfsManager.getWFSRecording(tapeName);
        if (recording == null) {
            logger.severe("[EventPlayer] Unable to identify recording " + tapeName);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(recording.getChangesFile()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            ResponseBuilder rb = Response.ok(sb.toString());
            return rb.build();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "[EventPlayer] Failed to write changes file to stream", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "[EventPlayer] failed to close reader", ex);
            }
        }
    }
}
