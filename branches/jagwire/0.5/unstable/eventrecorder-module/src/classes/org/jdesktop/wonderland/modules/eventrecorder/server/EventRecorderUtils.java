/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.messages.MessagePacker;
import org.jdesktop.wonderland.common.messages.MessagePacker.PackerException;
import org.jdesktop.wonderland.server.cell.CellMO;
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
     * Record the position of the event recorder.
     * @param tapeName the name of the recording for which the position should be recorded
     * @param positionState the position data, including rotation, translation, bounds and scale
     * @throws java.io.IOException if we fail to call the web service correctly (or if it's down)
     * @throws javax.xml.bind.JAXBException if the XML-isation fails
     */
    static void recordPosition(String tapeName, PositionComponentServerState positionState) throws IOException, JAXBException {
        PositionDescriptor positionDescriptor = new PositionDescriptor(tapeName, positionState);
        // Open an output connection to the URL, pass along any exceptions
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "recordPosition");

        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/xml");
        OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream());

        // Write out the class as an XML stream to the output connection
        positionDescriptor.encode(w);
        w.close();

        // For some reason, we need to read in the input for the HTTP POST to
        // work
        InputStreamReader r = new InputStreamReader(connection.getInputStream());
        while (r.read() != -1) {
            // Do nothing
        }
        r.close();
    }



    /**
     * Creates a new changes file
     * @param name the name of the recording for which the changes file should be created
     * @param timestamp the timestamp at which the changes began
     * @throws IOException
     */
    static void createChangesFile(String name, long timestamp)
            throws IOException {
        //logger.info("name: " + name);
        String encodedName = URLEncoder.encode(name, "UTF-8");
        //logger.info("encodedName: " + encodedName);
        String query = "?name=" + encodedName + "&timestamp=" + timestamp;
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "create/changesFile" + query);
        //logger.info("url: " + url);
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
    static void closeChangesFile(String name, long timestamp) throws IOException {
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
     * @param metadataDescriptor a description of the message, including the name of the recording
     * @throws java.io.IOException
     * @throws javax.xml.bind.JAXBException
     */
    static void recordChange(MessageDescriptor messageDescriptor) throws IOException, JAXBException {
        // Open an output connection to the URL, pass along any exceptions
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "recordMessage/changesFile");

        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/xml");
        OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream());

        // Write out the class as an XML stream to the output connection
        messageDescriptor.encode(w);
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
     * Record a metadata onto a changes file
     * @param metadataDescriptor a description of the metadata, including the name of the recording
     * @throws java.io.IOException
     * @throws javax.xml.bind.JAXBException
     */
    static void recordMetadata(MetadataDescriptor metadataDescriptor) throws IOException, JAXBException {
        logger.info("metadata: " + metadataDescriptor.getMetadata());
        // Open an output connection to the URL, pass along any exceptions
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "recordMetadata/changesFile");

        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/xml");
        OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream());

        // Write out the class as an XML stream to the output connection
        metadataDescriptor.encode(w);
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
     * Create a message descriptor that wraps the paramaters
     * @param tapeName the name of the recording
     * @param clientID the id of the client that sent the message
     * @param message the message received from the client that is to be recorded
     * @param timestamp the timestamp for the message
     * @return a MessageDescriptor that wraps the parameters
     * @throws org.jdesktop.wonderland.common.messages.MessagePacker.PackerException
     */
    static MessageDescriptor getMessageDescriptor(String tapeName, WonderlandClientID clientID, CellMessage message, long timestamp) throws PackerException {
        ByteBuffer byteBuffer = MessagePacker.pack(message, clientID.getID().shortValue());
        String encodedMessage = BASE_64_ENCODER.encode(byteBuffer);
        return new MessageDescriptor(tapeName, timestamp, message.getMessageID(), encodedMessage);
    }

    /**
     * Get a loaded cell descriptor for the given cell.
     * @param tapeName the name of the recording for the descriptor
     * @param cellMO the cell to get a descriptor for
     * @param parentID the id of the parent cell
     * @param timestamp the timestamp for the change
     * @return a LoadedCellDescriptor that describes the loaded cell
     * @throws IOException
     * @throws JAXBException
     */
    public static LoadedCellDescriptor getLoadedCellDescriptor(String tapeName, CellMO cellMO, CellID parentID, long timestamp)
        throws IOException, JAXBException
    {
        // Create the cell on the server, fetch the setup information from the
        // cell. If the cell does not return a valid setup object, then simply
        // ignore the cell
        CellServerState setup = cellMO.getServerState(null);
        if (setup == null) {
            return null;
        }
        // Put the cellID of the cell in its metadata
        String cellID = cellMO.getCellID().toString();
        setup.getMetaData().put("CellID", cellID);

        // Write the setup information as an XML string. If we have trouble
        // writing this, then punt.
        StringWriter sw = new StringWriter();
        setup.encode(sw);
        String setupStr = sw.toString();

        // Create the descriptor for the cell using the tape name, the timestamp
        // and setup information we obtained from the
        // cell
        return new LoadedCellDescriptor(tapeName, setupStr, parentID, timestamp);
    }

    /**
     * Appends to the changes file the description of the loaded cell
     * @param descriptor the descriptor to be recorded
     * @throws IOException
     * @throws JAXBException
     */
    public static void recordedLoadedCell(LoadedCellDescriptor descriptor)
            throws IOException, JAXBException
    {
        // Open an output connection to the URL, pass along any exceptions
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "recordLoadedCell/changesFile");

        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/xml");
        OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream());

        // Write out the class as an XML stream to the output connection
        descriptor.encode(w);
        w.close();

        // For some reason, we need to read in the input for the HTTP POST to
        // work
        InputStreamReader r = new InputStreamReader(connection.getInputStream());
        while (r.read() != -1) {
            // Do nothing
        }
        r.close();
    }

    /**
     * Append a message to the changes file of the recording with the details of the 
     * cell that's been unloaded
     * @param tapeName the name of the recording to which this change is appended
     * @param cellID the id of the cell that has been unloaded
     * @param timestamp the timestamp of the change
     * @throws java.io.IOException
     */
    public static void recordedUnloadedCell(String tapeName, CellID cellID, long timestamp) throws IOException {
        String encodedName = URLEncoder.encode(tapeName, "UTF-8");
        String query = "?name=" + encodedName + "&timestamp=" + timestamp + "&cellID=" + cellID;
        URL url = new URL(CellExporterUtils.getWebServerURL(), WEB_SERVICE_PREFIX + "recordUnloadedCell/changesFile" + query);

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String str;
        while ((str = in.readLine()) != null) {
            // str is one line of text; readLine() strips the newline character(s)
            System.out.println(str);
        }
        in.close();
    }

}
