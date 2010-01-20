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
package org.jdesktop.wonderland.modules.eventplayer.server;

import com.jme.math.Vector3f;
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
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableAvatarMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.messages.MessagePacker.ReceivedMessage;
import org.jdesktop.wonderland.modules.eventplayer.common.npcplayer.NpcPlayerCellServerState;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.RecordingLoaderUtils.CellImportEntry;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.modules.eventplayer.server.EventPlayingManager.ChangeReplayingListener;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.CellImportManager;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.CellImportManager.CellRetrievalListener;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.CellImportManager.RecordingLoadedListener;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellMOFactory;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.common.cell.state.AvatarCellServerState;
import org.jdesktop.wonderland.modules.eventplayer.server.npcplayer.NpcPlayerCellMO;
import org.jdesktop.wonderland.server.cell.view.AvatarCellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.wfs.importer.CellMap;

/**
 * An implementation of an event player that loads a recorded state of a world and replays messages to that world.
 * @author Bernard Horan
 */
public class EventPlayer implements ManagedObject, RecordingLoadedListener, CellRetrievalListener, ChangeReplayingListener, Serializable {

    private static final Logger logger = Logger.getLogger(EventPlayer.class.getName());
    /*The name of the tape representing the recording
     * This also provides the name of the directory into which the files are contained
     * */
    private String tapeName;
    /*a map from old cell id to new cell id.
     * this enables me to direct replayed messages at the appropriate cell
     */
    private Map<CellID, CellID> cellMap = new HashMap<CellID, CellID>();
    
    /*The wonderland architecture requires that all messages originate from a wonderlandclientid
     * For playback I use a fudged id.
     */
    private WonderlandClientID clientID;

    /*The wonderland architecture requires that all messages originate from a wonderlandclientsender
     * For playback I use a fudged sender.
     */
    private WonderlandClientSender clientSender;
    /*
     *the reference fo the event player cell
     */
    private ManagedReference<EventPlayerCellMO> playerCellMORef;
    /*
     * The original position of the event recorder
     */
    private PositionComponentServerState originalRecorderPosition;
    


    /** Creates a new instance of EventRecorderImpl
     * @param playerCell the cell that is the event player
     */
    public EventPlayer(EventPlayerCellMO playerCell) {
        playerCellMORef = AppContext.getDataManager().createReference(playerCell);
        clientID = new PlayerClientID();
        clientSender = new PlayerClientSender();
    }

    /**
     * Play the message
     * @param rMessage a message that has been parsed from a recording of events
     */
    public void playMessage(ReceivedMessage rMessage) {
        CellMessage message = (CellMessage) rMessage.getMessage();
        //logger.info("message: " + message);
        //logger.info("cellmap: " + cellMap);
        CellID oldCellID = message.getCellID();
        //logger.info("oldCellID: " + oldCellID);
        CellID newCellID = cellMap.get(oldCellID);
        //logger.info("newCellID: " + newCellID);
        message.setCellID(newCellID);
        CellMO targetCell = CellManagerMO.getCell(newCellID);
        if (targetCell == null) {
            logger.severe("Could not find cell for ID: " + newCellID);
            return;
        }
        //TODO workaround for npc player cells that appear to ignore parent coordinates
        if (targetCell instanceof NpcPlayerCellMO) {
            logger.info("targetCell: " + targetCell);
            CellMO parentCell = targetCell.getParent();
            if (parentCell != null) {
                logger.info("parentCell: " + parentCell);
                if (message instanceof MovableAvatarMessage) {
                    Vector3f messageTranslation = ((MovableAvatarMessage) message).getTranslation();
                    logger.info("message translation: " + messageTranslation);
                    Vector3f worldTranslation = parentCell.getWorldTransform(null).getTranslation(null);
                    logger.info("world translation: " + worldTranslation);
                    messageTranslation.multLocal(worldTranslation);
                    logger.info("after multiplication: " + ((MovableAvatarMessage) message).getTranslation());
                }                
            }
        }
        //logger.info("targetCell: " + targetCell);
        ChannelComponentMO channel = targetCell.getComponent(ChannelComponentMO.class);
        //logger.info("channel: " + channel);
        if (channel == null) {
            throw new RuntimeException("No channel for " + targetCell);
        }
        try {
            channel.messageReceived(clientSender, clientID, message);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "failed to replay message: " + message, e);
        }
    }


    /**
     * unload the recording given in the parameter
     * stop the playback of a recording if it's already running
     * @param tapeName the name of the selected tape in the event player
     */
    void unloadRecording(String tapeName) {
        logger.info("unload recording");
        //clear the cellMap
        cellMap.clear();
        // get the event playing service
        EventPlayingManager epm = AppContext.getManager(EventPlayingManager.class);
        //Call the method on the service
        epm.unloadRecording(tapeName);
    }



    /**
     * Load the recording given in the parameter
     * @param tapeName the name of the selected tape in the event player
     */
    void loadRecording(String tapeName) {
        //logger.info("start loading: " + tapeName);
        this.tapeName = tapeName;
        //Load the cells labelled by tape name
        //then replay messages
        CellImportManager im = AppContext.getManager(CellImportManager.class);

        //First load the recording. The remainder of the import will be
        //via recordingLoaded()
        im.loadRecording(tapeName, this);

        
    }

    /**
     * Start recording to the tape given in the parameter
     * @param tapeName the name of the selected tape in the event player
     */
    void startPlaying() {
        logger.info("start playing");
        // get the event playing service
        EventPlayingManager epm = AppContext.getManager(EventPlayingManager.class);
        //Call the method on the service
        //Callbacks to this object via playMessage() and allChangesPlayed()
        epm.replayChanges(tapeName, this);
    }

    public void stopPlaying() {
        logger.info("stop playing");

        EventPlayingManager epm = AppContext.getManager(EventPlayingManager.class);
        //Call the method on the service
        //Callbacks to this object via playMessage() and allChangesPlayed()
        epm.pauseChanges(tapeName, this);
    }

    public void cellRetrievalFailed(String reason, Throwable cause) {
        logger.log(Level.SEVERE, reason, cause);
    }

    /**
     * The recording has been loaded
     * @param root the root of the recording
     * @param ex if non-null, the loading of the recording failed
     */
    public void recordingLoaded(RecordingRoot root, Exception ex) {
        if (ex != null) {
            logger.log(Level.SEVERE, "Failed to load recording", ex);
            return;
        }
        if (root == null) {
            logger.severe("Root is null!");
            return;
        }
        //logger.info("Root: " + root);
        //logger.info("Position Info: " + root.getPositionInfo());
        try {
            originalRecorderPosition = root.getPosition();
        } catch (JAXBException ex1) {
            logger.log(Level.SEVERE, "Failed to retrieve player position", ex1);
            return;
        }
        CellImportManager im = AppContext.getManager(CellImportManager.class);
        // first, create a new recording.  The remainder of the export procedure will happen
        // in the cellsRetrieved() method of the listener
        im.retrieveCells(tapeName, this);
    }

    public void cellsRetrieved(CellMap<CellImportEntry> cellRetrievalMap, CellMap<CellID> cellPathMap) {
        Set<String> keys = cellRetrievalMap.keySet();
        for (String key : keys) {
            CellImportEntry entry = cellRetrievalMap.get(key);
            //logger.info("processing child " + entry.getName());

            CellID parentID = cellPathMap.get(entry.getRelativePath());

            CellServerState setup = entry.getServerState();
            translateServerState(setup);
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
            if (className == null) {
                if (setup instanceof AvatarCellServerState) {
                    logger.info("Loading an avatar: " + setup);
                    
                    //Create a new setup to describe an NPC
                    setup = new NpcPlayerCellServerState((AvatarCellServerState)setup);
                    
                    //Get the classname of the new setup
                    className = setup.getServerClassName();
                } else {
                    /* Log a warning and move onto the next cell */
                    logger.warning("Unable to load cell MO: " + setup);
                    continue;
                }
            }
            //logger.info("className: " + className);
            CellMO cellMO = CellMOFactory.loadCellMO(className);
            //logger.info("created cellMO: " + cellMO);
            if (cellMO == null) {
                /* Log a warning and move onto the next cell */
                logger.warning("Unable to load cell MO: " + className);
                continue;
            }

            /* Set the cell name */
            //cellMO.setName(entry.getName());
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
             * then we insert it directly into the event recorder cell
             */
            try {
                if (parentID == null) {
                    logger.fine("parentID == null");
                    
                    if (playerCellMORef != null) {
                        EventPlayerCellMO parent = playerCellMORef.get();
                        logger.fine("parent: " + parent);
                        parent.addReplayedChild(cellMO);
                        logger.fine("[EventPlayer] Parent Cell ID=" + cellMO.getParent().getCellID().toString());
                        Collection<ManagedReference<CellMO>> refs = cellMO.getParent().getAllChildrenRefs();
                        Iterator<ManagedReference<CellMO>> it = refs.iterator();
                        while (it.hasNext() == true) {
                            logger.fine("[EventPlayer] Child Cell=" + it.next().get().getCellID().toString());
                        }
                        logger.fine("[EventPlayer] Cell Live: " + cellMO.isLive());
                    } else {
                        logger.severe("There shouldn't be a null ref to the player cell)");
                        
                        WonderlandContext.getCellManager().insertCellInWorld(cellMO);
                    }
                }
                else {
                    CellMO parentCellMO = CellManagerMO.getCell(parentID);
                    logger.info("EventPlayer: Adding child " + cellMO.getName() + " (ID=" + cellMO.getCellID().toString() +
                            ") to parent " + parentCellMO.getName() + " (ID=" + parentID.toString() + ")");
                    
                    parentCellMO.addChild(cellMO);
//                    logger.info("EventPlayerImpl: Children of parent Cell " + cellMO.getParent().getName() + " ID=" + cellMO.getParent().getCellID().toString());
//                    Collection<ManagedReference> refs = cellMO.getParent().getAllChildrenRefs();
//                    Iterator<ManagedReference<CellMO>> it = refs.iterator();
//                    while (it.hasNext() == true) {
//                        CellMO child = it.next().get();
//                        logger.info("EventPlayerImpl: Child Cell: " + child.getName() + " ID=" + child.getCellID());
//                    }
                    logger.info("EventPlayerImpl: Cell Live: " + cellMO.isLive());
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
            cellPathMap.put(cellPath, cellMO.getCellID());

            String idValue = setup.getMetaData().get("CellID");
            //logger.info("Old cellID value: " + idValue);
            long id = Long.valueOf(idValue);
            //logger.info("Old cellID id: " + id);
            CellID oldCellID = new CellID(id);
            //logger.info("Old cellID: " + oldCellID);
            if (cellMap.get(oldCellID) != null) {
                throw new RuntimeException("Failed trying to add new cellId to cellmap where cellID already exists");
            }
            CellID newCellID = cellMO.getCellID();
            //logger.info("New cellID: " + newCellID);
            cellMap.put(oldCellID, newCellID);
            //logger.info("new cellID from map: " + cellMap.get(oldCellID));

            
        }
        //logger.info("COMPLETE");
    }

    public void allCellsRetrieved() {
       logger.fine("All Cells Retrieved");
       playerCellMORef.get().allCellsRetrieved();
    }

    public void allChangesPlayed() {
        //logger.info("All Messages Played");
        playerCellMORef.get().playbackDone();
    }

    public void loadCell(CellServerState setup, CellID parentID) {
        translateServerState(setup);
        /*
         * Create the cell and pass it the setup information
         */
        String className = setup.getServerClassName();
        if (className == null) {
            if (setup instanceof AvatarCellServerState) {
                    logger.info("Loading an avatar: " + setup);

                    //Create a new setup to describe an NPC
                    setup = new NpcPlayerCellServerState((AvatarCellServerState)setup);

                    //Get the classname of the new setup
                    className = setup.getServerClassName();
                } else {
                    logger.severe("No cellMO class name from " + setup);
                    return;
                }
            
        }
        //logger.getLogger().info("className: " + className);
        CellMO cellMO = null;
        try {
            cellMO = CellMOFactory.loadCellMO(className);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load cell: " + className, e);
            return;
        }
        //logger.getLogger().info("created cellMO: " + cellMO);
        if (cellMO == null) {
            /* Log a warning and move onto the next cell */
            logger.severe("Unable to load cell MO: " + className);
            return;
        }
        /* Call the cell's setup method */
        try {
            cellMO.setServerState(setup);
        } catch (ClassCastException cce) {
            logger.log(Level.WARNING, "Error setting up new cell " +
                    cellMO.getName() + " of type " +
                    cellMO.getClass(), cce);
            return;
        }
        try {
            if (parentID != null) {
                //Need to add the cellMO to the right parent
                CellMO parent = CellManagerMO.getCell(parentID);
                logger.info("parent: " + parent);
                parent.addChild(cellMO);
                logger.info("[EventPlayer] Parent Cell ID=" + cellMO.getParent().getCellID().toString());
                Collection<ManagedReference<CellMO>> refs = cellMO.getParent().getAllChildrenRefs();
                Iterator<ManagedReference<CellMO>> it = refs.iterator();
                while (it.hasNext() == true) {
                    logger.info("[EventPlayer] Child Cell=" + it.next().get().getCellID().toString());
                }
                logger.info("[EventPlayer] Cell Live: " + cellMO.isLive());

            } else {
                //Add it to the player's cell
                EventPlayerCellMO parent = playerCellMORef.get();
                logger.info("parent: " + parent);
                parent.addReplayedChild(cellMO);
                logger.info("[EventPlayer] Parent Cell ID=" + cellMO.getParent().getCellID().toString());
                Collection<ManagedReference<CellMO>> refs = cellMO.getParent().getAllChildrenRefs();
                Iterator<ManagedReference<CellMO>> it = refs.iterator();
                while (it.hasNext() == true) {
                    logger.info("[EventPlayer] Child Cell=" + it.next().get().getCellID().toString());
                }
                logger.info("[EventPlayer] Cell Live: " + cellMO.isLive());
            }
        } catch (MultipleParentException ex) {
            logger.log(Level.SEVERE, "A cell cannot have multiple parents", ex);
        }
        String idString = setup.getMetaData().get("CellID");
        logger.info("Old cellID value: " + idString);
        long id = Long.valueOf(idString);
        logger.info("Old cellID id: " + id);
        CellID oldCellID = new CellID(id);
        logger.info("Old cellID: " + oldCellID);
        CellID newCellID = cellMO.getCellID();
        logger.info("New cellID: " + newCellID);
        if (cellMap.get(oldCellID) != null) {
            throw new RuntimeException("Failed trying to add new cellId to cellmap where cellID already exists");
        }

        cellMap.put(oldCellID, newCellID);
        logger.info("new cellID from map: " + cellMap.get(oldCellID));
    }

    

    public void unloadCell(CellID oldCellID) {
        //logger.info("oldCellID: " + oldCellID);
        CellID newCellID = cellMap.get(oldCellID);
        //logger.info("newCellID: " + newCellID);
        CellMO targetCell = CellManagerMO.getCell(newCellID);
        if (targetCell == null) {
            logger.severe("Could not find cell for ID: " + newCellID);
            return;
        }
        if (targetCell.getParent() == playerCellMORef.get()) {
            playerCellMORef.get().removeReplayedChild(newCellID);
        }
        CellManagerMO.getCellManager().removeCellFromWorld(targetCell);
        //remove the unloaded cell from the map
        cellMap.remove(oldCellID);
    }

    private void translateServerState(CellServerState cellServerState) {
        logger.info("translating: " + cellServerState);
        PositionComponentServerState cellPositionState = (PositionComponentServerState) cellServerState.getComponentServerState(PositionComponentServerState.class);
        if (cellPositionState == null) {
            logger.severe("Cell has no position state");
            return;
        }
        //Subtract the translation of the recorder from the translation of the cell
        Vector3f cellTranslation = cellPositionState.getTranslation();
        logger.info("Old translation: " + cellTranslation);
        Vector3f recorderTranslation = originalRecorderPosition.getTranslation();
        Vector3f newTranslation = cellTranslation.subtract(recorderTranslation);
        cellPositionState.setTranslation(newTranslation);
        logger.info("new translation: " + cellPositionState.getTranslation());
    }

}
