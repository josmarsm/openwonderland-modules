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

import java.io.PrintWriter;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.jdesktop.wonderland.modules.eventrecorder.server.MetadataDescriptor;
import org.jdesktop.wonderland.web.wfs.WFSManager;
import org.jdesktop.wonderland.web.wfs.WFSRecording;
import org.jdesktop.wonderland.web.wfs.WFSRecordingWriter;

/**
 * Handles Jersey RESTful requests to append metadata to the changes file
 * of a recording whose name is given in the metadata descrptor
 * <p>
 * URI: http://<machine>:<port>/eventrecorder/eventrecorder/resources/recordMetadata/changesFile
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@Path(value="/recordMetadata/changesFile")
public class RecordMetadataResource {

    /**
     * Append metadata to a changes file
     * @param metadataDescriptor The necessary information about the metadata
     * @return An OK response upon success, BAD_REQUEST upon error
     */
    @POST
    @Consumes({"application/xml"})
    public Response recordMetadata(final MetadataDescriptor metadataDescriptor) {
        // Do some basic stuff, get the WFS wfsManager class, etc
        Logger logger = Logger.getLogger(RecordMetadataResource.class.getName());
        WFSManager wfsManager = WFSManager.getWFSManager();
        String tapeName = metadataDescriptor.getTapeName();
        if (tapeName == null) {
            logger.severe("[EventRecorder] No tape name");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        WFSRecording recording = wfsManager.getWFSRecording(tapeName);
        if (recording == null) {
            logger.severe("[EventRecorder] Unable to identify recording " + tapeName);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        logger.info("metadata: " + metadataDescriptor.getMetadata());
        WFSRecordingWriter recorder = new WFSRecordingWriter() {

            public void recordChange(PrintWriter writer) {
                writer.println("<Metadata messageID=\"" + metadataDescriptor.getMessageID() + "\">");
                //The metadata could be anything, so we need to escape it
                writer.println("<![CDATA["); //start of CDATA
                writer.println(metadataDescriptor.getMetadata());
                writer.println("]]>"); //End of CDATA
                writer.println("</Metadata>");
            }
        };
        recording.recordChange(recorder);
                
        // Formulate the response and return it
        return Response.ok().build();
    }
    
}
