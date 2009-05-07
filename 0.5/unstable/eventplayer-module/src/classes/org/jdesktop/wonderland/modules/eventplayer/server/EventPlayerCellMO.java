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
import com.sun.sgs.app.ManagedReference;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import java.io.File;
import java.io.FilenameFilter;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.eventplayer.common.EventPlayerCellChangeMessage;
import org.jdesktop.wonderland.modules.eventplayer.common.EventPlayerCellServerState;
import org.jdesktop.wonderland.modules.eventplayer.common.EventPlayerClientState;
import org.jdesktop.wonderland.modules.eventplayer.common.Tape;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;

/**
 *
 * Server side cell that represents the event recorder object in world.
 * Reponsible for receiving and sending messages to and from the client cell and managing the eventrecorder object
 * that actually does the work of recording.
 * @author Bernard Horan
 * 
 */
public class EventPlayerCellMO extends CellMO {
    
    private static final Logger eventPlayerLogger = Logger.getLogger(EventPlayerCellMO.class.getName());
    private static int INSTANCE_COUNT = 0;
    private int instanceNumber;
    private Set<Tape> tapes = new HashSet<Tape>();
    private Tape selectedTape = null;
    private boolean isLoading;
    private String userName;
    private String recordingDirectory;
    private String playerName;
    private ManagedReference<EventPlayerImpl> playerRef;
    private boolean isPlaying;


    public EventPlayerCellMO() {
        super();
        addComponent(new MovableComponentMO(this));
        instanceNumber = ++INSTANCE_COUNT;
        playerName = "Player" + instanceNumber;
        recordingDirectory = "/tmp/EventRecordings/" + playerName;
        createTapes();
        isLoading = false;
    }

    /**
     * Set the live state of this cell. Live cells are connected to the
     * world root and are present in the world, non-live cells are not
     * @param live
     */
    @Override
    protected void setLive(boolean live) {
        eventPlayerLogger.info("live: " + live);
        super.setLive(live);
        if (live) {
            ChannelComponentMO channel = getChannel();
            if (channel == null) {
                throw new IllegalStateException("Cell does not have a ChannelComponent");
            }
            //Add the message receiver to the channel
            channel.addMessageReceiver(EventPlayerCellChangeMessage.class,
                new EventPlayerCellMOMessageReceiver(this));
        } else {
            getChannel().removeMessageReceiver(EventPlayerCellChangeMessage.class);
        }
    }

    /**
     * Returns the client-side state of the cell. If the cellClientState argument
     * is null, then the method should create an appropriate class, otherwise,
     * the method should just fill in details in the class. Returns the client-
     * side state class
     *
     * @param cellClientState If null, create a new object
     * @param clientID The unique ID of the client
     * @param capabilities The client capabilities
     * @return an instance of type CellClientState that describes the client-side state of the cell
     */
    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID,
            ClientCapabilities capabilities) {
        
        if (cellClientState == null) {
            cellClientState = new EventPlayerClientState(tapes, selectedTape, isLoading, userName);
        }
        eventPlayerLogger.fine("cellClientState: " + cellClientState);
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /**
     * Set up the cell from the given properties
     * @param cellServerState the properties to setup with
     */
    @Override
    public void setServerState(CellServerState cellServerState) {
        super.setServerState(cellServerState);
        createEventPlayer();
    }


    /**
     * Returns the setup information currently configured on the cell. If the
     * setup argument is non-null, fill in that object and return it. If the
     * setup argument is null, create a new setup object.
     *
     * @param cellServerState The setup object, if null, creates one.
     * @return The current setup information
     */
    @Override
    public CellServerState getServerState(CellServerState cellServerState) {
        eventPlayerLogger.info("Getting server state");
        /* Create a new EventRecorderCellServerState and populate its members */
        if (cellServerState == null) {
            cellServerState = new EventPlayerCellServerState();
            EventPlayerCellServerState state = (EventPlayerCellServerState)cellServerState;
            state.setInstanceNumber(instanceNumber);
            state.setTapes(tapes);
            state.setSelectedTape(selectedTape);
            state.setPlaying(isLoading);
            state.setPlaying(isLoading);
            state.setUserName(userName);
            state.setRecordingDirectory(recordingDirectory);
        }
        return super.getServerState(cellServerState);
    }

    

    /**
     * Returns the fully qualified name of the class that represents
     * this cell on the client
     * @param clientID The unique ID of the client
     * @param capabilities The client capabilities
     * @return a string representing the name of the class of the cell on the client
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        eventPlayerLogger.fine("Getting client cell class name");
        return "org.jdesktop.wonderland.modules.eventplayer.client.EventPlayerCell";
    }

    private void createTapes() {
        //Add any existing files
        File tapeDir = new File(recordingDirectory);
        if (!tapeDir.exists()) {
            eventPlayerLogger.info("Non existent directory: " + tapeDir);
            tapeDir.mkdirs();
        }
        String[] tapeFiles = tapeDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        for (int i = 0; i < tapeFiles.length; i++) {
                String string = tapeFiles[i];
                eventPlayerLogger.info("tapeFile: " + string);
                int index = string.indexOf(".xml");
                Tape aTape = new Tape(string.substring(0, index));
                aTape.setUsed();
                selectedTape = aTape; //Selected tape is last existing tape
                tapes.add(aTape);
            }

        if (selectedTape == null) {
            eventPlayerLogger.info("no selected tape");
            selectedTape = new Tape("Untitled Tape");
            tapes.add(selectedTape);
        }
    }

    private String getPlayerFilename() {
        return recordingDirectory + File.separator+ selectedTape.getTapeName();
    }

    private void setLoading(boolean p) {
        if (isLoading) {
            //Already loading
            if (!p) {
                stopLoading();
            }
        } else {
            //Not loading
            if (p) {
                //Start loading
                startLoading();
            }
        }
        isLoading = p;
    }

    private void setPlaying(boolean p) {
        if (isPlaying) {
            //Already playing
            if (!p) {
                stopPlaying();
            }
        } else {
            //Not loading
            if (p) {
                //Start loading
                startPlaying();
            }
        }
        isPlaying = p;
    }

    private void createEventPlayer() {
        if (playerRef == null) {
            EventPlayerImpl eventPlayer = new EventPlayerImpl(this, playerName);
            playerRef = AppContext.getDataManager().createReference(eventPlayer);
        }
    }


    private void startLoading() {
        //eventPlayerLogger.info("Start Loading");
        playerRef.get().startLoading(selectedTape.getTapeName());
    }

    private void startPlaying() {
        //eventPlayerLogger.info("Start Playing");
        playerRef.get().startPlaying(selectedTape.getTapeName());
    }


    private void stopLoading() {
        eventPlayerLogger.info("Stop Loading");
        playerRef.get().stopLoading();
    }

    private void stopPlaying() {
        eventPlayerLogger.info("Stop Playing");
        playerRef.get().stopPlaying();
    }

    

    private void processLoadMessage(WonderlandClientID clientID, EventPlayerCellChangeMessage arcm) {
        setLoading(arcm.isLoading());
        userName = arcm.getUserName();

        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }

    private void processPlayMessage(WonderlandClientID clientID, EventPlayerCellChangeMessage arcm) {
        setPlaying(arcm.isPlaying());
        userName = arcm.getUserName();

        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }


    private void processTapeUsedMessage(WonderlandClientID clientID, EventPlayerCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        for (Tape aTape : tapes) {
            if(aTape.getTapeName().equals(tapeName)) {
                aTape.setUsed();
                // send a message to all clients
                getChannel().sendAll(clientID, arcm);
            }
        }
    }

    private void processTapeSelectedMessage(WonderlandClientID clientID, EventPlayerCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        for (Tape aTape : tapes) {
            if(aTape.getTapeName().equals(tapeName)) {
                selectedTape = aTape;
                // send a message to all clients
                getChannel().sendAll(clientID, arcm);
            }
        }
    }

    private void processNewTapeMessage(WonderlandClientID clientID, EventPlayerCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        Tape newTape = new Tape(tapeName);
        tapes.add(newTape);
        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }

    private ChannelComponentMO getChannel() {
        return getComponent(ChannelComponentMO.class);
    }

    private static class EventPlayerCellMOMessageReceiver extends AbstractComponentMessageReceiver {

        public EventPlayerCellMOMessageReceiver(EventPlayerCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            //eventRecorderLogger.info("message received: " + message + ", ID: " + clientID);
            EventPlayerCellMO cellMO = (EventPlayerCellMO) getCell();
            EventPlayerCellChangeMessage arcm = (EventPlayerCellChangeMessage) message;
            switch (arcm.getAction()) {
                case LOAD:
                    cellMO.processLoadMessage(clientID, arcm);
                    break;
                case PLAY:
                    cellMO.processPlayMessage(clientID, arcm);
                    break;
                case TAPE_SELECTED:
                    cellMO.processTapeSelectedMessage(clientID, arcm);
                    break;
            }
        }

        @Override
        protected void postRecordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            super.postRecordMessage(sender, clientID, message);
        }
    }

    

}
