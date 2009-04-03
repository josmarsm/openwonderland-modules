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

package org.jdesktop.wonderland.modules.eventplayer.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.messages.MessagePacker;
import org.jdesktop.wonderland.common.messages.MessagePacker.PackerException;
import org.jdesktop.wonderland.modules.eventplayer.server.ChangesFile;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.wfs.exporter.CellExporterUtils;
import sun.misc.BASE64Encoder;

/**
 *
 * @author Bernard Horan
 */
public class EventPlayerUtils {
    /* The prefix to add to URLs for the eventrecorder web service */
    public static final String WEB_SERVICE_PREFIX = "eventplayer/eventplayer/resources/";

    final private static BASE64Encoder BASE_64_ENCODER = new BASE64Encoder();
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

    public static void playChange(ChangeDescriptor changeDescriptor) throws IOException, JAXBException {
        // Open an output connection to the URL, pass along any exceptions
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "append/changesFile");

        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/xml");
        OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream());

        // Write out the class as an XML stream to the output connection
        changeDescriptor.encode(w);
        w.close();

        // For some reason, we need to read in the input for the HTTP POST to
        // work
        InputStreamReader r = new InputStreamReader(connection.getInputStream());
        while (r.read() != -1) {
            // Do nothing
        }
        r.close();
    }

    static ChangeDescriptor getChangeDescriptor(String tapeName, WonderlandClientID clientID, CellMessage message, long timestamp) throws PackerException {
        ByteBuffer byteBuffer = MessagePacker.pack(message, clientID.getID().shortValue());
        String encodedMessage = BASE_64_ENCODER.encode(byteBuffer);
        return new ChangeDescriptor(tapeName, timestamp, encodedMessage);
    }

    

}
