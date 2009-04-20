/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.sharedstatetest.server.recorder;

import com.sun.sgs.app.AppContext;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.Recorder;
import com.sun.mpk20.voicelib.app.RecorderSetup;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.sgs.app.ManagedReference;
import com.sun.voip.client.connector.CallStatus;
import java.io.File;
import java.io.FilenameFilter;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapListenerSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.modules.sharedstatetest.common.recorder.AudioRecorderCellServerState;
import org.jdesktop.wonderland.modules.sharedstatetest.common.recorder.RecorderConstants;
import org.jdesktop.wonderland.modules.sharedstatetest.common.recorder.RecorderState;
import org.jdesktop.wonderland.modules.sharedstatetest.common.recorder.Tape;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;

/**
 *
 * @author Bernard Horan
 * @author Joe Provino
 * 
 */
public class AudioRecorderCellMO extends CellMO 
        implements ManagedCallStatusListener, SharedMapListenerSrv
{
    
    private static final Logger audioRecorderLogger = Logger.getLogger(AudioRecorderCellMO.class.getName());
    private static int INSTANCE_COUNT = 0;
    private int instanceNumber;
    private String callId;
    private Set<Tape> tapes = new HashSet<Tape>();
    private Tape selectedTape = null;
    private String recordingDirectory;
    private Recorder recorder;

    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sscRef;

    private ManagedReference<SharedMapSrv> statusMapRef;
    private ManagedReference<SharedMapSrv> tapeMapRef;

    public AudioRecorderCellMO() {
        super();
        addComponent(new MovableComponentMO(this));
        addComponent(new SharedStateComponentMO(this));

        instanceNumber = ++INSTANCE_COUNT;
        recordingDirectory = "/tmp/AudioRecordings/Recorder" + instanceNumber;

        createTapes();

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
            // get or create the shared maps we use
            SharedMapSrv statusMap = sscRef.get().get(RecorderConstants.STATUS_MAP);
            statusMap.addSharedMapListener(this);

            SharedMapSrv tapeMap = sscRef.get().get(RecorderConstants.TAPE_MAP);
            tapeMap.addSharedMapListener(this);

            // put the current status
            statusMap.put(RecorderConstants.STATUS, 
                          SharedString.valueOf(RecorderState.STOPPED.name()));

            // add all the tapes to the tape map
            for (Tape t : tapes) {
                tapeMap.put(t.getTapeName(), 
                            SharedBoolean.valueOf(!t.isFresh()));
            }

            // add the currently selected tape
            statusMap.put(RecorderConstants.CURRENT_TAPE,
                          SharedString.valueOf(selectedTape.getTapeName()));

            statusMapRef = AppContext.getDataManager().createReference(statusMap);
            tapeMapRef = AppContext.getDataManager().createReference(tapeMap);
        }
    }
        
    @Override
    public CellClientState getClientState(CellClientState cellClientState,
            WonderlandClientID clientID, ClientCapabilities capabilities)
    {

        audioRecorderLogger.fine("Getting client state");
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
            cellServerState = new AudioRecorderCellServerState();
        }
        return super.getServerState(cellServerState);
    }


    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        audioRecorderLogger.fine("Getting client cell class name");
        return "org.jdesktop.wonderland.modules.sharedstatetest.client.recorder.AudioRecorderCell";
    }

    private void createTapes() {
        //Add any existing files
        File tapeDir = new File(recordingDirectory);
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
                selectedTape = aTape; //Selected tape is last existing tape
                tapes.add(aTape);
            }

        if (selectedTape == null) {
            audioRecorderLogger.fine("no selected tape");
            selectedTape = new Tape("Untitled Tape");
            tapes.add(selectedTape);
        }
    }

    private String getRecorderFilename() {
        //MUST end in '.au'
        return selectedTape.getTapeName() + ".au";
    }

    public RecorderState getRecorderState() {
        SharedString status = statusMapRef.get().get(RecorderConstants.STATUS,
                                                     SharedString.class);
        if (status == null) {
            return RecorderState.STOPPED;
        }

        return RecorderState.valueOf(status.getValue());
    }

    /**
     * Set the current recording state of the audio recorder.
     * @param state the state of the recorder
     * @param owner the ID of the user who requested the state change,
     * or null if the change originated on the server
     */
    public void setRecorderState(RecorderState state,
                                 WonderlandClientID owner)
    {
        RecorderState curState = getRecorderState();
        if (curState == state) {
            // do nothing if we are switching to the same state
            return;
        }

        // if we are currently doing something (recording or playing), stop
        // doing it, since we are going to transition to a different state
        switch (curState) {
            case PLAYING:
                stopPlaying();
                break;
            case RECORDING:
                stopRecording();
                break;
        }

        // now start doing whatever we should be doing
        switch (state) {
            case PLAYING:
                setOwner(owner);
                startPlaying();
                break;
            case RECORDING:
                setOwner(owner);
                startRecording();
                break;
            case STOPPED:
                setOwner(null);
                break;
        }
    }

    private void setOwner(WonderlandClientID owner) {
        SharedString ownerID = null;
        if (owner != null) {
            ownerID = SharedString.valueOf(owner.getID().toString());
        }
        
        statusMapRef.get().put(RecorderConstants.OWNER, ownerID);
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

	setup.recordDirectory = recordingDirectory;

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

//    // TODO: make me work
//    private void processVolumeMessage(WonderlandClientID clientID, AudioRecorderCellChangeMessage ntcm) {
//        VoiceManager vm = AppContext.getManager(VoiceManager.class);
//
//        Player player = vm.getPlayer(callId);
//
//        if (player == null) {
//            audioRecorderLogger.warning("can't find player for " + callId);
//            return;
//        }
//
//        if (ntcm.isRecording()) {
//            audioRecorderLogger.fine("set recording volume of " + callId + " to " + ntcm.getVolume());
//
//            player.setMasterVolume(ntcm.getVolume());
//        } else {
//            /*
//             * Set the private volume for this client for playback
//             */
//            Spatializer spatializer = vm.getVoiceManagerParameters().livePlayerSpatializer;
//
//            spatializer.setAttenuator(ntcm.getVolume());
//
//        //audioRecorderLogger.info(clientName + " setting private playback volume for " + callId + " volume " + ntcm.getVolume());
//
//        //TODO need to get client player
//        //player.setPrivateSpatializer(clientName, spatializer);
//        }
//    }

    public boolean propertyChanged(SharedMapSrv map,
                                   WonderlandClientID sourceID,
                                   String key,
                                   SharedData prevData,
                                   SharedData curData)
    {
        if (map.getName().equals(RecorderConstants.TAPE_MAP)) {
            // a tape was added or removed
            return handleTapeChange(sourceID, key, prevData, curData);
        } else if (map.getName().equals(RecorderConstants.STATUS_MAP)) {
            // status change
            return handleStatusChange(sourceID, key, prevData, curData);
        } else {
            // this shouldn't happen
            audioRecorderLogger.warning("Change in unknown map: " +
                                        map.getName());
            return true;
        }
    }

    /**
     * Handle a change to the tape map
     * @param sourceID the source of the change
     * @param key the name of the tape
     * @param prevData the previous used state of this tape (or null if this
     * is a new tape)
     * @param curData the current used state of this tape (or null if the
     * tape was removed)
     * @return true to send this updated to all clients, or false to block it
     */
    private boolean handleTapeChange(WonderlandClientID sourceID,
                                     String key, SharedData prevData,
                                     SharedData curData)
    {
        if (prevData == null) {
            // this is a new tape
            Tape newTape = new Tape(key);
            tapes.add(newTape);
        } else if (curData == null) {
            // this tape was removed
            Tape removeTape = new Tape(key);
            tapes.remove(removeTape);
        } else {
            // the used state of the tape changed
            Tape modTape = findTape(key);
            if (((SharedBoolean) curData).getValue()) {
                modTape.setUsed();
            }
        }

        return true;
    }

    private Tape findTape(String name) {
        for (Tape t : tapes) {
            if (t.getTapeName().equals(name)) {
                return t;
            }
        }

        return null;
    }

    private boolean handleStatusChange(WonderlandClientID sourceID,
                                       String key, SharedData prevData,
                                       SharedData curData)
    {
        if (key.equals(RecorderConstants.STATUS)) {
            // change the state of the recorder
            String statusStr = ((SharedString) curData).getValue();
            RecorderState state = RecorderState.valueOf(statusStr);
            setRecorderState(state, sourceID);
        } else if (key.equals(RecorderConstants.CURRENT_TAPE)) {
            // change the currently playing tape
            String tapeStr = ((SharedString) curData).getValue();
            Tape tape = findTape(tapeStr);
            if (tape == null) {
                // couldn't find the tape...
                audioRecorderLogger.warning("Select unknown tape " + tapeStr);
                return false;
            }

            selectedTape = tape;
        } else if (key.equals(RecorderConstants.OWNER)) {
            // users cannot change the owner, only the server can
            return false;
        }

        // all set
        return true;
    }

    public void callStatusChanged(CallStatus status) {
        logger.warning("Got call status " + status);

        switch(status.getCode()) {
            case CallStatus.TREATMENTDONE:
                // perform the action
                setRecorderState(RecorderState.STOPPED, null);

                // notify the map
                statusMapRef.get().put(RecorderConstants.STATUS,
                                       SharedString.valueOf(RecorderState.STOPPED.name()));
                break;
        }

    }
}
