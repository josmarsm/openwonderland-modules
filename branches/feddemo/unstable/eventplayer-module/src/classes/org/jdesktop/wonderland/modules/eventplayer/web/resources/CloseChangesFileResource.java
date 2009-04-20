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

import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.modules.eventplayer.server.ChangesFile;
import org.jdesktop.wonderland.modules.eventplayer.web.ChangesManager;

/**
 * Handles Jersey RESTful requests to close a changes file in a pre-determined directory according to the
 * tapeName. Returns an XML representation of the ChangesFile class
 * given the unique path of the wfs for later reference.
 * <p>
 * URI: http://<machine>:<port>/wonderland-web-wfs/wfs/close/changesFile
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@Path(value="/close/changesFile")
public class CloseChangesFileResource {

    /**
     * Closes an existing changes file. Adds a
     * new WFS object and creates the entry on disk. Returns a WorldRoot object
     * that represents the new recording
     * 
     * @param tapeName
     * @return A ChangesFile object
     */
    @GET
    @Produces({"text/plain", "application/xml", "application/json"})
    public Response closeChangesFile(@QueryParam("name") String tapeName) {
        // Do some basic stuff, get the WFS wfsManager class, etc
        Logger logger = Logger.getLogger(CloseChangesFileResource.class.getName());
        ChangesManager cManager = ChangesManager.getChangesManager();
        ChangesFile cFile = cManager.getChangesFile(tapeName);
        if (cFile == null) {
            logger.warning("[WFS] Unable to identify changes file for " + tapeName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        cFile.closeFile();
        cManager.removeChangesFile(tapeName);
        
        
        // Formulate the response and return the world root object
        ResponseBuilder rb = Response.ok(cFile);
        return rb.build();
    }
}
