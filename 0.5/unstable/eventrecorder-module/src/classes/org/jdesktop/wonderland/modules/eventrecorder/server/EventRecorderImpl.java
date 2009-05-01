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

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.modules.eventrecorder.server.ChangesFile;
import org.jdesktop.wonderland.common.wfs.WorldRoot;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.ChangesFileCloseListener;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.MessageRecordingResult;
import org.jdesktop.wonderland.server.eventrecorder.EventRecorder;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.ChangesFileCreationListener;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.MessageRecordingListener;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager.CellExportListener;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager.CellExportResult;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager.RecordingCreationListener;

/**
 * An implementation of an event recorder that records the initial state of the cells as WFS and then
 * subsequent messages
 * @author Bernard Horan
 */
public class EventRecorderImpl implements ManagedObject, EventRecorder, RecordingCreationListener,
        CellExportListener, ChangesFileCreationListener, ChangesFileCloseListener, MessageRecordingListener, Serializable {

    private static final Logger logger = Logger.getLogger(EventRecorderImpl.class.getName());
    /*The reference to the cell that is the event recorder in the world */
    private ManagedReference<CellMO> cellRef;
    /*is this recorder actually recording*/
    private boolean isRecording = false;
    /*The name of this recorder*/
    //TODO: is this necessary?
    private String recorderName;
    /*The name of the tape to which the recording is being made
     * This also provides the name of the directory into which the files are placed
     * */
    private String tapeName;
    /* A set of cells that for which we failed to record the initiall state
     * At present this includes all avatars
     * If we get a message for one of these cells, we ignore it
     * */
    private Set<CellID> failedCells = new HashSet<CellID>();

    /** Creates a new instance of EventRecorderImpl
     * @param originCell the cell that is the event recorder
     * @param name the name of the event recorder
     */
    public EventRecorderImpl(CellMO originCell, String name) {
        cellRef = AppContext.getDataManager().createReference(originCell);
        this.recorderName = name;
    }

    /**
     * Register this event recorder with the recorder manager.
     * Once it's registered it will receive recordMessage() method calls
     */
    public void register() {
        logger.fine("registering with recorder manager");
        if (isRecording) {
            throw new RuntimeException("Can't register this EventRecorder when recording");
        }
        RecorderManager.getDefaultManager().register(this);
    }

    /**
     * Unregister from the recorder manager, to avoid receiving any more messages
     * to be recorded
     */
    public void unregister() {
        logger.fine("unregistering with recorder manager");
        if (isRecording) {
            throw new RuntimeException("Can't unregister this EventRecorder when recording");
        }
        RecorderManager.getDefaultManager().unregister(this);
    }

    public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        logger.fine("sender: " + sender + ", " + clientID + ", " + message);
        CellID cellID = message.getCellID();
        //TODO: check if cellID is a cell that's within the bounds of the recorder's recording volume
        if (failedCells.contains(cellID)) {
            logger.warning("Ignoring message for cellID: " + cellID);
            return;
        }
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        mgr.recordMessage(tapeName, clientID, message, this);
    }

    public String getName() {
        return recorderName;
    }

    /**
     * Start recording to the tape given in the parameter
     * @param tapeName the name of the selected tape in the event recorder
     */
    void startRecording(String tapeName) {
        logger.info("start recording to: " + tapeName);
        this.tapeName = tapeName;
        //Record the state of the current cells
        //this rest of the procedure happens in recordingCreated
        //Here we should find the cells that are within the range of the originCell
        Set<CellID> rootCells = CellManagerMO.getCellManager().getRootCells();
        Set<CellID> recordedCells = new HashSet<CellID>();
        recordedCells.addAll(rootCells);
        recordCells(recordedCells);
    }

    /**
     * Stop the recording
     */
    public void stopRecording() {
        logger.info("stop recording, isRecording: " + isRecording);
        if (!isRecording) {
            logger.warning("Attempt to stop recording when not already recording");
            return;
        }
        isRecording = false;
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        mgr.closeChangesFile(tapeName, this);
        unregister();
        tapeName = null;
    }

    public void fileClosed(ChangesFile cFile) {
        logger.info("Changes file successfully closed");
    }

    public void fileClosureFailed(String reason, Throwable cause) {
        logger.log(Level.SEVERE, reason, cause);
    }

    private void createChangesFile() {
        logger.fine("opening changes file");
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        //Open the file for recording changes
        //this rest of the procedure happens in fileCreated
        mgr.createChangesFile(tapeName, this);
    }

    public void fileCreated(ChangesFile cFile) {
        logger.info("Changes file created, so start recording");
        isRecording = true;
        logger.info("isRecording: " + isRecording);
    }

    public void fileCreationFailed(String reason, Throwable cause) {
        logger.log(Level.SEVERE, reason, cause);
    }

    /**
     * Export a set of cells in the current world to a recording with the tape
     * name.  
     * @param cells the set of cells to record
     */
    private void recordCells(Set<CellID> cells) {
        // get the export service
        CellExportManager em = AppContext.getManager(CellExportManager.class);

        // first, create a new recording.  The remainder of the export procedure will happen
        // in the recordingCreated() method of the listener
        em.createRecording(tapeName, cells, this);
    }

    public void recordingCreated(WorldRoot worldRoot, Set<CellID> cells) {
        //The new recording has been created, but the cells have not yet been exported
        //Register the eventRecorder in the hope that the export succeeds
        register();

        // export the cells
        // remainder of procedure is in exportResult
        CellExportManager em = AppContext.getManager(CellExportManager.class);
        em.exportCells(worldRoot, cells, this, true);
    }

    public void recordingFailed(String reason, Throwable cause) {
        logger.log(Level.WARNING, "Error creating recording: " + reason, cause);
    }

    public void exportResult(Map<CellID, CellExportResult> results) {
        //cells have been exported
        int successCount = 0;
        int errorCount = 0;

        for (Map.Entry<CellID, CellExportResult> e : results.entrySet()) {
            CellID id = e.getKey();
            CellExportResult res = e.getValue();
            if (res.isSuccess()) {
                successCount++;
            } else {
                errorCount++;
                logger.log(Level.WARNING, "Error exporting " + id + " " + CellManagerMO.getCell(id) + " : " +
                        res.getFailureReason(), res.getFailureCause());
                logger.warning("Adding to failed cells");
                failedCells.add(id);
                logger.info("failed cells: " + failedCells);
            }
        }

        createChangesFile();
    }

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public String toString() {
        return super.toString() + " name: " + getName();
    }

    public void messageRecordingResult(MessageRecordingResult result) {
        //message has been written, or not
        MessageID id = result.getMessageID();
        if (!result.isSuccess()) {
            logger.log(Level.WARNING, "Error writing message " + id + ": " +
                           result.getFailureReason(), result.getFailureCause());
        } else {
            logger.info("Success writing message " + id);
        }
    }
}
