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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.messages.MessagePacker;
import org.jdesktop.wonderland.common.messages.MessagePacker.PackerException;
import org.jdesktop.wonderland.common.wfs.WFSRecordingList;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.wfs.exporter.CellExporterUtils;
import sun.misc.BASE64Encoder;

/**
 * A ultility class used to call web services from the darkstar service
 * @author Bernard Horan
 */
public class EventRecorderUtils {
    /* The prefix to add to URLs for the eventrecorder web service */
    private static final String WEB_SERVICE_PREFIX = "eventrecorder/eventrecorder/resources/";

    private static final Logger logger = Logger.getLogger(EventRecorderUtils.class.getName());

    final private static BASE64Encoder BASE_64_ENCODER = new BASE64Encoder();
    /**
     * Creates a new changes file
     * @param name the name of the recording for which the changes file should be created
     * @param timestamp the timestamp at which the changes began
     * @throws IOException
     */
    static void createChangesFile(String name, long timestamp)
            throws IOException {
        logger.info("name: " + name);
        String encodedName = URLEncoder.encode(name, "UTF-8");
        logger.info("encodedName: " + encodedName);
        String query = "?name=" + encodedName + "&timestamp=" + timestamp;
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "create/changesFile" + query);
        logger.info("url: " + url);
        // Read all the text returned by the server
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String str;
        while ((str = in.readLine()) != null) {
            // str is one line of text; readLine() strips the newline character(s)
            System.out.println(str);
        }
        in.close();
    }

    /**
     * Close a changes file
     * @param name the name of the recording managing the changes file
     * @throws java.io.IOException
     * @throws javax.xml.bind.JAXBException
     */
    static void closeChangesFile(String name, long timestamp) throws IOException, JAXBException {
        String encodedName = URLEncoder.encode(name, "UTF-8");
        String query = "?name=" + encodedName + "&timestamp=" + timestamp;
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "close/changesFile" + query);

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String str;
        while ((str = in.readLine()) != null) {
            // str is one line of text; readLine() strips the newline character(s)
            System.out.println(str);
        }
        in.close();
    }

    /**
     * Record a change onto a changes file
     * @param changeDescriptor a description of the change, including the name of the recording
     * @throws java.io.IOException
     * @throws javax.xml.bind.JAXBException
     */
    static void recordChange(ChangeDescriptor changeDescriptor) throws IOException, JAXBException {
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
        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            System.out.println(line);
        }
        rd.close();

    }

    /**
     * Create a change descriptor that wraps the paramaters
     * @param tapeName the name of the recording
     * @param clientID the id of the client that sent the message
     * @param message the message received from the client that is to be recorded
     * @param timestamp the timestamp for the message
     * @return a ChangeDescriptor that wraps the parameters
     * @throws org.jdesktop.wonderland.common.messages.MessagePacker.PackerException
     */
    static ChangeDescriptor getChangeDescriptor(String tapeName, WonderlandClientID clientID, CellMessage message, long timestamp) throws PackerException {
        ByteBuffer byteBuffer = MessagePacker.pack(message, clientID.getID().shortValue());
        String encodedMessage = BASE_64_ENCODER.encode(byteBuffer);
        return new ChangeDescriptor(tapeName, timestamp, encodedMessage);
    }

}
