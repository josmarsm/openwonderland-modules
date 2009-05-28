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
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.util.ScalableHashSet;
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
import java.util.logging.Level;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.eventrecorder.common.EventRecorderCellChangeMessage;
import org.jdesktop.wonderland.modules.eventrecorder.common.EventRecorderCellServerState;
import org.jdesktop.wonderland.modules.eventrecorder.common.EventRecorderClientState;
import org.jdesktop.wonderland.modules.eventrecorder.common.Tape;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.ChangesFileCreationListener;
import org.jdesktop.wonderland.modules.eventrecorder.server.WFSRecordingList;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;

/**
 *
 * Server side cell that represents the event recorder object in world.
 * Reponsible for receiving and sending messages to and from the client cell and managing the eventrecorder object
 * that actually does the work of recording.
 * @author Bernard Horan
 * 
 */
public class EventRecorderCellMO extends CellMO implements ChangesFileCreationListener {
    
    private static final Logger eventRecorderLogger = Logger.getLogger(EventRecorderCellMO.class.getName());
    private static int INSTANCE_COUNT = 0;
    private EventRecorderCellServerState serverState;
    private String recorderName;
    private ManagedReference<EventRecorderImpl> recorderRef;


    public EventRecorderCellMO() {
        super();
        addComponent(new MovableComponentMO(this));
        int instanceNumber = ++INSTANCE_COUNT;
        serverState = new EventRecorderCellServerState();
        recorderName = "Recorder" + instanceNumber;
        createTapes();
        serverState.setRecording(false);
    }

    /**
     * Set the live state of this cell. Live cells are connected to the
     * world root and are present in the world, non-live cells are not
     * @param live
     */
    @Override
    protected void setLive(boolean live) {
        //eventRecorderLogger.info("live: " + live);
        super.setLive(live);
        if (live) {
            ChannelComponentMO channel = getChannel();
            if (channel == null) {
                throw new IllegalStateException("Cell does not have a ChannelComponent");
            }
            //Add the message receiver to the channel
            channel.addMessageReceiver(EventRecorderCellChangeMessage.class,
                new EventRecorderCellMOMessageReceiver(this));
        } else {
            getChannel().removeMessageReceiver(EventRecorderCellChangeMessage.class);
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
            cellClientState = new EventRecorderClientState(serverState.getTapes(), serverState.getSelectedTape(), serverState.isRecording(), serverState.getUserName());
        }
        //eventRecorderLogger.fine("cellClientState: " + cellClientState);
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /**
     * Set up the cell from the given properties
     * @param cellServerState the properties to setup with
     */
    @Override
    public void setServerState(CellServerState cellServerState) {
        super.setServerState(cellServerState);
        createEventRecorder();
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
        //eventRecorderLogger.info("Getting server state");
        if (cellServerState == null) {
            cellServerState = serverState;
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
        //eventRecorderLogger.fine("Getting client cell class name");
        return "org.jdesktop.wonderland.modules.eventrecorder.client.EventRecorderCell";
    }

    boolean isRecording() {
        return serverState.isRecording();
    }

    private void createTapes() {
        WFSRecordingList recordingList = EventRecorderUtils.getWFSRecordings();
        String[] recordingNames = recordingList.getRecordings();
        for (int i = 0; i < recordingNames.length; i++) {
                String name = recordingNames[i];
                Tape aTape = new Tape(name);
                aTape.setUsed();
                serverState.setSelectedTape(aTape); //Selected tape is last existing tape
                serverState.addTape(aTape);
        }
        if (serverState.getSelectedTape() == null) {
            eventRecorderLogger.info("no selected tape");
            serverState.setSelectedTape(new Tape("Untitled Tape"));
            serverState.addTape(serverState.getSelectedTape());
        }
    }

    private void setRecording(boolean r) {
        //eventRecorderLogger.info("setRecording: " + r);
        //eventRecorderLogger.info("isRecording: " + isRecording);
        if (isRecording()) {
            //Already recording
            if (!r) {
                //Stop recording
                stopRecording();
            }
        } else {
            //Not recording
            if (r) {
                //Start recording
                startRecording();
            }
        }
        serverState.setRecording(r);
    }

    private void createEventRecorder() {
        if (recorderRef == null) {
            EventRecorderImpl eventRecorder = new EventRecorderImpl(this, recorderName);
            recorderRef = AppContext.getDataManager().createReference(eventRecorder);
        }
    }


    private void startRecording() {
        eventRecorderLogger.info("Start Recording");
        recorderRef.get().startRecording(serverState.getSelectedTape().getTapeName());
    }


    private void stopRecording() {
        eventRecorderLogger.info("Stop Recording");
        recorderRef.get().stopRecording();
    }

    

    private void processRecordMessage(WonderlandClientID clientID, EventRecorderCellChangeMessage arcm) {
        //eventRecorderLogger.info("isRecording: " + arcm.isRecording());
        setRecording(arcm.isRecording());
        serverState.setUserName(arcm.getUserName());

        // send a message to all clients
        //getChannel().sendAll(clientID, arcm);
    }


    private void processTapeUsedMessage(WonderlandClientID clientID, EventRecorderCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        for (Tape aTape : serverState.getTapes()) {
            if(aTape.getTapeName().equals(tapeName)) {
                aTape.setUsed();
                // send a message to all clients
                getChannel().sendAll(clientID, arcm);
            }
        }
    }

    private void processTapeSelectedMessage(WonderlandClientID clientID, EventRecorderCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        for (Tape aTape : serverState.getTapes()) {
            if(aTape.getTapeName().equals(tapeName)) {
                serverState.setSelectedTape(aTape);
                // send a message to all clients
                getChannel().sendAll(clientID, arcm);
            }
        }
    }

    private void processNewTapeMessage(WonderlandClientID clientID, EventRecorderCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        Tape newTape = new Tape(tapeName);
        serverState.addTape(newTape);
        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }

    private ChannelComponentMO getChannel() {
        return getComponent(ChannelComponentMO.class);
    }

    public void fileCreated() {
        logger.info("Changes file created, so start recording");
        //Register the eventRecorder, so it can start receiving messages to record
        recorderRef.get().register();
        //Let clients know that we're now recording, so that can change their UI
        // send a message to all clients
        EventRecorderCellChangeMessage arcm = EventRecorderCellChangeMessage.recordingMessage(cellID, isRecording(), serverState.getUserName());
        getChannel().sendAll(null, arcm);
    }

    public void fileCreationFailed(String reason, Throwable cause) {
        //There has been a problem creating the changes file
        //Log the error and terminate
        logger.log(Level.SEVERE, reason, cause);
        serverState.setRecording(false);
    }

    private static class EventRecorderCellMOMessageReceiver extends AbstractComponentMessageReceiver {

        public EventRecorderCellMOMessageReceiver(EventRecorderCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            //eventRecorderLogger.info("message received: " + message + ", ID: " + clientID);
            EventRecorderCellMO cellMO = (EventRecorderCellMO) getCell();
            EventRecorderCellChangeMessage arcm = (EventRecorderCellChangeMessage) message;
            switch (arcm.getAction()) {
                case RECORD:
                    cellMO.processRecordMessage(clientID, arcm);
                    break;
                case TAPE_USED:
                    cellMO.processTapeUsedMessage(clientID, arcm);
                    break;
                case TAPE_SELECTED:
                    cellMO.processTapeSelectedMessage(clientID, arcm);
                    break;
                case NEW_TAPE:
                    cellMO.processNewTapeMessage(clientID, arcm);
                    break;
            }
        }

        @Override
        protected void postRecordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            super.postRecordMessage(sender, clientID, message);
        }
    }

    

}
