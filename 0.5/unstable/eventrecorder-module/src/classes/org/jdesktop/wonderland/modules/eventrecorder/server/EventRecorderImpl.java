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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.eventrecorder.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.util.ScalableHashSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.wfs.WorldRoot;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.ChangesFileCloseListener;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.LoadedCellRecordingListener;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.MessageRecordingResult;
import org.jdesktop.wonderland.server.eventrecorder.EventRecorder;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.MessageRecordingListener;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.UnloadedCellsRecordingListener;
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
        CellExportListener, ChangesFileCloseListener, MessageRecordingListener, LoadedCellRecordingListener, UnloadedCellsRecordingListener,Serializable {

    private static final Logger logger = Logger.getLogger(EventRecorderImpl.class.getName());
    /*The reference to the cell that is the event recorder in the world */
    private CellID eventRecorderCellID = null;
    /*The name of this recorder*/
    //TODO: is this necessary?
    private String recorderName;
    /*The name of the tape to which the recording is being made
     * This also provides the name of the directory into which the files are placed
     * */
    private String tapeName;
    /* A binding that is used to reference a set of cells that for which we failed to record the initiall state
     * At present this includes all avatars
     * If we get a message for one of these cells, we ignore it
     * */
    private static final String FAILED_CELLS_BINDING = "FAILED_CELLS";

    /** Creates a new instance of EventRecorderImpl
     * @param originCell the cell that is the event recorder
     * @param name the name of the event recorder
     */
    public EventRecorderImpl(CellMO originCell, String name) {
        eventRecorderCellID = originCell.getCellID();
        ScalableHashSet<CellID> failedCells = new ScalableHashSet<CellID>();
        AppContext.getDataManager().setBinding(FAILED_CELLS_BINDING, failedCells);
        this.recorderName = name;
    }

    /**
     * Register this event recorder with the recorder manager.
     * Once it's registered it will receive recordMessage() method calls
     */
    public void register() {
        //logger.fine("registering with recorder manager");
        RecorderManager.getDefaultManager().register(this);
    }

    /**
     * Unregister from the recorder manager, to avoid receiving any more messages
     * to be recorded
     */
    public void unregister() {
        //logger.fine("unregistering with recorder manager");
        RecorderManager.getDefaultManager().unregister(this);
    }

    private ScalableHashSet getFailedCells() {
        return (ScalableHashSet) AppContext.getDataManager().getBinding(FAILED_CELLS_BINDING);
    }

    public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        //logger.fine("sender: " + sender + ", " + clientID + ", " + message);
        CellID cellID = message.getCellID();
        //TODO: check if cellID is a cell that's within the bounds of the recorder's recording volume
        if (getFailedCells().contains(cellID)) {
            logger.warning("Ignoring message for cellID: " + cellID);
            return;
        }
        if (cellID.equals(eventRecorderCellID)) {
            logger.warning("Ignoring message for the event recorder cell");
            return;
        }
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        mgr.recordMessage(tapeName, clientID, message, this);
    }
    
    void recordLoadedCell(CellID cellID) {
        //Ensure that we don't record the event recorder cell
        if (cellID == eventRecorderCellID) {
            logger.warning("Not recording the load of my own cellMO");
            return;
        }
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        //Callback is via recordLoadedCellResult()
        mgr.recordLoadedCell(tapeName, cellID, this);
        
    }

    void recordUnloadedCell(CellID cellID) {
        //TODO What to do if we unload the cell that's recording??!!
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        //Callback is via recordUnloadedCellResult()
        mgr.recordUnloadedCell(tapeName, cellID, this);
    }



    public String getName() {
        return recorderName;
    }

    /**
     * Start recording to the tape given in the parameter
     * @param tapeName the name of the selected tape in the event recorder
     * @param rootcells the cells currently loaded and visible in the viewcellcache of the recorder
     */
    void startRecording(String tapeName, Set<CellID> rootCells) {
        logger.info("start recording to: " + tapeName);
        this.tapeName = tapeName;
        //Record the state of the current cells
        //this rest of the procedure happens in recordingCreated
        Set<CellID> recordedCells = new HashSet<CellID>();
        recordedCells.addAll(rootCells);
        //Remove the event recorder cell, we don't want it to be recorded
        recordedCells.remove(eventRecorderCellID);
        recordCells(recordedCells);
    }

    /**
     * Stop the recording
     */
    void stopRecording() {
        //Stop receiving messages to record
        unregister();
        //Close the file containing the recorded messages
        //We're notified if the file is successfully closed in fileClosed()
        //If the file closure fails, we're notified by a call to fileClosurefailed()
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        mgr.closeChangesFile(tapeName, this);
        
    }

    public void fileClosed() {
        logger.info("Changes file successfully closed");
        tapeName = null;
    }

    public void fileClosureFailed(String reason, Throwable cause) {
        //There has been a problem closing the changes file
        //Log the error and terminate
        logger.log(Level.SEVERE, reason, cause);
        tapeName = null;
    }

    private void createChangesFile() {
        logger.info("opening changes file");
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        //Open the file for recording changes
        //on success the EventRecorderCellMO receives a call to fileCreated()
        //if this fails, it is informed by a call to fileCreationFailed()
        EventRecorderCellMO cellMO = (EventRecorderCellMO) CellManagerMO.getCell(eventRecorderCellID);
        mgr.createChangesFile(tapeName, cellMO);
    }

    /**
     * Export a set of cells in the current world to a recording with the tape
     * name.  
     * @param cells the set of cells to record
     */
    private void recordCells(Set<CellID> cells) {
        // get the export service
        CellExportManager em = AppContext.getManager(CellExportManager.class);

        // Create a new recording.  The remainder of the export procedure will happen
        // in the recordingCreated() method of the listener
        // Or if it fails, in the recordingFailed() method
        em.createRecording(tapeName, cells, this);
    }

    public void recordingCreated(WorldRoot worldRoot, Set<CellID> cells) {
        //The new recording has been created, but the cells have not yet been exported
        // export the cells
        // remainder of procedure is in recordLoadedCellsResult
        if (cells.isEmpty()) {
            logger.warning("no cells to export");
        } 
        CellExportManager em = AppContext.getManager(CellExportManager.class);
        em.exportCells(worldRoot, cells, this, true);
        
    }

    public void recordingFailed(String reason, Throwable cause) {
        //There has been a problem creating the recording
        //Log the error and terminate
        logger.log(Level.SEVERE, "Error creating recording: " + reason, cause);
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
                //logger.warning("Adding to failed cells");
                getFailedCells().add(id);
            }
        }
        if (!results.isEmpty() && (successCount == 0)) {
            logger.severe("Failed to export any cells to the recording, terminating recording");
            return;
        }
        //We've succeeded in exporting the cells (if there were any), so create the changes file to record the messages
        createChangesFile();
    }

    public boolean isRecording() {
        if (eventRecorderCellID == null) {
            return false;
        }
        EventRecorderCellMO cellMO = (EventRecorderCellMO) CellManagerMO.getCell(eventRecorderCellID);
        if (cellMO == null) {
            return false;
        }
        return cellMO.isRecording();

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
            //logger.info("Success writing message " + id);
        }
    }

    public void recordLoadedCellResult(CellID cellID, Exception exception) {
        if (exception != null) {
            logger.log(Level.SEVERE, "Failed to record loaded cell " + CellManagerMO.getCell(cellID) + " id: " + cellID, exception);
            getFailedCells().add(cellID);
        } else {
            logger.info("recorded loadCell: " + cellID);
        }
    }

    public void recordUnloadedCellResult(CellID cellID, Exception exception) {
        if (exception != null) {
            logger.log(Level.SEVERE, "Failed to record unloaded cell  id: " + cellID , exception);
        } else {
            logger.info("recorded unloadCell: " + cellID);
        }
    }
}
