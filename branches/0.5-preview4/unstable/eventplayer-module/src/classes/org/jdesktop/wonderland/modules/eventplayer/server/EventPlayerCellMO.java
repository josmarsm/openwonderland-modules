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
import com.sun.mpk20.voicelib.app.Recorder;
import com.sun.mpk20.voicelib.app.RecorderSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.util.ScalableHashSet;
import java.io.IOException;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import java.util.logging.Level;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.wfs.WFSRecordingList;
import org.jdesktop.wonderland.modules.eventplayer.common.EventPlayerCellChangeMessage;
import org.jdesktop.wonderland.modules.eventplayer.common.EventPlayerCellServerState;
import org.jdesktop.wonderland.modules.eventplayer.common.EventPlayerClientState;
import org.jdesktop.wonderland.modules.eventplayer.common.Tape;
import org.jdesktop.wonderland.modules.eventplayer.common.TapeStateMessageResponse;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager;
import org.jdesktop.wonderland.server.wfs.exporter.CellExportManager.ListRecordingsListener;
import org.jdesktop.wonderland.server.wfs.exporter.CellExporterUtils;

/**
 *
 * Server side cell that represents the event player object in world.
 * Reponsible for receiving and sending messages to and from the client cell and managing the eventplayer object
 * that actually does the work of loading and playing recordings.
 * @author Bernard Horan
 * 
 */
public class EventPlayerCellMO extends CellMO implements ListRecordingsListener {
    
    private static final Logger eventPlayerLogger = Logger.getLogger(EventPlayerCellMO.class.getName());
    private EventPlayerCellServerState serverState;
    private ManagedReference<EventPlayer> playerRef;
    private ManagedReference<ScalableHashSet<CellID>> loadedCellsRef;
    private String callId;
    private Recorder audioRecorder;

    public EventPlayerCellMO() {
        super();
        addComponent(new MovableComponentMO(this));
        serverState = new EventPlayerCellServerState();
        ScalableHashSet<CellID> loadedCells = new ScalableHashSet<CellID>();
        loadedCellsRef = AppContext.getDataManager().createReference(loadedCells);
        createTapes();
        callId = getCellID().toString();
        int ix = callId.indexOf("@");
        if (ix >= 0) {
            callId = callId.substring(ix + 1);
        }
    }

    @Override
    protected void setLive(boolean live) {
        //eventPlayerLogger.info("live: " + live);
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
            cellClientState = new EventPlayerClientState(serverState.getTapes(), serverState.getSelectedTape(), serverState.isPlaying(), serverState.isPaused(), serverState.getUserName(), loadedCellsRef.get().size());
        }
        //eventPlayerLogger.fine("cellClientState: " + cellClientState);
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
        //eventPlayerLogger.info("Getting server state");
        /* Create a new EventRecorderCellServerState and populate its members */
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
        //eventPlayerLogger.fine("Getting client cell class name");
        return "org.jdesktop.wonderland.modules.eventplayer.client.EventPlayerCell";
    }

    void addReplayedChild(CellMO cellMO) throws MultipleParentException {
        CellID childCellID = cellMO.getCellID();
        logger.fine("adding replayed child: " + cellMO);
        loadedCellsRef.getForUpdate().add(childCellID);
        addChild(cellMO);
        EventPlayerCellChangeMessage epccm = EventPlayerCellChangeMessage.addReplayedChild(getCellID());
        getChannel().sendAll(null, epccm);
        
    }

    void removeReplayedChild(CellID childCellID) {
        loadedCellsRef.getForUpdate().remove(childCellID);
        EventPlayerCellChangeMessage msg = EventPlayerCellChangeMessage.removeReplayedChild(getCellID());
        getChannel().sendAll(null, msg);
    }

    private void createTapes() {
        WFSRecordingList recordingList = null;
        try {
            recordingList = CellExporterUtils.getWFSRecordings();
        } catch (JAXBException ex) {
            eventPlayerLogger.log(Level.SEVERE, "Failed to retrieve recordings", ex);
            return;
        } catch (IOException ex) {
            eventPlayerLogger.log(Level.SEVERE, "Failed to retrieve recordings", ex);
            return;
        }
        String[] tapeNames = recordingList.getRecordings();
        for (int i = 0; i < tapeNames.length; i++) {
                String name = tapeNames[i];
                Tape aTape = new Tape(name);
                serverState.addTape(aTape);
        }
        if (serverState.getTapes().isEmpty()) {
            eventPlayerLogger.warning("no tapes");
        }
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
                serverState.addTape(aTape);
        }
    }

    private void processTapeStateMessage(WonderlandClientID clientID, WonderlandClientSender sender, EventPlayerCellChangeMessage arcm) {
        //eventPlayerLogger.info("clientID: " + clientID + ", sender: " + sender);
        MessageID messageID  = arcm.getMessageID();
        //eventPlayerLogger.info("messageID: " + messageID);
        CellExportManager mgr = AppContext.getManager(CellExportManager.class);
        //eventPlayerLogger.info("asynchronously requesting recordings");
        //If successful, async method call to reocordingsListed()
        //If not successful, async call to recordingsListfailed()
        mgr.listRecordings(messageID, sender, clientID, this);
    }

    private void setPlaying(boolean p) {
        logger.info("p: " + p);
        logger.info("isPlaying: " + serverState.isPlaying());
        if (serverState.isPlaying()) {
            //Already playing
            if (!p) {
                stopPlaying();
                serverState.setPlaying(true);
                serverState.setPaused(true);
            }
        } else {
            //Not playing
            if (p) {
                //Start playing
                startPlaying();
                serverState.setPlaying(true);
                serverState.setPaused(false);
            }
        }
        
    }

    private void createEventPlayer() {
        if (playerRef == null) {
            EventPlayer eventPlayer = new EventPlayer(this);
            playerRef = AppContext.getDataManager().createReference(eventPlayer);
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

        //setup.recordDirectory = getAudioRecordingDirectory();

        try {
            audioRecorder = vm.createRecorder(callId, setup);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private String getAudioRecordingDirectory() {
        return "/tmp/EventRecordings";
    }

    private void unloadRecording() {
        Tape selectedTape = serverState.getSelectedTape();
        if (selectedTape != null) {
            logger.info("selected tape: " + selectedTape);
            ScalableHashSet<CellID> loadedCells = loadedCellsRef.getForUpdate();
            for (CellID childCellID : loadedCells) {
                CellMO cell = CellManagerMO.getCell(childCellID);
                logger.info("removing cell: " + cell);
                CellManagerMO.getCellManager().removeCellFromWorld(cell);
            }
            loadedCells.clear();
            playerRef.get().unloadRecording(selectedTape.getTapeName());
        }
    }

    private void loadRecording() {
        eventPlayerLogger.fine("Load recording");
        playerRef.get().loadRecording(serverState.getSelectedTape().getTapeName());
    }

    private void startPlaying() {
        eventPlayerLogger.info("Start Playing");
        playerRef.get().startPlaying();
        try {
            audioRecorder.playRecording(getAudioRecordingURL());
        } catch (IOException ex) {
            eventPlayerLogger.log(Level.SEVERE, "Failed to play recording", ex);
        }

    }

    private String getAudioRecordingURL() {
        String tapeName = serverState.getSelectedTape().getTapeName();
        //TODO this is too much of a hack
        //tapeName = tapeName.replace(' ', '_');
        String resourceName = "EventRecording_" + tapeName + ".au";
        String webserverURL = System.getProperty("wonderland.web.server.url", "http://localhost:8080");
        String recordingURL = webserverURL + "/webdav/content/system/AudioRecordings/" + resourceName;
        return recordingURL;
    }

    private void stopPlaying() {
        eventPlayerLogger.info("Stop Playing");
        playerRef.get().stopPlaying();
        try {
            audioRecorder.stopPlayingRecording();
        } catch (IOException ex) {
            eventPlayerLogger.log(Level.SEVERE, "Failed to stop playing recording", ex);
        }
    }

    void playbackDone() {
        try {
            audioRecorder.stopPlayingRecording();
        } catch (IOException ex) {
            eventPlayerLogger.log(Level.SEVERE, "Failed to stop playing recording", ex);
        }
        serverState.setPlaying(false);
        EventPlayerCellChangeMessage epccm = EventPlayerCellChangeMessage.playbackDone(getCellID());
        getChannel().sendAll(null, epccm);
    }

    void allCellsRetrieved() {
        logger.fine("All cells retrieved");
        EventPlayerCellChangeMessage epccm = EventPlayerCellChangeMessage.allCellsRetrieved(getCellID());
        getChannel().sendAll(null, epccm);
    }

    private void processPlayMessage(WonderlandClientID clientID, EventPlayerCellChangeMessage arcm) {
        logger.info("isPlaying: " + arcm.isPlaying());
        setPlaying(arcm.isPlaying());
        serverState.setUserName(arcm.getUserName());

        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }

    private void processTapeSelectedMessage(WonderlandClientID clientID, EventPlayerCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        for (Tape aTape : serverState.getTapes()) {
            if(aTape.getTapeName().equals(tapeName)) {
                unloadRecording();
                serverState.setSelectedTape(aTape);
                // send a message to all clients
                getChannel().sendAll(clientID, arcm);
                loadRecording();
            }
        }
    }

    public void listRecordingsResult(MessageID messageID, WonderlandClientSender sender, WonderlandClientID clientID, WFSRecordingList recordings) {
        //eventPlayerLogger.info("received recordings: " + recordings.getRecordings());
        updateTapes(recordings.getRecordings());
        //eventPlayerLogger.info("serverState: " + serverState);
        TapeStateMessageResponse tsmr = TapeStateMessageResponse.tapeStateMessage(messageID, serverState);
        //eventPlayerLogger.info("responding to original synchronous request");
        sender.send(clientID, tsmr);
    }

    public void listRecordingsFailed(MessageID messageID, WonderlandClientSender sender, WonderlandClientID clientID, String message, Exception ex) {
        eventPlayerLogger.log(Level.SEVERE, message, ex);
        TapeStateMessageResponse tsmr = TapeStateMessageResponse.tapeStateFailedMessage(messageID);
        sender.send(clientID, tsmr);
    }

    private ChannelComponentMO getChannel() {
        return getComponent(ChannelComponentMO.class);
    }

    private static class EventPlayerCellMOMessageReceiver extends AbstractComponentMessageReceiver {

        public EventPlayerCellMOMessageReceiver(EventPlayerCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            logger.fine("message received: " + message + ", ID: " + clientID);
            EventPlayerCellMO cellMO = (EventPlayerCellMO) getCell();
            EventPlayerCellChangeMessage arcm = (EventPlayerCellChangeMessage) message;
            switch (arcm.getAction()) {
                case LOAD:
                    cellMO.processTapeSelectedMessage(clientID, arcm);
                    break;
                case PLAY:
                    cellMO.processPlayMessage(clientID, arcm);
                    break;
                case REQUEST_TAPE_STATE:
                    cellMO.processTapeStateMessage(clientID, sender, arcm);
                    break;
                default:
                    eventPlayerLogger.severe("Unknown action type: " + arcm.getAction());
            }
        }

        
    }

}
