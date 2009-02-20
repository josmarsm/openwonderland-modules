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

package org.jdesktop.wonderland.modules.eventrecorder.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.modules.eventrecorder.server.ChangesFile;
import org.jdesktop.wonderland.server.wfs.exporter.CellExporterUtils;

/**
 *
 * @author Bernard Horan
 */
public class EventRecorderUtils {
    /* The prefix to add to URLs for the eventrecorder web service */
    public static final String WEB_SERVICE_PREFIX = "eventrecorder/eventrecorder/resources/";
    /**
     * Creates a new changes file, returns a ChangesFile object representing the
     * new changes file or null upon failure
     */
    public static ChangesFile createChangesFile(String name, long timestamp)
            throws IOException, JAXBException
    {
        String encodedName = URLEncoder.encode(name, "UTF-8");
        String query = "?name=" + encodedName + "&timestamp=" + timestamp;
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "create/changesFile" + query);

        return ChangesFile.decode(new InputStreamReader(url.openStream()));
    }

    /**
     * 
     * @param name
     * @return
     * @throws java.io.IOException
     * @throws javax.xml.bind.JAXBException
     */
    public static ChangesFile closeChangesFile(String name) throws IOException, JAXBException {
        String encodedName = URLEncoder.encode(name, "UTF-8");
        String query = "?name=" + encodedName;
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "close/changesFile" + query);

        return ChangesFile.decode(new InputStreamReader(url.openStream()));
    }

    

}
