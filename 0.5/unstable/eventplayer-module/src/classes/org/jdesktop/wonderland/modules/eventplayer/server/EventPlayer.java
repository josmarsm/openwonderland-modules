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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.messages.MessagePacker.ReceivedMessage;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.RecordingLoaderUtils.CellImportEntry;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.modules.eventplayer.server.EventPlayingManager.MessagesReplayingListener;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.CellImportManager;
import org.jdesktop.wonderland.modules.eventplayer.server.wfs.CellImportManager.CellRetrievalListener;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellMOFactory;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.wfs.importer.CellMap;
import org.xml.sax.InputSource;

/**
 * An implementation of an event player that loads a recorded state of a world and replays messages to that world.
 * @author Bernard Horan
 */
public class EventPlayer implements ManagedObject, CellRetrievalListener, MessagesReplayingListener,Serializable {

    private static final Logger logger = Logger.getLogger(EventPlayer.class.getName());
    /*is this recorder loading a world?*/
    private boolean isLoading = false;
    /*The name of this recorder*/
    //TODO: is this necessary?
    private String playerName;
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
    


    /** Creates a new instance of EventRecorderImpl
     * @param originCell the cell that is the event recorder
     * @param name the name of the event recorder
     */
    public EventPlayer(CellMO originCell, String name) {
        this.playerName = name;
        clientID = new PlayerClientID();
    }

    public String getName() {
        return playerName;
    }

    public void playMessage(ReceivedMessage rMessage) {
        
        CellMessage message = (CellMessage) rMessage.getMessage();
        //logger.info("cellmap: " + cellMap);
        CellID oldCellID = message.getCellID();
        logger.info("oldCellID: " + oldCellID);
        CellID newCellID = cellMap.get(oldCellID);
        logger.info("newCellID: " + newCellID);
        message.setCellID(newCellID);
        CellMO targetCell = CellManagerMO.getCell(newCellID);
        logger.info("targetCell: " + targetCell);
        ChannelComponentMO channel = targetCell.getComponent(ChannelComponentMO.class);
        logger.info("channel: " + channel);
        if (channel == null) {
            throw new RuntimeException("No channel for " + targetCell);
        }
        channel.messageReceived(null, clientID, message);
    }

    /**
     * Start recording to the tape given in the parameter
     * @param tapeName the name of the selected tape in the event recorder
     */
    void loadRecording(String tapeName) {
        logger.info("start loading: " + tapeName);
        this.tapeName = tapeName;
        //Load the cells labelled by tape name
        //then replay messages
        loadRecording();
    }

    /**
     * Start recording to the tape given in the parameter
     * @param tapeName the name of the selected tape in the event recorder
     */
    void startPlaying(String tapeName) {
        logger.info("start playing: " + tapeName);
        //this.tapeName = tapeName;
        //Load the cells labelled by tape name
        //then replay messages
        replayMessages();
    }

    private void loadRecording() {
        // get the export service
        CellImportManager im = AppContext.getManager(CellImportManager.class);

        // first, create a new recording.  The remainder of the export procedure will happen
        // in the cellsRetrieved() method of the listener
        im.retrieveCells(tapeName, this);
    }


    public void stopPlaying() {
        
    }


    @Override
    public String toString() {
        return super.toString() + " name: " + getName();
    }

    public void cellRetrievalFailed(String reason, Throwable cause) {
        logger.log(Level.SEVERE, reason, cause);
    }

    public void cellsRetrieved(CellMap<CellImportEntry> cellRetrievalMap, CellMap<CellID> cellPathMap) {
        Set<String> keys = cellRetrievalMap.keySet();
        for (String key : keys) {
            CellImportEntry entry = cellRetrievalMap.get(key);
            logger.info("processing child " + entry.getName());

            CellID parentID = cellPathMap.get(entry.getRelativePath());

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
                if (parentID == null) {
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
                    CellMO parentCellMO = CellManagerMO.getCell(parentID);
                    logger.info("EventPlayerImpl: Adding child " + cellMO.getName() + " (ID=" + cellMO.getCellID().toString() +
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
            logger.info("Old cellID: " + oldCellID);
            if (cellMap.get(oldCellID) != null) {
                throw new RuntimeException("Failed trying to add new cellId to cellmap where cellID already exists");
            }
            CellID newCellID = cellMO.getCellID();
            logger.info("New cellID: " + newCellID);
            cellMap.put(oldCellID, newCellID);
            //logger.info("new cellID from map: " + cellMap.get(oldCellID));

            
        }
        logger.info("COMPLETE");
    }

    private void replayMessages() {
        InputSource recordingSource = new InputSource("/Users/bh37721/.wonderland-server/0.5-dev/wfs/recordings/Untitled Tape/changes.xml");
        // get the event playing service
        EventPlayingManager epm = AppContext.getManager(EventPlayingManager.class);
        epm.replayMessages(recordingSource, this);
    }

    

    

    public void allCellsRetrieved() {
       //TODO
    }

    

}
