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

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.modules.eventrecorder.server.PositionDescriptor;
import org.jdesktop.wonderland.web.wfs.WFSManager;
import org.jdesktop.wonderland.web.wfs.WFSRecording;

/**
 * Handles Jersey RESTful requests to record the position
 * of a recording whose name is given in the position descriptor.<
 * <p>
 * URI: http://<machine>:<port>/eventrecorder/eventrecorder/resources/recordPosition
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@Path(value="/recordPosition")
public class RecordPositionResource {

    /**
     * Record an xml-ised description of a cell's position to a file
     * @param positionDesciptor The necessary information about the position to be recorded
     * @return An OK response upon success, BAD_REQUEST upon error
     */
    @POST
    @Consumes({"application/xml"})
    public Response recordPositionResource(final PositionDescriptor positionDesciptor)  {
        // Do some basic stuff, get the WFS wfsManager class, etc
        Logger logger = Logger.getLogger(RecordPositionResource.class.getName());
        WFSManager wfsManager = WFSManager.getWFSManager();
        String tapeName = positionDesciptor.getTapeName();
        if (tapeName == null) {
            logger.severe("[EventRecorder] No tape name");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        WFSRecording recording = wfsManager.getWFSRecording(tapeName);
        if (recording == null) {
            logger.warning("[Event Recorder] Unable to identify recording " + tapeName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        try {
            recording.recordPositionInfo(positionDesciptor.getPositionInfo());
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "[Event Recorder] Failed to create changes file", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
    }
    
    

    
}
