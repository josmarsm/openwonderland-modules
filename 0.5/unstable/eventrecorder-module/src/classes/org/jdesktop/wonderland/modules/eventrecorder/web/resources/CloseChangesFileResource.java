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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.web.wfs.WFSManager;
import org.jdesktop.wonderland.web.wfs.WFSRecording;

/**
 * Handles Jersey RESTful requests to close a changes file in a pre-determined directory according to the
 * tapeName. 
 * <p>
 * URI: http://<machine>:<port>/eventrecorder/eventrecorder/resources/close/changesFile
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@Path(value="/close/changesFile")
public class CloseChangesFileResource {

    /**
     * Closes an existing changes file. 
     * 
     * @param tapeName
     * @return An OK response upon success, BAD_REQUEST upon error
     */
    @GET
    public Response closeChangesFile(@QueryParam("name") String tapeName) {
        // Do some basic stuff, get the WFS wfsManager class, etc
        Logger logger = Logger.getLogger(CloseChangesFileResource.class.getName());
        WFSManager wfsManager = WFSManager.getWFSManager();
        if (tapeName == null) {
            logger.severe("[EventRecorder] No tape name");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        WFSRecording recording = wfsManager.getWFSRecording(tapeName);
        if (recording == null) {
            logger.severe("[EventRecorder] Unable to identify recording " + tapeName);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        recording.closeChangesFile();
        return Response.ok().build();
    }
}
