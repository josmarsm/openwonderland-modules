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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.modules.eventrecorder.server.WFSRecordingList;
import org.jdesktop.wonderland.web.wfs.WFSManager;
import org.jdesktop.wonderland.web.wfs.WFSRecording;
import org.jdesktop.wonderland.web.wfs.WFSRoot;


/**
 * The WFSRootsResource class is a Jersey RESTful resource that allows clients
 * to query for the WFS recording names by using a URI.
 * <p>
 * The format of the URI is: http://<machine>:<port>/eventrecorder/eventrecorder/resources/recordings.
 * <p>
 * The recordings information returned is the JAXB serialization of the root name
 * information (the WFSRoots class). The getCellResource() method handles the
 * HTTP GET request
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @uathor Bernard Horan
 */
@Path(value="/recordings")
public class WFSRecordingsResource {
    
    /**
     * Returns the JAXB XML serialization of the WFS recording names. Returns
     * the XML via an HTTP GET request. The format of the URI is:
     * <p>
     * /wfs/recordings
     * <p>
     * Returns BAD_REQUEST to the HTTP connection upon error
     *
     * @return The XML serialization of the cell setup information via HTTP GET
     */
    @GET
    @Produces("text/plain")
    public Response getRecordings() {
        /* Fetch thhe error logger for use in this method */
        Logger logger = WFSManager.getLogger();
        
        /*
         * Fetch the wfs manager and the individual recording names. If the recordings
         * is null, then return a blank response.
         */
        WFSManager wfsm = WFSManager.getWFSManager();
        List<WFSRecording> recordingList = wfsm.getWFSRecordings();
        List<String> recordingNames = new ArrayList<String>(recordingList.size());
        for (WFSRoot recording : recordingList) {
            recordingNames.add(recording.getName());
        }
        WFSRecordingList wfsRecordings = new WFSRecordingList(recordingNames.toArray(new String[0]));
        
        /* Send the serialized recording names to the client */
        try {
            StringWriter sw = new StringWriter();
            wfsRecordings.encode(sw);
            ResponseBuilder rb = Response.ok(wfsRecordings);
            return rb.build();
        } catch (javax.xml.bind.JAXBException excp) {
            logger.warning("WFSManager: Unable to write recordings: " + excp.toString());
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
    }
}
