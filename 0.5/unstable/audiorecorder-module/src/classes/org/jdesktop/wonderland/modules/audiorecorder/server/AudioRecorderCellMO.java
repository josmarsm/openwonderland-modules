/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.audiorecorder.server;

import com.sun.sgs.app.AppContext;
import java.io.IOException;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.modules.audiorecorder.common.AudioRecorderCellChangeMessage;
import org.jdesktop.wonderland.modules.audiorecorder.common.AudioRecorderCellClientState;
import org.jdesktop.wonderland.modules.audiorecorder.common.Tape;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.Recorder;
import com.sun.mpk20.voicelib.app.RecorderSetup;
import com.sun.mpk20.voicelib.app.Spatializer;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.voip.client.connector.CallStatus;
import java.io.File;
import java.io.FilenameFilter;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
import org.jdesktop.wonderland.modules.audiorecorder.common.AudioRecorderCellServerState;

/**
 *
 * @author Bernard Horan
 * @author Joe Provino
 * 
 */
public class AudioRecorderCellMO extends CellMO implements ManagedCallStatusListener {
    
    private static final Logger audioRecorderLogger = Logger.getLogger(AudioRecorderCellMO.class.getName());
    private static int INSTANCE_COUNT = 0;
    private int instanceNumber;
    private String callId;
    private Recorder recorder;
    private AudioRecorderCellServerState serverState;

    public AudioRecorderCellMO() {
        super();
        addComponent(new MovableComponentMO(this));
        serverState = new AudioRecorderCellServerState();
        instanceNumber = ++INSTANCE_COUNT;
        serverState.setRecordingDirectory("/tmp/AudioRecordings/Recorder" + instanceNumber);
        createTapes();
        serverState.setPlaying(false);
        serverState.setRecording(false);
        callId = getCellID().toString();
        int ix;

        if ((ix = callId.indexOf("@")) >= 0) {
            callId = callId.substring(ix + 1);
        }
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        if (live) {
            ChannelComponentMO channel = getChannel();
            if (channel == null) {
                throw new IllegalStateException("Cell does not have a ChannelComponent");
            }
            //Add the message receiver to the channel
            channel.addMessageReceiver(AudioRecorderCellChangeMessage.class,
                    (ChannelComponentMO.ComponentMessageReceiver) new AudioRecorderCellMOMessageReceiver(this));
        } else {
            getChannel().removeMessageReceiver(AudioRecorderCellChangeMessage.class);
        }
    }
        
    @Override
    public CellClientState getClientState(CellClientState cellClientState,
            WonderlandClientID clientID, ClientCapabilities capabilities) {

        audioRecorderLogger.fine("Getting client state");

        if (cellClientState == null) {
            cellClientState = new AudioRecorderCellClientState(serverState.getTapes(), serverState.getSelectedTape(), serverState.isPlaying(), serverState.isRecording(), serverState.getUserName());
        }

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setServerState(CellServerState cellServerState) {
        super.setServerState(cellServerState);

        // Check to see if the CellServerState has a PositionComponentServerState
        // and takes it origin. This will only work upon the initial creation
        // of the cell and not when the cell is moved at all. This class should
        // add a transform change listener to listen for changes in the cell
        // origin after the cell has been created.
        CellComponentServerState state = cellServerState.getComponentServerState(PositionComponentServerState.class);
        if (state != null) {
            setupRecorder(((PositionComponentServerState) state).getOrigin());
        }
    }

    @Override
    public CellServerState getServerState(CellServerState cellServerState) {
        if (cellServerState == null) {
            cellServerState = serverState;
        }
        return super.getServerState(cellServerState);
    }


    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        audioRecorderLogger.fine("Getting client cell class name");
        return "org.jdesktop.wonderland.modules.audiorecorder.client.AudioRecorderCell";
    }

    private void createTapes() {
        //Add any existing files
        File tapeDir = new File(serverState.getRecordingDirectory());
        if (!tapeDir.exists()) {
            audioRecorderLogger.fine("Non existent directory: " + tapeDir);
            tapeDir.mkdirs();
        }
        String[] tapeFiles = tapeDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".au");
            }
        });
        for (int i = 0; i < tapeFiles.length; i++) {
                String string = tapeFiles[i];
                audioRecorderLogger.fine("tapeFile: " + string);
                int index = string.indexOf(".au");
                Tape aTape = new Tape(string.substring(0, index));
                aTape.setUsed();
                serverState.setSelectedTape(aTape); //Selected tape is last existing tape
                serverState.addTape(aTape);
            }

        if (serverState.getSelectedTape() == null) {
            audioRecorderLogger.fine("no selected tape");
            serverState.setSelectedTape(new Tape("Untitled Tape"));
            serverState.addTape(serverState.getSelectedTape());
        }
    }

    private String getRecorderFilename() {
        //MUST end in '.au'
        return serverState.getSelectedTape().getTapeName() + ".au";
    }

    private void setPlaying(boolean p) {
        if (serverState.isPlaying()) {
            //Already playing
            if (!p) {
                stopPlaying();
            }
        } else {
            //Not playing
            if (p) {
                //Start playing
                startPlaying();
            }
        }
        serverState.setPlaying(p);
    }

    private void setRecording(boolean r) {
        if (serverState.isRecording()) {
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

    private void setupRecorder(Origin origin) {
//       Vector3d currentPosition = new Vector3d();
//       getOriginWorld().get(currentPosition);
//

	VoiceManager vm = AppContext.getManager(VoiceManager.class);

    vm.addCallStatusListener(this, callId);

	RecorderSetup setup = new RecorderSetup();

	setup.x = origin.x;
	setup.y = origin.y;
	setup.z = origin.z;

	logger.info("Recorder Origin is " + "(" 
	    + origin.x + ":" + origin.y + ":" + origin.z + ")");

	setup.spatializer = vm.getVoiceManagerParameters().livePlayerSpatializer;

	setup.recordDirectory = serverState.getRecordingDirectory();

        try {
            recorder = vm.createRecorder(callId, setup);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void startPlaying() {
        audioRecorderLogger.fine("Start Playing");
        try {
            recorder.playRecording(getRecorderFilename());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void startRecording() {
        audioRecorderLogger.fine("Start Recording");
        try {
            recorder.startRecording(getRecorderFilename());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void stopPlaying() {
        audioRecorderLogger.fine("Stop Playing");
        try {
            recorder.stopPlayingRecording();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void stopRecording() {
        audioRecorderLogger.fine("Stop Recording");
        try {
            recorder.stopRecording();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void processPlayMessage(WonderlandClientID clientID, AudioRecorderCellChangeMessage arcm) {
        setPlaying(arcm.isPlaying());
        serverState.setUserName(arcm.getUserName());

        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }

    private void processRecordMessage(WonderlandClientID clientID, AudioRecorderCellChangeMessage arcm) {
        setRecording(arcm.isRecording());
        serverState.setUserName(arcm.getUserName());

        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }

    private void processVolumeMessage(WonderlandClientID clientID, AudioRecorderCellChangeMessage ntcm) {
	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	Player player = vm.getPlayer(callId);

	if (player == null) {
	    audioRecorderLogger.warning("can't find player for " + callId);
	    return;
	}

        if (ntcm.isRecording()) {
            audioRecorderLogger.fine("set recording volume of " + callId + " to " + ntcm.getVolume());

            player.setMasterVolume(ntcm.getVolume());
        } else {
            /*
             * Set the private volume for this client for playback
             */
            Spatializer spatializer = vm.getVoiceManagerParameters().livePlayerSpatializer;

            spatializer.setAttenuator(ntcm.getVolume());

            //audioRecorderLogger.info(clientName + " setting private playback volume for " + callId + " volume " + ntcm.getVolume());

	    //TODO need to get client player
            //player.setPrivateSpatializer(clientName, spatializer);
        }
    }

    private void processTapeUsedMessage(WonderlandClientID clientID, AudioRecorderCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        for (Tape aTape : serverState.getTapes()) {
            if(aTape.getTapeName().equals(tapeName)) {
                aTape.setUsed();
                // send a message to all clients
                getChannel().sendAll(clientID, arcm);
            }
        }
    }

    private void processTapeSelectedMessage(WonderlandClientID clientID, AudioRecorderCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        for (Tape aTape : serverState.getTapes()) {
            if(aTape.getTapeName().equals(tapeName)) {
                serverState.setSelectedTape(aTape);
                // send a message to all clients
                getChannel().sendAll(clientID, arcm);
            }
        }
    }

    private void processNewTapeMessage(WonderlandClientID clientID, AudioRecorderCellChangeMessage arcm) {
        String tapeName = arcm.getTapeName();
        Tape newTape = new Tape(tapeName);
        serverState.addTape(newTape);
        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }

    private ChannelComponentMO getChannel() {
        return getComponent(ChannelComponentMO.class);
    }

    private static class AudioRecorderCellMOMessageReceiver extends AbstractComponentMessageReceiver {
        public AudioRecorderCellMOMessageReceiver(AudioRecorderCellMO cellMO) {
            super(cellMO);
        }
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            //audioRecorderLogger.info("message received: " + message + ", ID: " + clientID);
            AudioRecorderCellMO cellMO = (AudioRecorderCellMO)getCell();
            AudioRecorderCellChangeMessage arcm = (AudioRecorderCellChangeMessage)message;
            switch (arcm.getAction()) {
            case PLAY:
                cellMO.processPlayMessage(clientID, arcm);
                break;
            case RECORD:
                cellMO.processRecordMessage(clientID, arcm);
                break;
            case SET_VOLUME:
                cellMO.processVolumeMessage(clientID, arcm);
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
    }

    public void callStatusChanged(CallStatus status) {
	logger.fine("Got call status " + status);

        switch(status.getCode()) {
        case CallStatus.TREATMENTDONE:
            setPlaying(false);

            /*
             * Send message to all clients
             */
	    getChannel().sendAll(null, 
		AudioRecorderCellChangeMessage.playbackDoneMessage(getCellID()));
            break;
        }

    }

}
