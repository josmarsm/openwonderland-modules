/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.eventrecorder.server;

import org.jdesktop.wonderland.server.eventrecorder.*;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.TransactionProxy;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.messages.MessagePacker;
import org.jdesktop.wonderland.common.messages.MessagePacker.PackerException;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import sun.misc.BASE64Encoder;

/**
 *
 * @author Bernard Horan
 */
public class EventRecordingService extends AbstractService implements EventRecordingManager {

    /** The name of this class. */
    private static final String NAME = EventRecordingService.class.getName();
    /** The package name. */
    private static final String PKG_NAME = "org.jdesktop.wonderland.server.eventrecorder";
    /** The logger for this class. */
    private static final LoggerWrapper logger =
            new LoggerWrapper(Logger.getLogger(PKG_NAME));
    /** The name of the version key. */
    private static final String VERSION_KEY = PKG_NAME + ".service.version";
    /** The major version. */
    private static final int MAJOR_VERSION = 1;
    /** The minor version. */
    private static final int MINOR_VERSION = 0;
    final private static String ENCODING = "ISO-8859-1";
    final private static BASE64Encoder BASE_64_ENCODER = new BASE64Encoder();
    private Map<String, PrintWriter> recorderTable = new HashMap<String, PrintWriter>();
    private long timeOfLastChange = 0l;

    public EventRecordingService(Properties props,
            ComponentRegistry registry,
            TransactionProxy proxy) {
        super(props, registry, proxy, logger);


        logger.log(Level.CONFIG, "Creating EventRecordingService properties:{0}",
                props);



        try {
            /*
             * Check service version.
             */
            transactionScheduler.runTask(new KernelRunnable() {

                public String getBaseTaskType() {
                    return NAME + ".VersionCheckRunner";
                }

                public void run() {
                    checkServiceVersion(
                            VERSION_KEY, MAJOR_VERSION, MINOR_VERSION);
                }
            }, taskOwner);
        } catch (Exception ex) {
            logger.logThrow(Level.SEVERE, ex, "Error creating service");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void doReady() {
        //Nothing to do
    }

    @Override
    protected void doShutdown() {
        //Close any open files
        for (String recorderName : recorderTable.keySet()) {
            stopRecording(recorderName);
        }
    }

    @Override
    protected void handleServiceVersionMismatch(Version oldVersion,
            Version currentVersion) {
        throw new IllegalStateException(
                "unable to convert version:" + oldVersion +
                " to current version:" + currentVersion);
    }

    public void openChangesFile(EventRecorder eventRecorder, String filename) {
        logger.getLogger().info("Filename: " + filename);
        KernelRunnable task = new OpenChangesFileTask(eventRecorder.getName(), filename);
        taskScheduler.scheduleTask(task, taskOwner);
    }

    public void recordMessage(EventRecorder eventRecorder, WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        logger.getLogger().info("sender: " + sender + ", clientID: " + clientID + ", message: " + message);
        KernelRunnable task = new RecordMessageTask(eventRecorder.getName(), sender, clientID, message);
        taskScheduler.scheduleTask(task, taskOwner);
    }

    public void stopRecording(EventRecorder eventRecorder) {
        logger.getLogger().info("Event Recorder: " + eventRecorder);
        stopRecording(eventRecorder.getName());
    }

    public void stopRecording(String recorderName) {
        logger.getLogger().info("Event Recorder name: " + recorderName);
        KernelRunnable task = new StopRecordingTask(recorderName);
        taskScheduler.scheduleTask(task, taskOwner);
    }

    private class OpenChangesFileTask implements KernelRunnable {

        private final String recorderName;
        private final String changesFilename;

        OpenChangesFileTask(String recorderName, String filename) {
            this.recorderName = recorderName;
            changesFilename = filename;
        }

        public String getBaseTaskType() {
            return "EventRecordingService.OpenChangesFileTask";
        }

        public void run() throws Exception {
            PrintWriter changesWriter = new PrintWriter(new FileOutputStream(changesFilename), true);
            logger.getLogger().info("recorderTable: " + recorderTable);
            recorderTable.put(recorderName, changesWriter);
            changesWriter.println("<?xml version=\"1.0\" encoding=\"" + ENCODING + "\"?>");
            changesWriter.println("<Wonderland_Recorder>");
            changesWriter.println("<Wonderland_Changes>");
            timeOfLastChange = new Date().getTime();
        }
    }

    private class RecordMessageTask implements KernelRunnable {

        private final WonderlandClientSender sender;
        private final WonderlandClientID clientID;
        private final CellMessage message;
        private PrintWriter changesWriter;
        private CellID cellID;
        private CellMO recordedCell;

        RecordMessageTask(String recorderName, WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {

            this.sender = sender;
            this.clientID = clientID;
            this.message = message;
            cellID = message.getCellID();
            recordedCell = CellManagerMO.getCell(cellID);
            logger.getLogger().info("recorderTable: " + recorderTable);

            changesWriter = recorderTable.get(recorderName);

        }

        public String getBaseTaskType() {
            return "EventRecordingService.RecordMessageTask";
        }

        public void run() throws Exception {

            writeChange();
        }

        public void writeChange() {
            changesWriter.print("<Message ");
            if (recordedCell != null) {
                changesWriter.print("cellId=\"" + cellID + "\" ");
            }
            long delay;
            long now = new Date().getTime();
            delay = now - timeOfLastChange;
            timeOfLastChange = now;
            changesWriter.print("delay=\"" + delay + "\" ");
            changesWriter.println(">");
            writeMessage();
            changesWriter.println("</Message>");
        }

        private void writeMessage() {
            try {
                ByteBuffer byteBuffer = MessagePacker.pack(message, clientID.getID().shortValue());
                changesWriter.println(BASE_64_ENCODER.encode(byteBuffer));
            } catch (PackerException ex) {
                logger.getLogger().log(Level.SEVERE, null, ex);
            }
        }
    }

    private class StopRecordingTask implements KernelRunnable {
        private String recorderName;

        StopRecordingTask(String recorderName) {
            logger.getLogger().info("EventRecorder name: " + recorderName);
            logger.getLogger().info("recorderTable: " + recorderTable);
            this.recorderName = recorderName;
        }

        public String getBaseTaskType() {
            return "EventRecordingService.StopRecordingTask";
        }

        public void run() throws Exception {

            PrintWriter changesWriter = recorderTable.get(recorderName);
            logger.getLogger().info("ChangesWriter: " + changesWriter);
            changesWriter.println("</Wonderland_Changes>");
            changesWriter.println("</Wonderland_Recorder>");
            changesWriter.close();
            recorderTable.remove(recorderName);
        }
    }
}
