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
package org.jdesktop.wonderland.modules.eventplayer.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.modules.eventplayer.server.EventPlayingManager.MessagePlayingResult;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.RecordingLoaderUtils.CellImportEntry;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.modules.eventplayer.server.EventPlayingManager.MessagePlayingListener;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.CellImportManager;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.CellImportManager.RecordingLoadingListener;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellMOFactory;
import org.jdesktop.wonderland.server.wfs.importer.CellMap;

/**
 * An implementation of an event recorder that records the initial state of the cells as WFS and then
 * subsequent messages
 * @author Bernard Horan
 */
public class EventPlayerImpl implements ManagedObject, RecordingLoadingListener, MessagePlayingListener, Serializable {

    private static final Logger logger = Logger.getLogger(EventPlayerImpl.class.getName());
    /*The reference to the cell that is the event recorder in the world */
    private ManagedReference<CellMO> cellRef;
    /*is this recorder actually recording*/
    private boolean isPlaying = false;
    /*The name of this recorder*/
    //TODO: is this necessary?
    private String playerName;
    /*The name of the tape to which the recording is being made
     * This also provides the name of the directory into which the files are placed
     * */
    private String tapeName;

    private Map<CellID, CellID> cellMap = new HashMap<CellID, CellID>();

    /** Creates a new instance of EventRecorderImpl
     * @param originCell the cell that is the event recorder
     * @param name the name of the event recorder
     */
    public EventPlayerImpl(CellMO originCell, String name) {
        cellRef = AppContext.getDataManager().createReference(originCell);
        this.playerName = name;
    }

    

    public void playMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        logger.fine("sender: " + sender + ", " + clientID + ", " + message);
        CellID cellID = message.getCellID();
        //TODO: check if cellID is a cell that's within the bounds of the recorder's recording volume
        EventPlayingManager mgr = AppContext.getManager(EventPlayingManager.class);
        mgr.playMessage(tapeName, clientID, message, this);
    }

    public String getName() {
        return playerName;
    }

    /**
     * Start recording to the tape given in the parameter
     * @param tapeName the name of the selected tape in the event recorder
     */
    void startPlaying(String tapeName) {
        logger.info("start playing: " + tapeName);
        this.tapeName = tapeName;
        //Load the cells labelled by tape name
        //then replay messages
        loadRecording();
        replayMessages();
    }

    private void loadRecording() {
        // get the export service
        CellImportManager im = AppContext.getManager(CellImportManager.class);

        // first, create a new recording.  The remainder of the export procedure will happen
        // in the recordingLoaded() method of the listener
        im.loadRecording(tapeName, this);
    }

    /**
     * Stop the recording
     */
    public void stopPlaying() {
        logger.info("stop playing, isPlaying: " + isPlaying);
        logger.info("children: " + cellRef.get().getNumChildren());
        Collection<ManagedReference<CellMO>> children = cellRef.get().getAllChildrenRefs();
        for (ManagedReference<CellMO> childRef : children) {
            logger.info(childRef.get().toString());
        }
        if (!isPlaying) {
            logger.warning("Attempt to stop playing when not already playing");
            return;
        }
        isPlaying = false;
        //Stop the messages being played
        EventPlayingManager mgr = AppContext.getManager(EventPlayingManager.class);
        //mgr.closeChangesFile(tapeName, this);
        tapeName = null;
    }


    @Override
    public String toString() {
        return super.toString() + " name: " + getName();
    }

    public void messagePlayingResult(MessagePlayingResult result) {
        //message has been written, or not
        MessageID id = result.getMessageID();
        if (!result.isSuccess()) {
            logger.log(Level.WARNING, "Error writing message " + id + ": " +
                           result.getFailureReason(), result.getFailureCause());
        } else {
            logger.info("Success writing message " + id);
        }
    }

    public void fileOpened(ChangesFile changesFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void fileOpeningFailed(String reason, Throwable cause) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void recordingLoadingFailed(String reason, Throwable cause) {
        logger.log(Level.SEVERE, reason, cause);
    }

    public void recordingLoaded(CellMap<CellImportEntry> cellImportMap) {
        logger.info("ENTERING RECORDINGLOADED with cellMap: " + cellImportMap);
        new Exception().printStackTrace(System.err);
        CellMap<ManagedReference<CellMO>> cellMOMap = new CellMap();
        Set<String> keys = cellImportMap.keySet();
        for (String key : keys) {
            CellImportEntry entry = cellImportMap.get(key);
            logger.info("processing child " + entry.getName());

            ManagedReference<CellMO> parentRef = cellMOMap.get(entry.getRelativePath());

            CellServerState setup = entry.getServerState();
            //logger.info(setup.toString());
            

            /*
             * If the cell is at the root, then the relative path will be "/"
             * and we do not want to prepend it to the cell path.
             */
            String cellPath = entry.getRelativePath() + "/" + entry.getName();
            if (entry.getRelativePath().compareTo("") == 0) {
                cellPath = entry.getName();
            }

            /*
             * Create the cell and pass it the setup information
             */
            String className = setup.getServerClassName();
            //logger.info("className: " + className);
            CellMO cellMO = CellMOFactory.loadCellMO(className);
            //logger.info("created cellMO: " + cellMO);
            if (cellMO == null) {
                /* Log a warning and move onto the next cell */
                logger.warning("Unable to load cell MO: " + className);
                continue;
            }

            /* Set the cell name */
            cellMO.setName(entry.getName());
            //logger.info("set name: " + entry.getName());

            /** XXX TODO: add an import details cell component XXX */

            /* Call the cell's setup method */
            try {
                cellMO.setServerState(setup);
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, "Error setting up new cell " +
                        cellMO.getName() + " of type " +
                        cellMO.getClass(), cce);
                continue;
            }

            /*
             * Add the child to the cell hierarchy. If the cell has no parent,
             * then we insert it directly into the world
             */
            try {
                if (parentRef == null) {
                    //logger.info("parentRef == null");
                    /*
                    if (cellRef != null) {
                        CellMO parent = cellRef.get();
                        logger.info("parent: " + parent);
                        parent.addChild(cellMO);
                        logger.info("WFSLoader: Parent Cell ID=" + cellMO.getParent().getCellID().toString());
                        Collection<ManagedReference<CellMO>> refs = cellMO.getParent().getAllChildrenRefs();
                        Iterator<ManagedReference<CellMO>> it = refs.iterator();
                        while (it.hasNext() == true) {
                            logger.info("WFSLoader: Child Cell=" + it.next().get().getCellID().toString());
                        }
                        logger.info("WFSLoader: Cell Live: " + cellMO.isLive());
                    } else {*/
                        WonderlandContext.getCellManager().insertCellInWorld(cellMO);
                    //}
                }
                else {
                    logger.info("WFSLoader: Adding child (ID=" + cellMO.getCellID().toString() +
                            ") to parent (ID=" + parentRef.get().getCellID().toString() + ")");
                    parentRef.get().addChild(cellMO);
                    logger.info("WFSLoader: Parent Cell ID=" + cellMO.getParent().getCellID().toString());
                    Collection<ManagedReference<CellMO>> refs = cellMO.getParent().getAllChildrenRefs();
                    Iterator<ManagedReference<CellMO>> it = refs.iterator();
                    while (it.hasNext() == true) {
                        logger.info("WFSLoader: Child Cell=" + it.next().get().getCellID().toString());
                    }
                    logger.info("WFSLoader: Cell Live: " + cellMO.isLive());
                }
            } catch (MultipleParentException excp) {
                logger.log(Level.WARNING, "Attempting to add a new cell with " +
                        "multiple parents: " + cellMO.getName());
                continue;
            }

            /*
             * Since we are loading cells for the first time, we put the cell
             * in both the cell object and last modified reference map. We
             * add the cell to its parent. If the parent is null, we add to the
             * root.
             */
            ManagedReference<CellMO> cellMORef = AppContext.getDataManager().createReference(cellMO);
            cellMOMap.put(cellPath, cellMORef);

            String idValue = setup.getMetaData().get("CellID");
            //logger.info("Old cellID value: " + idValue);
            long id = Long.valueOf(idValue);
            //logger.info("Old cellID id: " + id);
            CellID oldCellID = new CellID(id);
            //logger.info("Old cellID: " + oldCellID);
            CellID newCellID = cellMO.getCellID();
            //logger.info("New cellID: " + newCellID);
            cellMap.put(oldCellID, newCellID);
            //logger.info("new cellID from map: " + cellMap.get(oldCellID));

            
        }
        logger.info("COMPLETE");
    }

    private void replayMessages() {
        //loadMessageFile();
        //replayMessageFile();
    }
}
