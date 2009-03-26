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

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.common.wfs.WorldRoot;
import org.jdesktop.wonderland.modules.eventplayer.server.ChangeDescriptor;
import org.jdesktop.wonderland.tools.wfs.WFS;
import org.jdesktop.wonderland.modules.eventplayer.server.ChangesFile;
import org.jdesktop.wonderland.modules.eventplayer.web.ChangesManager;
import org.jdesktop.wonderland.web.wfs.WFSManager;
import org.jdesktop.wonderland.web.wfs.WFSRecording;
import org.jdesktop.wonderland.web.wfs.WFSSnapshot;

/**
 * Handles Jersey RESTful requests to create a changes file in a pre-determined directory according to the
 * name. Returns an XML representation of the WorldRoot class
 * given the unique path of the wfs for later reference.
 * <p>
 * URI: http://<machine>:<port>/wonderland-web-wfs/wfs/append/changesFile
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@Path(value="/append/changesFile")
public class AppendChangesFileResource {

    /**
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

        ChangesManager cManager = ChangesManager.getChangesManager();
        ChangesFile changesFile = cManager.getChangesFile(tapeName);
        // Create the changes file check return value is not null (error if so)
        if (changesFile == null) {
            logger.warning("[EventRecorder] Unable to locate changesFile " + tapeName);
            Response.status(Response.Status.BAD_REQUEST).build();
        }
        changesFile.appendMessage(changeDescriptor.getEncodedMessage(), changeDescriptor.getTimestamp());
        
        
        // Formulate the response and return the world root object
        return Response.ok().build();
    }
}
