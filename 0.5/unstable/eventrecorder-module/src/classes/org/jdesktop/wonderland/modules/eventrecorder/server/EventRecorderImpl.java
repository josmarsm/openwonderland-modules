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
import com.sun.sgs.app.ManagedReference;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.wfs.WorldRoot;
import org.jdesktop.wonderland.server.eventrecorder.EventRecorder;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager.CellExportListener;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager.CellExportResult;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager.RecordingCreationListener;

/**
 *
 * @author Bernard Horan
 */
public class EventRecorderImpl implements EventRecorder, RecordingCreationListener, CellExportListener, Serializable {
    private static final Logger logger = Logger.getLogger(EventRecorderImpl.class.getName());
    private ManagedReference<CellMO> cellRef;
    private boolean isRecording = false;
    private String recorderName;
    private String tapeName;
    private Set<CellID> failedCells = new HashSet<CellID>();
    
    /** Creates a new instance of EventRecorderImpl
     * @param originCell
     * @param name
     */
    public EventRecorderImpl(CellMO originCell, String name) {
        cellRef = AppContext.getDataManager().createReference(originCell);
        this.recorderName = name;
    }

    public void register() {
        logger.fine("registering with recorder manager");
        if (isRecording) {
            throw new RuntimeException("Can't register this EventRecorder when recording");
        }
        RecorderManager.getDefaultManager().register(this);
    }

    public void unregister() {
        logger.fine("unregistering with recorder manager");
        if (isRecording) {
            throw new RuntimeException("Can't unregister this EventRecorder when recording");
        }
        RecorderManager.getDefaultManager().unregister(this);
    }

    public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        logger.fine("sender: " + sender + ", " + clientID + ", " + message);
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        mgr.recordMessage(this, sender, clientID, message);      
    }

    public String getName() {
        return recorderName;
    }
    

    void startRecording(String tapeName) {
        logger.info("start recording to: " + tapeName);
        this.tapeName = tapeName;
        //Record the state of the current cells
        //this rest of the procedure happens in recordingCreated
        Set<CellID> rootCells = CellManagerMO.getCellManager().getRootCells();
        Set<CellID> recordedCells = new HashSet<CellID>();
        recordedCells.addAll(rootCells);
        recordCells(recordedCells);
        
    }

    public void stopRecording() {
        logger.info("stop recording");
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        mgr.stopRecording(this);
        isRecording = false;
        unregister();
        tapeName = null;
    }    

    
    
    

    private void openChangesFile(String changesFilename) throws FileNotFoundException {
        logger.fine("opening changes file: " + changesFilename);
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        mgr.openChangesFile(this, changesFilename);
    } 
    

    /**
     * Export a set of cells in the current world to a recording with the given
     * name.  Use null to create a recording with a default name
     * @param name the name or null
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

        logger.info("rootPath: " + worldRoot.getRootPath());


        // export the cells
        // remainder of procedure is in exportResult
        CellExportManager em = AppContext.getManager(CellExportManager.class);
        em.exportCells(worldRoot, cells, this);
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
                logger.log(Level.WARNING, "Error exporting " + id + " " + CellManagerMO.getCell(id)+ " : " +
                           res.getFailureReason(), res.getFailureCause());
                logger.warning("Added to failed cells");
                failedCells.add(id);
            }
        }

        //logger.warning("Exported " + successCount + " cells.  " + errorCount + " errors detected.");
        
        //2. open the file to record changes
        //3. set recording to true

//        try {
//            openChangesFile(tapeName+"-changes.xml");
//            isRecording = true;
//        } catch (FileNotFoundException ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public String toString() {
        return super.toString() + " name: " + getName();
    }
    
}
