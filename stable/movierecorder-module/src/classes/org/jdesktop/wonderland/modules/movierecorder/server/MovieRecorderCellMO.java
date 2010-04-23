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

package org.jdesktop.wonderland.modules.movierecorder.server;

import com.jme.math.Vector3f;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.sgs.app.AppContext;
import com.sun.voip.client.connector.CallStatus;
import java.io.IOException;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.modules.movierecorder.common.MovieRecorderCellChangeMessage;
import org.jdesktop.wonderland.modules.movierecorder.common.MovieRecorderCellClientState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import com.sun.mpk20.voicelib.app.Recorder;
import com.sun.mpk20.voicelib.app.RecorderSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import java.util.logging.Level;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.modules.movierecorder.common.MovieRecorderCellServerState;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;

/**
 *
 * @author Bernard Horan
 * @author Joe Provino
 * 
 */
public class MovieRecorderCellMO extends CellMO implements ManagedCallStatusListener {
    
    private static final Logger movieRecorderLogger = Logger.getLogger(MovieRecorderCellMO.class.getName());
    private static int INSTANCE_COUNT = 0;
    private String callId;
    private Recorder recorder;
    private MovieRecorderCellServerState serverState;
    private WonderlandClientID recordingClientID;

    public MovieRecorderCellMO() {
        super();
        addComponent(new MovableComponentMO(this));
        serverState = new MovieRecorderCellServerState();
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
            channel.addMessageReceiver(MovieRecorderCellChangeMessage.class,
                    (ChannelComponentMO.ComponentMessageReceiver) new MovieRecorderCellMOMessageReceiver(this));
        } else {
            getChannel().removeMessageReceiver(MovieRecorderCellChangeMessage.class);
        }
    }
        
    @Override
    public CellClientState getClientState(CellClientState cellClientState,
            WonderlandClientID clientID, ClientCapabilities capabilities) {

        movieRecorderLogger.fine("Getting client state");

        if (cellClientState == null) {
            cellClientState = new MovieRecorderCellClientState(serverState.isRecording());
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
        movieRecorderLogger.fine("Getting client cell class name");
        return "org.jdesktop.wonderland.modules.movierecorder.client.MovieRecorderCell";
    }

    private void setRecording(boolean r, String recordingName) {
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
                startRecording(recordingName);
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

        movieRecorderLogger.info("Movie recorder Origin is " + "(" + origin.x + ":" + origin.y + ":" + origin.z + ")");

        setup.spatializer = vm.getVoiceManagerParameters().livePlayerSpatializer;

        movieRecorderLogger.info("movie recorder setup: " + setup);

        try {
            recorder = vm.createRecorder(callId, setup);
            movieRecorderLogger.info("Created new recorder: " + recorder);
        } catch (IOException e) {
            movieRecorderLogger.log(Level.SEVERE, "Failed to create recorder", e);
        }
    }

    private void startRecording(String recordingName) {
        movieRecorderLogger.info("Start Recording: " + recordingName);
        try {
            recorder.startRecording(recordingName + ".au");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void stopRecording() {
        movieRecorderLogger.info("Stop Recording");
        try {
            recorder.stopRecording();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void processRecordMessage(WonderlandClientID clientID, MovieRecorderCellChangeMessage arcm) {
        setRecording(arcm.isRecording(), arcm.getRecordingName());
        if (arcm.isRecording()) {
            // send a message to all clients
            getChannel().sendAll(clientID, arcm);
            recordingClientID = clientID;
        }
        //We don't send a message to all clients immediately we stop recording
        //We send it when we hear from the voicebridge that the recording is done
        //see callStatusChanged)(
    }

    private ChannelComponentMO getChannel() {
        return getComponent(ChannelComponentMO.class);
    }

    public void callStatusChanged(CallStatus status) {
        movieRecorderLogger.info("Got call status " + status);
        if (status.getCode() == CallStatus.RECORDERDONE) {
            //We can tell clients that the recording has finished
            movieRecorderLogger.info("Recording is done");
            MovieRecorderCellChangeMessage arcm = MovieRecorderCellChangeMessage.recordingMessage(getCellID(), null, false);
            getChannel().sendAll(recordingClientID, arcm);
            recordingClientID = null;
        }
    }

    private static class MovieRecorderCellMOMessageReceiver extends AbstractComponentMessageReceiver {
        public MovieRecorderCellMOMessageReceiver(MovieRecorderCellMO cellMO) {
            super(cellMO);
        }
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            //movieRecorderLogger.info("message received: " + message + ", ID: " + clientID);
            MovieRecorderCellMO cellMO = (MovieRecorderCellMO)getCell();
            MovieRecorderCellChangeMessage arcm = (MovieRecorderCellChangeMessage)message;
            switch (arcm.getAction()) {
            case RECORD:
                cellMO.processRecordMessage(clientID, arcm);
                break;
            }
        }

        @Override
        protected void postRecordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            MovieRecorderCellChangeMessage arcm = (MovieRecorderCellChangeMessage) message;
            UserMO user = UserManager.getUserManager().getUser(clientID);
            RecorderManager.getDefaultManager().recordMetadata(message,  arcm.getDescription() + " initiated by " + user.getUsername() + "[" + user.getIdentity().getFullName() + "]");
        }
    }   

}
