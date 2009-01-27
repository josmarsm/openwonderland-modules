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
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.server.eventrecorder.EventRecorder;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.eventrecorder.EventRecordingManager;

/**
 *
 * @author Bernard Horan
 */
public class EventRecorderImpl implements EventRecorder, Serializable {
    private static final Logger logger = Logger.getLogger(EventRecorderImpl.class.getName());
    private ManagedReference<CellMO> cellRef;
    private boolean isRecording = false;
    private String name;
    private transient PrintWriter syncWriter;
    final private static String ENCODING = "ISO-8859-1";   
    
    /** Creates a new instance of EventRecorderImpl
     * @param originCell
     */
    public EventRecorderImpl(CellMO originCell, String name) {
        cellRef = AppContext.getDataManager().createReference(originCell);
        this.name = name;
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
        return name;
    }
    

    void startRecording(String pathName) {
        logger.info("start recording to: " + pathName);
        //1. Record the sync state
        //2. open the file to record changes
        //3. set recording to true
        try {
            openSyncFile(pathName+"sync");
            synchronize();
            endSync();
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        register();
        try {
            openChangesFile(pathName);
            isRecording = true;
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void stopRecording() {
        logger.info("stop recording");
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        mgr.stopRecording(this);
        isRecording = false;
        unregister();
    }    

    
    public void endSync() {
        logger.fine("end sync");
        syncWriter.println("</Wonderland_Sync>");
        syncWriter.println("</Wonderland_Recorder>");
        closeSyncFile();
    }

    private void closeSyncFile() {
        logger.fine("closing sync file");
        syncWriter.close();
    }
    

    private void openChangesFile(String changesFilename) throws FileNotFoundException {
        logger.fine("opening changes file: " + changesFilename);
        EventRecordingManager mgr = AppContext.getManager(EventRecordingManager.class);
        mgr.openChangesFile(this, changesFilename);
    } 
    
    private void openSyncFile(String syncFilename) throws FileNotFoundException {
        logger.fine("opening sync file: " + syncFilename);
        syncWriter = new PrintWriter(new FileOutputStream(syncFilename), true);
        syncWriter.println("<?xml version=\"1.0\" encoding=\""+ENCODING+"\"?>");
        syncWriter.println("<Wonderland_Recorder>");
        syncWriter.println("<Wonderland_Sync>");
    }

    private void synchronize() {
        logger.info("Recording initial state");
        Set<CellID> rootCells = CellManagerMO.getCellManager().getRootCells();
        for (CellID cellID : rootCells) {
            CellMO rootCell = CellManagerMO.getCell(cellID);
            synchronizeCell(rootCell);
        }
        logger.info("Finished recording initial state");
    }

    private void synchronizeCell(CellMO cell) {
        //logger.info("cell: " + cell);
        CellServerState serverState = cell.getServerState(null);
        //logger.info("serverState: " + serverState);
        syncWriter.println("<Cell cellID=\"" + cell.getCellID() + "\" class=\"" + cell.getClass().getCanonicalName() + "\">");
        if (serverState != null) {
            try {
                serverState.encode(syncWriter);
                
            } catch (JAXBException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        Collection<ManagedReference<CellMO>> children = cell.getAllChildrenRefs();
        if (children.size() > 0) {
            syncWriter.println("<Children>");
            for (ManagedReference<CellMO> managedReference : children) {
                CellMO child = managedReference.get();
                synchronizeCell(child);
            }
            syncWriter.println("</Children>");
        }
        syncWriter.println("</Cell>");
    }
   
    

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public String toString() {
        return super.toString() + " name: " + getName();
    }
    
}
