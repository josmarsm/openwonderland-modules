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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.common.wfs.WorldRoot;
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
 * URI: http://<machine>:<port>/wonderland-web-wfs/wfs/create/changesFile
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@Path(value="/create/changesFile")
public class CreateChangesFileResource {

    /**
     * Creates a new changes file. Adds a
     * new WFS object and creates the entry on disk. Returns a WorldRoot object
     * that represents the new recording
     * 
     * @param name 
     * @param timestamp
     * @return A ChangesFile object
     */
    @GET
    @Produces({"text/plain", "application/xml", "application/json"})
    public Response createChangesFile(@QueryParam("name") String name, @QueryParam("timestamp") long timestamp) {
        // Do some basic stuff, get the WFS wfsManager class, etc
        Logger logger = Logger.getLogger(CreateChangesFileResource.class.getName());
        WFSManager wfsManager = WFSManager.getWFSManager();
        WFSRecording recording = wfsManager.getWFSRecording(name);
        if (recording == null) {
            logger.warning("[WFS] Unable to identify recording " + name);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }

        ChangesManager cManager = ChangesManager.getChangesManager();
        ChangesFile changesFile = cManager.createChangesFile(recording, timestamp);
        // Create the changes file check return value is not null (error if so)
        if (changesFile == null) {
            logger.warning("[WFS] Unable to create changesFile " + name);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        
        // Formulate the response and return the world root object
        ResponseBuilder rb = Response.ok(changesFile);
        return rb.build();
    }
}
