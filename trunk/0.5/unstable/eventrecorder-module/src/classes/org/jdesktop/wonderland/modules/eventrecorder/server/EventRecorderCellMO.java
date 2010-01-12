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


package org.jdesktop.wonderland.modules.eventrecorder.server;

import com.jme.math.Vector3f;
import com.sun.mpk20.voicelib.app.Recorder;
import com.sun.mpk20.voicelib.app.RecorderSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.modules.eventrecorder.common.EventRecorderCellChangeMessage;
import org.jdesktop.wonderland.modules.eventrecorder.common.EventRecorderCellServerState;
import org.jdesktop.wonderland.modules.eventrecorder.common.EventRecorderClientState;
import org.jdesktop.wonderland.modules.eventrecorder.common.Tape;
import org.jdesktop.wonderland.modules.eventrecorder.common.TapeStateMessageResponse;
import org.jdesktop.wonderland.modules.eventrecorder.server.EventRecordingManager.ChangesFileCreationListener;
import org.jdesktop.wonderland.common.wfs.WFSRecordingList;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;
import org.jdesktop.wonderland.server.cell.ViewCellCacheMO;
import org.jdesktop.wonderland.server.cell.view.ViewCellMO;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager.ListRecordingsListener;
import org.jdesktop.wonderland.server.wfs.exporter.CellExporterUtils;

/**
 *
 * Server side cell that represents the event recorder object in world.
 * Reponsible for receiving and sending messages to and from the client cell and managing the eventrecorder object
 * that actually does the work of recording.
 * @author Bernard Horan
 * 
 */
public class EventRecorderCellMO extends ViewCellMO implements ChangesFileCreationListener, ListRecordingsListener {
    
    private static final Logger eventRecorderLogger = Logger.getLogger(EventRecorderCellMO.class.getName());
    private static int INSTANCE_COUNT = 0;
    private EventRecorderCellServerState serverState;
    private String recorderName;
    private ManagedReference<EventRecorderImpl> recorderRef;
    private ManagedReference<EventRecorderCellCacheMO> eventRecorderCellCacheRef;
    private String callId;
    private Recorder audioRecorder;

    
    public EventRecorderCellMO() {
        super();
        addComponent(new MovableComponentMO(this));
        int instanceNumber = ++INSTANCE_COUNT;
        serverState = new EventRecorderCellServerState();
        recorderName = "Recorder" + instanceNumber;
        createTapes();
        serverState.setRecording(false);
        addTransformChangeListener(new TransformChangeListener());
        callId = getCellID().toString();
        int ix = callId.indexOf("@");
        if (ix >= 0) {
            callId = callId.substring(ix + 1);
        }
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
            //Logout from the cell cache and unset the ref
            eventRecorderCellCacheRef.get().logout();
            logger.info("setting cellCacheRef to null");
            eventRecorderCellCacheRef = null;
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
        // Check to see if the CellServerState has a PositionComponentServerState
        // and takes it origin. This will only work upon the initial creation
        // of the cell and not when the cell is moved at all. This class should
        // add a transform change listener to listen for changes in the cell
        // origin after the cell has been created.
        PositionComponentServerState state = (PositionComponentServerState) cellServerState.getComponentServerState(PositionComponentServerState.class);
        if (state != null) {
            setupAudioRecorder(state.getTranslation());
        }
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

    Set<CellID> getLoadedCells() {
        return eventRecorderCellCacheRef.get().getLoadedCells();
    }
    
    boolean isRecording() {
        return serverState.isRecording();
    }

    private void createTapes() {
        WFSRecordingList recordingList = null;
        try {
            recordingList = CellExporterUtils.getWFSRecordings();
        } catch (JAXBException ex) {
            eventRecorderLogger.log(Level.SEVERE, "Failed to retrieved list of recordings", ex);
            return;
        } catch (IOException ex) {
            eventRecorderLogger.log(Level.SEVERE, "Failed to retrieved list of recordings", ex);
            return;
        }
        String[] tapeNames = recordingList.getRecordings();
        for (int i = 0; i < tapeNames.length; i++) {
                String name = tapeNames[i];
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

    private ManagedReference<EventRecorderCellCacheMO> getEventRecorderCellCacheRef() {
        return eventRecorderCellCacheRef;
    }

    private ManagedReference<EventRecorderImpl> getRecorderRef() {
        return recorderRef;
    }

    private void setCellCache(EventRecorderCellCacheMO cache) {
        eventRecorderCellCacheRef = AppContext.getDataManager().createReference(cache);
        cache.login();
    }

    private void updateTapes(String[] tapeNames) {
        //tapeNames are the names of the recordings as known to the web service
        //If there's no tapes, do nothing
        if (tapeNames.length < 1) {
            return;
        }
        //firstly, remove all the existing tape names
        serverState.clearTapes();
        //Now add a tape of each name
        for (int i = 0; i < tapeNames.length; i++) {
                String name = tapeNames[i];
                Tape aTape = new Tape(name);
                aTape.setUsed();
                serverState.addTape(aTape);
        }
        //Check to see if the currently selected tape still exists
        //If not, set it to be the first one
        if (!serverState.getTapes().contains(serverState.getSelectedTape())) {
            List<Tape> sortedTapes = new ArrayList<Tape>(serverState.getTapes());
            Collections.sort(sortedTapes);
            serverState.setSelectedTape(sortedTapes.get(0));
        }
    }

    private void processTapeStateMessage(WonderlandClientID clientID, WonderlandClientSender sender, EventRecorderCellChangeMessage arcm) {
        //eventRecorderLogger.info("clientID: " + clientID + ", sender: " + sender);
        MessageID messageID  = arcm.getMessageID();
        //eventRecorderLogger.info("messageID: " + messageID);
        CellExportManager mgr = AppContext.getManager(CellExportManager.class);
        //eventRecorderLogger.info("asynchronously requesting recordings");
        //If successful, async method call to reocordingsListed()
        //If not successful, async call to recordingsListfailed()
        mgr.listRecordings(messageID, sender, clientID, this);
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

    private void setupAudioRecorder(Vector3f origin) {
        VoiceManager vm = AppContext.getManager(VoiceManager.class);

        RecorderSetup setup = new RecorderSetup();

        setup.x = origin.x;
        setup.y = origin.y;
        setup.z = origin.z;

        logger.info("Recorder Origin is " + "(" + origin.x + ":" + origin.y + ":" + origin.z + ")");

        setup.spatializer = vm.getVoiceManagerParameters().livePlayerSpatializer;

        try {
            audioRecorder = vm.createRecorder(callId, setup);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private String getAudioRecordingDirectory() {
        return "/tmp/EventRecordings";
    }

    private void startRecording() {
        //eventRecorderLogger.info("Start Recording");
        //logger.info("cellCacheRef for " + recorderName + ": " + eventRecorderCellCacheRef);
        String tapeName = serverState.getSelectedTape().getTapeName();
        try {
            audioRecorder.startRecording(getAudioRecorderFilename(tapeName));
        } catch (IOException ex) {
            eventRecorderLogger.log(Level.SEVERE, "Failed to start audio recording", ex);
        }
        recorderRef.get().startRecording(tapeName, eventRecorderCellCacheRef.get().getLoadedCells());
    }

    private String getAudioRecorderFilename(String tapeName) {
        //TODO nasty hack
        //tapeName = tapeName.replace(' ', '_');
        return getAudioRecordingDirectory() + File.separator + "EventRecording_" + tapeName + ".au";
    }


    private void stopRecording() {
        //eventRecorderLogger.info("Stop Recording");
        recorderRef.get().stopRecording();
        try {
            audioRecorder.stopRecording();
        } catch (IOException ex) {
            eventRecorderLogger.log(Level.SEVERE, "Failed to stop audio recording", ex);
        }
    }

    

    private void processRecordMessage(WonderlandClientID clientID, EventRecorderCellChangeMessage arcm) {
        //eventRecorderLogger.info("isRecording: " + arcm.isRecording());
        setRecording(arcm.isRecording());
        serverState.setUserName(arcm.getUserName());

        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
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
        //logger.info("Changes file created, so start recording");
        //Register the eventRecorder, so it can start receiving messages to record
        recorderRef.get().register();
        //Let clients know that we're now recording, so that can change their UI
        // send a message to all clients
        EventRecorderCellChangeMessage arcm = EventRecorderCellChangeMessage.recordingMessage(cellID, isRecording(), serverState.getUserName());
        getChannel().sendAll(null, arcm);
        //The selected tape is no longer fresh
        //Change its state and let clients know
        serverState.getSelectedTape().setUsed();
        arcm = EventRecorderCellChangeMessage.setTapeUsed(cellID, serverState.getSelectedTape().getTapeName());
        getChannel().sendAll(null, arcm);
    }

    public void fileCreationFailed(String reason, Throwable cause) {
        //There has been a problem creating the changes file
        //Log the error and terminate
        logger.log(Level.SEVERE, reason, cause);
        serverState.setRecording(false);
    }

    public void listRecordingsResult(MessageID messageID, WonderlandClientSender sender, WonderlandClientID clientID, WFSRecordingList recordings) {
        //eventRecorderLogger.info("received recordings: " + recordings.getRecordings());
        updateTapes(recordings.getRecordings());
        //eventRecorderLogger.info("serverState: " + serverState);
        TapeStateMessageResponse tsmr = TapeStateMessageResponse.tapeStateMessage(messageID, serverState);
        //eventRecorderLogger.info("responding to original synchronous request");
        sender.send(clientID, tsmr);
    }

    public void listRecordingsFailed(MessageID messageID, WonderlandClientSender sender, WonderlandClientID clientID, String message, Exception ex) {
        eventRecorderLogger.log(Level.SEVERE, message, ex);
        TapeStateMessageResponse tsmr = TapeStateMessageResponse.tapeStateFailedMessage(messageID);
        sender.send(clientID, tsmr);
    }    

    @Override
    public ViewCellCacheMO getCellCache() {
        return eventRecorderCellCacheRef.get();
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
                case TAPE_SELECTED:
                    cellMO.processTapeSelectedMessage(clientID, arcm);
                    break;
                case NEW_TAPE:
                    cellMO.processNewTapeMessage(clientID, arcm);
                    break;
                case REQUEST_TAPE_STATE:
                    cellMO.processTapeStateMessage(clientID, sender, arcm);
                    break;
                default:
                    eventRecorderLogger.severe("Unknown action type: " + arcm.getAction());
            }
        }

        
    }

    private static class TransformChangeListener implements TransformChangeListenerSrv {

        public void transformChanged(ManagedReference<CellMO> cellRef, CellTransform localTransform, CellTransform worldTransform) {
        //It's at this stage that we know we're in the world and located at a certain position
        //logger.info("worldTransform: " + getWorldTransform(null));
        //Create the cell cache and set it up
        EventRecorderCellMO cellMO = (EventRecorderCellMO) cellRef.get();
        if (cellMO.getEventRecorderCellCacheRef() == null) {
            cellMO.setCellCache(new EventRecorderCellCacheMO(cellMO, cellMO.getRecorderRef()));
            //We don't need to listen any longer
            cellMO.removeTransformChangeListener(this);
        } else {
            //we shouldn't have reached here
            logger.severe("Failed to remove change listener");
        }
    }

    }

}
