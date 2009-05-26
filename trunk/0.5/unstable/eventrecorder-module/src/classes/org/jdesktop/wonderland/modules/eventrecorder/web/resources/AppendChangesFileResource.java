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

import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.modules.eventrecorder.server.ChangeDescriptor;
import org.jdesktop.wonderland.web.wfs.WFSManager;
import org.jdesktop.wonderland.web.wfs.WFSRecording;

/**
 * Handles Jersey RESTful requests to append a message to the changes file
 * of a recording whose name is given in the change descrptor
 * <p>
 * URI: http://<machine>:<port>/eventrecorder/eventrecorder/resources/append/changesFile
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@Path(value="/append/changesFile")
public class AppendChangesFileResource {

    /**
     * Append a message to a changes file
     * @param changeDescriptor The necessary information about the change message
     * @return An OK response upon success, BAD_REQUEST upon error
     */
    @POST
    @Consumes({"application/xml"})
    public Response appendChangesFile(ChangeDescriptor changeDescriptor) {
        // Do some basic stuff, get the WFS wfsManager class, etc
        Logger logger = Logger.getLogger(AppendChangesFileResource.class.getName());
        WFSManager wfsManager = WFSManager.getWFSManager();
        String tapeName = changeDescriptor.getTapeName();
        if (tapeName == null) {
            logger.severe("[EventRecorder] No tape name");
            Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        WFSRecording recording = wfsManager.getWFSRecording(tapeName);
        if (recording == null) {
            logger.severe("[EventRecorder] Unable to identify recording " + tapeName);
            Response.status(Response.Status.BAD_REQUEST).build();
        }

        recording.appendChangeMessage(changeDescriptor.getEncodedMessage(), changeDescriptor.getTimestamp());
                
        // Formulate the response and return the world root object
        return Response.ok().build();
    }
}
