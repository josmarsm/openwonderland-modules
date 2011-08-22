/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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

package org.jdesktop.wonderland.modules.audiorecorder.server;

import com.jme.math.Vector3f;
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
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
import com.sun.voip.client.connector.CallStatus;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.modules.audiorecorder.common.AudioRecorderCellServerState;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;

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
    private String recordingDirectory;
    private String selectedTapeName;
    private final Set<RecordingStoppedListener> recordingStoppedListeners =
            new LinkedHashSet<RecordingStoppedListener>();

    public AudioRecorderCellMO() {
        super();
        addComponent(new MovableComponentMO(this));
        serverState = new AudioRecorderCellServerState();
        instanceNumber = ++INSTANCE_COUNT;
        recordingDirectory = "/tmp/AudioRecordings/Recorder" + instanceNumber;
        selectedTapeName = null;
        serverState.setPlaying(false);
        serverState.setRecording(false);
        callId = getCellID().toString();
        int ix = callId.indexOf("@");
        if (ix >= 0) {
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
            cellClientState = new AudioRecorderCellClientState(serverState.isPlaying(), selectedTapeName, serverState.isRecording(), serverState.getUserName());
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
            setupRecorder(((PositionComponentServerState) state).getTranslation());
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

    public void addRecordingStoppedListener(RecordingStoppedListener listener) {
        recordingStoppedListeners.add(listener);
    }

    public void removeRecordingStoppedListener(RecordingStoppedListener listener) {
        recordingStoppedListeners.remove(listener);
    }

    private String getRecorderFilename(Tape aTape) {
        //MUST end in '.au'
        return recordingDirectory + File.separator + aTape.getTapeName() + ".au";
    }

    private void setPlaying(boolean p, Tape aTape) {
        if (serverState.isPlaying()) {
            //Already playing
            if (!p) {
                stopPlaying();
            }
        } else {
            //Not playing
            if (p) {
                //Start playing
                startPlaying(aTape);
            }
        }
        serverState.setPlaying(p);
    }

    private void setRecording(boolean r, Tape aTape) {
        if (serverState.isRecording()) {
            //Already recording
            if (!r) {
                //Stop recording
                stopRecording(aTape);
            }
        } else {
            //Not recording
            if (r) {
                //Start recording
                startRecording(aTape);
            }
        }
        serverState.setRecording(r);

    }

    private void setupRecorder(Vector3f origin) {
        VoiceManager vm = AppContext.getManager(VoiceManager.class);

        vm.addCallStatusListener(this, callId);

        RecorderSetup setup = new RecorderSetup();

        setup.x = origin.x;
        setup.y = origin.y;
        setup.z = origin.z;

        logger.fine("Recorder Origin is " + "(" + origin.x + ":" + origin.y + ":" + origin.z + ")");

        setup.spatializer = vm.getVoiceManagerParameters().livePlayerSpatializer;

        try {
            recorder = vm.createRecorder(callId, setup);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void startPlaying(Tape aTape) {
        audioRecorderLogger.info("Start Playing: " + aTape.getURL().toString());
        try {
            // convert from wlcontent:// URL to a local URL
            String urlStr = aTape.getURL().toString();
            if (urlStr.startsWith("wlcontent://")) {
                urlStr = urlStr.substring("wlcontent://".length());
                urlStr = System.getProperty("wonderland.web.server.url") +
                         "webdav/content/" + urlStr;
            }
            
            recorder.playRecording(urlStr);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void startRecording(Tape aTape) {
        audioRecorderLogger.fine("Start Recording");
        try {
            recorder.startRecording(getRecorderFilename(aTape));
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

    private void stopRecording(Tape aTape) {
        audioRecorderLogger.fine("Stop Recording");
        try {
            recorder.stopRecording();
            notifyRecordingStoppedListeners(aTape);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void processPlayMessage(WonderlandClientID clientID, AudioRecorderCellChangeMessage arcm) {
        audioRecorderLogger.info("message: " + arcm.getDescription());
        setPlaying(arcm.isPlaying(), arcm.getTape());
        serverState.setUserName(arcm.getUserName());

        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }

    private void processRecordMessage(WonderlandClientID clientID, AudioRecorderCellChangeMessage arcm) {
        setRecording(arcm.isRecording(), arcm.getTape());
        serverState.setUserName(arcm.getUserName());

        // send a message to all clients
        getChannel().sendAll(clientID, arcm);
    }

    private void processTapeSelectedMessage(WonderlandClientID clientID, AudioRecorderCellChangeMessage arcm) {
        audioRecorderLogger.info("selected tape: " + arcm.getTapeName());
        selectedTapeName = arcm.getTapeName();
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

    private ChannelComponentMO getChannel() {
        return getComponent(ChannelComponentMO.class);
    }

    private void notifyRecordingStoppedListeners(Tape aTape) {
        for (RecordingStoppedListener listener : recordingStoppedListeners) {
            listener.recordingStopped(aTape);
        }
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
            case TAPE_SELECTED:
                cellMO.processTapeSelectedMessage(clientID, arcm);
                break;
            }
        }

        @Override
        protected void postRecordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            AudioRecorderCellChangeMessage arcm = (AudioRecorderCellChangeMessage) message;
            UserMO user = UserManager.getUserManager().getUser(clientID);
            RecorderManager.getDefaultManager().recordMetadata(message,  arcm.getDescription() + " initiated by " + user.getUsername() + "[" + user.getIdentity().getFullName() + "]");
        }
    }

    public void callStatusChanged(CallStatus status) {
	logger.fine("Got call status " + status);

        switch(status.getCode()) {
        case CallStatus.TREATMENTDONE:
            setPlaying(false, null);

            /*
             * Send message to all clients
             */
	    getChannel().sendAll(null, 
		AudioRecorderCellChangeMessage.playbackDone(getCellID()));
            break;
        }

    }

}
