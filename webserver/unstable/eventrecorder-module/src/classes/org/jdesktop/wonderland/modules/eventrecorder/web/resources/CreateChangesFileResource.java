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
package org.jdesktop.wonderland.modules.eventrecorder.web.resources;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.web.wfs.WFSManager;
import org.jdesktop.wonderland.web.wfs.WFSRecording;

/**
 * Handles Jersey RESTful requests to create a changes file in a pre-determined directory according to the
 * name. 
 * <p>
 * URI: http://<machine>:<port>/eventrecorder/eventrecorder/resources/create/changesFile?name=Untitled+Tape&timestamp=99999
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@Path(value="/create/changesFile")
public class CreateChangesFileResource {

    /**
     * Creates a new changes file. 
     * 
     * @param name of the tape
     * @param timestamp the start time at which the recording has begun
     * @return a response to indicate success
     */
    @GET
    public Response createChangesFile(@QueryParam("name") String name, @QueryParam("timestamp") long timestamp) {
        // Do some basic stuff, get the WFS wfsManager class, etc
        Logger logger = Logger.getLogger(CreateChangesFileResource.class.getName());
        WFSManager wfsManager = WFSManager.getWFSManager();
        WFSRecording recording = wfsManager.getWFSRecording(name);
        if (recording == null) {
            logger.warning("[Event Recorder] Unable to identify recording " + name);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        try {
            recording.createChangesFile(timestamp);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "[Event Recorder] Failed to create changes file", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
        
    }
}
