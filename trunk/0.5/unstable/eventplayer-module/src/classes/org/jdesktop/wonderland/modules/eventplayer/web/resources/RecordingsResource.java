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
package org.jdesktop.wonderland.modules.eventplayer.web.resources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.common.wfs.WFSRecordingList;
import org.jdesktop.wonderland.common.wfs.WorldRoot;
import org.jdesktop.wonderland.web.wfs.WFSManager;
import org.jdesktop.wonderland.web.wfs.WFSRecording;

/**
 * The WFSRecordingsResource class is a Jersey RESTful resource that allows clients
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
     * Returns the JAXB XML serialization of the WFS recording names. Returns
     * the XML via an HTTP GET request. The format of the URI is:
     * <p>
     * /wfs/listrecordings
     * <p>
     * Returns BAD_REQUEST to the HTTP connection upon error
     *
     * @return The XML serialization of the wfs recordings via HTTP GET
     */

    @GET
    @Path("{recording}")
    @Produces({"text/plain", "application/xml", "application/json"})
    public Response getRecording(@PathParam("recording") String tapeName) {
        Logger logger = Logger.getLogger(RecordingsResource.class.getName());
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
        WorldRoot worldRoot = new WorldRoot(recording.getRootPath());

        /* Send the serialized recording to the client */
        return Response.ok(worldRoot).build();
    }

    @GET
    @Path("{recording}/changes")
    @Produces({"text/plain"})
    public Response getChanges(@PathParam("recording") String tapeName) {
        Logger logger = Logger.getLogger(RecordingsResource.class.getName());
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
