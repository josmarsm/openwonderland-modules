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
import org.jdesktop.wonderland.server.eventrecorder.EventRecorder;
import org.jdesktop.wonderland.modules.eventrecorder.common.EventRecorderCellChangeMessage;
import org.jdesktop.wonderland.modules.eventrecorder.common.EventRecorderCellServerState;
import org.jdesktop.wonderland.modules.eventrecorder.common.EventRecorderClientState;
import org.jdesktop.wonderland.modules.eventrecorder.common.Tape;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;

/**
 *
 * @author Bernard Horan
 * 
 */
public class EventRecorderCellMO extends CellMO {
    
    private static final Logger eventRecorderLogger = Logger.getLogger(EventRecorderCellMO.class.getName());
    private static int INSTANCE_COUNT = 0;
    private int instanceNumber;
    private Set<Tape> tapes = new HashSet<Tape>();
    private Tape selectedTape = null;
    private boolean isPlaying, isRecording;
    private String userName;
    private String recordingDirectory;
    private String recorderName;
    private EventRecorderImpl eventRecorder = null;


    public EventRecorderCellMO() {
        addComponent(new MovableComponentMO(this));
        instanceNumber = ++INSTANCE_COUNT;
        recorderName = "Recorder" + instanceNumber;
        recordingDirectory = "/tmp/EventRecordings/" + recorderName;
        createTapes();
        isRecording = false;
    }

    /**
     * Set the live state of this cell. Live cells are connected to the
     * world root and are present in the world, non-live cells are not
     * @param live
     */
    @Override
    protected void setLive(boolean live) {
        eventRecorderLogger.info("live: " + live);
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
            cellClientState = new EventRecorderClientState(tapes, selectedTape, isRecording, userName);
        }
        eventRecorderLogger.fine("cellClientState: " + cellClientState);
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
        eventRecorderLogger.info("Getting server state");
        /* Create a new EventRecorderCellServerState and populate its members */
        if (cellServerState == null) {
            cellServerState = new EventRecorderCellServerState();
            EventRecorderCellServerState state = (EventRecorderCellServerState)cellServerState;
            state.setInstanceNumber(instanceNumber);
            state.setTapes(tapes);
            state.setSelectedTape(selectedTape);
            state.setPlaying(isPlaying);
            state.setRecording(isRecording);
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
        eventRecorderLogger.fine("Getting client cell class name");
        return "org.jdesktop.wonderland.modules.eventrecorder.client.EventRecorderCell";
    }

    private void createTapes() {
        //Add any existing files
        File tapeDir = new File(recordingDirectory);
        if (!tapeDir.exists()) {
            eventRecorderLogger.info("Non existent directory: " + tapeDir);
            tapeDir.mkdirs();
        }
        String[] tapeFiles = tapeDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        for (int i = 0; i < tapeFiles.length; i++) {
                String string = tapeFiles[i];
                eventRecorderLogger.info("tapeFile: " + string);
                int index = string.indexOf(".xml");
                Tape aTape = new Tape(string.substring(0, index));
                aTape.setUsed();
                selectedTape = aTape; //Selected tape is last existing tape
                tapes.add(aTape);
            }

        if (selectedTape == null) {
            eventRecorderLogger.info("no selected tape");
            selectedTape = new Tape("Untitled Tape");
            tapes.add(selectedTape);
        }
    }

    private String getRecorderFilename() {
        //MUST end in '.xml'
        return recordingDirectory + File.separator+ selectedTape.getTapeName() + ".xml";
    }

    private void setRecording(boolean r) {
        if (isRecording) {
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
        isRecording = r;

    }

    private EventRecorder createEventRecorder() {
        if (eventRecorder == null) {
            eventRecorder = new EventRecorderImpl(this, recorderName);
        }
        return eventRecorder;
    }


    private void startRecording() {
        eventRecorderLogger.info("Start Recording");
        eventRecorder.startRecording(getRecorderFilename());
    }


    private void stopRecording() {
        eventRecorderLogger.info("Stop Recording");
        eventRecorder.stopRecording();
    }

    

    private void processRecordMessage(WonderlandClientID clientID, EventRecorderCellChangeMessage arcm) {
        setRecording(arcm.isRecording());
        userName = arcm.getUserName();

        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }


    private void processTapeUsedMessage(WonderlandClientID clientID, EventRecorderCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        for (Tape aTape : tapes) {
            if(aTape.getTapeName().equals(tapeName)) {
                aTape.setUsed();
                // send a message to all clients
                getChannel().sendAll(clientID, arcm);
            }
        }
    }

    private void processTapeSelectedMessage(WonderlandClientID clientID, EventRecorderCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        for (Tape aTape : tapes) {
            if(aTape.getTapeName().equals(tapeName)) {
                selectedTape = aTape;
                // send a message to all clients
                getChannel().sendAll(clientID, arcm);
            }
        }
    }

    private void processNewTapeMessage(WonderlandClientID clientID, EventRecorderCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        Tape newTape = new Tape(tapeName);
        tapes.add(newTape);
        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }

    private ChannelComponentMO getChannel() {
        return getComponent(ChannelComponentMO.class);
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
