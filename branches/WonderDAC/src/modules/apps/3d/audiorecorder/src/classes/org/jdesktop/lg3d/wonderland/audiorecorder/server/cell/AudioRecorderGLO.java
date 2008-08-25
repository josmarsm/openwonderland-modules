/**
 * Project Looking Glass
 * 
 * $RCSfile: AudioRecorderGLO.java,v $
 * 
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 * 
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * $Revision: 1.1.2.5 $
 * $Date: 2008/02/07 10:23:09 $
 * $State: Exp $ 
 */

package org.jdesktop.lg3d.wonderland.audiorecorder.server.cell;

import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
import java.util.HashSet;
import java.util.Set;
import javax.media.j3d.Bounds;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderCellMessage;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderCellSetup;
import org.jdesktop.lg3d.wonderland.config.common.WonderlandConfig;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.StationaryCellGLO;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderMessage;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderCellMessage;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.VoiceHandler;
import com.sun.mpk20.voicelib.impl.app.VoiceHandlerImpl;
import java.io.IOException;
import java.util.logging.Logger;

import org.jdesktop.lg3d.wonderland.config.common.WonderlandConfig;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;

import com.sun.sgs.app.AppContext;  // TW
import com.sun.voip.client.connector.CallStatus;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.Tape;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellAccessControl;  // TW
import org.jdesktop.lg3d.wonderland.darkstar.server.ClientIdentityManager;  // TW
import org.jdesktop.lg3d.wonderland.darkstar.server.UserGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.auth.WonderlandIdentity;  // TW
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.AvatarCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.UserCellCacheGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOHelper;  // TW

/**
 * Represents the audio recorder on the server and communicates with the voice bridge.
 * @author Bernard Horan
 * @author Joe Provino
 */
public class AudioRecorderGLO extends StationaryCellGLO 
        implements BeanSetupGLO, CellMessageListener, 
	ManagedCallStatusListener {

    private static final Logger logger = Logger.getLogger(AudioRecorderGLO.class.getName()); 
    private static int INSTANCE_COUNT = 0;
    
    private double[] rotation;
    private double[] origin;
    private double scale;
    private int instanceNumber;
    private String callId;
    private Set<Tape> tapes = new HashSet<Tape>();
    private Tape selectedTape;
    private BasicCellGLOSetup<AudioRecorderCellSetup> setup;
            
    /**
     * Create a new audio recorder GLO with default bounds and origin
     */
    public AudioRecorderGLO() {
        this(null, null);
        
    }
    
    /** 
     * Creates a new instance of AudioRecorderGLO
     * @param bounds the bounds of this cell
     * @param center the origin of this cell
     */
    public AudioRecorderGLO(Bounds bounds, Matrix4d center) {
        super(bounds, center); 

	getVoiceHandler().addCallStatusListener(this);
        selectedTape = new Tape("Untitled Tape");
        tapes.add(selectedTape);
        instanceNumber = ++INSTANCE_COUNT;
        logger.info("Recorder filename: " + getRecorderFilename());

	callId = this.toString();
        
        int ix;

        if ((ix = callId.indexOf("@")) >= 0) {
            callId = callId.substring(ix + 1);
        }
    }
        
    
    @Override
    public void openChannel() {
        this.openDefaultChannel();
    }
    
    public String getClientCellClassName() {
        return "org.jdesktop.lg3d.wonderland.audiorecorder.client.cell.AudioRecorderCell";
    }
    
    public AudioRecorderCellSetup getSetupData() {
        return setup.getCellSetup();
    }

    @Override
    public void pingClients(){  // TW
        // As this cell's privileges are changed, we need
        // to ensure that everyone stays in sync.  Stop
        // any recording/playing that's taking place to
        // keep everyone in the same state.
        // TW
        AudioRecorderMessage msg = null;
            
        if (getSetupData().isRecording()) {  // TW
            setRecording(false);  // TW
            msg = AudioRecorderMessage.recordingMessage(false, getSetupData().getUserName());  // TW
        }
        else if (getSetupData().isPlaying()) {  // TW
            setPlaying(false);  // TW
            msg = AudioRecorderMessage.playingMessage(false, getSetupData().getUserName());  // TW
        }

        Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());  // TW
        
        // Was something happening with the recorder?  If so, send
        // our message.
        // TW
        if (msg != null)  // TW
            getCellChannel().send(sessions, msg.getBytes());  // TW
            
        // Construct a (very) simple message...  TW
        msg = AudioRecorderMessage.pingMessage();  // TW
            
        logger.info("Privileges have changed; pinging all clients....");  // TW

        // Ping all of our clients.
        getCellChannel().send(sessions, msg.getBytes());  // TW        
    }

    @Override
    public void pingClient(ClientSession client){  // TW
        AudioRecorderMessage msg = null;
            
        // Construct a (very) simple message...  TW
        msg = AudioRecorderMessage.pingMessage();  // TW
            
        logger.info("Privileges have changed; pinging a client...");  // TW

        if (getCellChannel().getSessions().contains(client))  // TW
            // Ping our client.
            getCellChannel().send(client, msg.getBytes());  // TW
        else
            logger.severe("Attempting to ping a client outside of this GLO's channel:  " + client.getName());        
    }
    
    public void receivedMessage(ClientSession client, CellMessage message) {
        AudioRecorderCellMessage arcm = (AudioRecorderCellMessage) message;

        // Obtain a UserGLO object for use later.
        // TW
        UserGLO user = UserGLO.getUserGLO(client.getName());
        
        // Does the user have interact permission?  If not, then they
        // can't even see the PDF cell.  If they have control, take
        // it away, then drop them like yesterday's cheese!
        // TW
        if (!CellAccessControl.canInteract(user.getUserIdentity(),this)){  // TW
                         
            // Get the client to re-evaluate their surroundings.
            // TW
            user.getAvatarCellRef().get(AvatarCellGLO.class).getUserCellCacheRef().
                                    get(UserCellCacheGLO.class).refactor();
            
            return;  // TW
        }
        
        // Does the user have permissions to alter the Audio Recorder?  If not,
        // exit from this method.
        //
        // TW            
        if (!CellAccessControl.canAlter(user.getUserIdentity(),this)) {  // TW
            
            // Since this client does not have alter permissions, send
            // them a message to that effect.  The client can then
            // work to prevent the end user from making any changes
            // to its local Audio Recorder cell.
            //
            // TW
            // Construct a (very) simple AudioRecorderMessage first...  TW
            AudioRecorderMessage msg = AudioRecorderMessage.noAlterMessage();  // TW
            
            // Send this off to the client.  TW
            logger.fine("AudioRecorder GLO broadcasting NO_ALTER_PERM msg: " + msg);  // TW
            getCellChannel().send(client, msg.getBytes());  // TW
            
            return;  // TW
        }
        else {
            // Since this client has alter permissions, send
            // them a message to that effect.
            //
            // TW
            // Construct a (very) simple AudioRecorderMessage first...  TW
            AudioRecorderMessage msg = AudioRecorderMessage.alterMessage();  // TW
            
            // Send this off to the client.  TW
            logger.fine("AudioRecorder GLO broadcasting ALTER_PERM msg: " + msg);  // TW
            getCellChannel().send(client, msg.getBytes());  // TW            
        }
        
        switch (arcm.getAction()) {
            case PLAY:
                processPlayMessage(client, arcm);
                break;
            case RECORD:
                processRecordMessage(client, arcm);
                break;
            case SET_VOLUME:
                processVolumeMessage(client, arcm);
                break;
            case TAPE_USED:
                processTapeUsedMessage(client, arcm);
                break;
            case TAPE_SELECTED:
                processTapeSelectedMessage(client, arcm);
                break;
            case NEW_TAPE:
                processNewTapeMessage(client, arcm);
                break;
            case PING:
                break;
        }
    }

    private void processNewTapeMessage(ClientSession client, AudioRecorderCellMessage arcm) {
        String tapeName = arcm.getTapeName();
        Tape newTape = new Tape(tapeName);
        tapes.add(newTape);
        // send a message to all clients except the sender to notify of 
        // the updated tape
        AudioRecorderMessage msg = AudioRecorderMessage.newTapeMessage(tapeName);

        Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
        sessions.remove(client);
        getCellChannel().send(sessions, msg.getBytes());               
    }
    
    
    
    private void processTapeSelectedMessage(ClientSession client, AudioRecorderCellMessage arcm) {
        String tapeName = arcm.getTapeName();
        for (Tape aTape : tapes) {
            if(aTape.getTapeName().equals(tapeName)) {
                selectedTape = aTape;
                getSetupData().setSelectedTape(selectedTape);  
                // send a message to all clients except the sender to notify of 
                // the updated tape
                AudioRecorderMessage msg = AudioRecorderMessage.tapeSelectedMessage(tapeName);

                Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
                sessions.remove(client);
                getCellChannel().send(sessions, msg.getBytes());
            }
        }      
    }
    
    private void processTapeUsedMessage(ClientSession client, AudioRecorderCellMessage arcm) {
        String tapeName = arcm.getTapeName();
        for (Tape aTape : tapes) {
            if(aTape.getTapeName().equals(tapeName)) {
                aTape.setUsed();
                // send a message to all clients except the sender to notify of 
                // the updated tape
                AudioRecorderMessage msg = AudioRecorderMessage.tapeUsedMessage(tapeName);

                Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
                sessions.remove(client);
                getCellChannel().send(sessions, msg.getBytes());
            }
        }       
    }
    
    private void processPlayMessage(ClientSession client, AudioRecorderCellMessage ntcm) {
        setPlaying(ntcm.isPlaying());
        getSetupData().setUserName(ntcm.getUserName());

        // send a message to all clients except the sender to notify of 
        // the updated selection
        AudioRecorderMessage msg = AudioRecorderMessage.playingMessage(getSetupData().isPlaying(), getSetupData().getUserName());

        Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
        sessions.remove(client);
        getCellChannel().send(sessions, msg.getBytes());
    }
    
    private void processRecordMessage(ClientSession client, AudioRecorderCellMessage arcm) {
        setRecording(arcm.isRecording());
        getSetupData().setUserName(arcm.getUserName());

        // send a message to all clients except the sender to notify of 
        // the updated selection
        AudioRecorderMessage msg = AudioRecorderMessage.recordingMessage(getSetupData().isRecording(), getSetupData().getUserName());

        Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
        sessions.remove(client);
        getCellChannel().send(sessions, msg.getBytes());
    }
    
    private void processVolumeMessage(ClientSession client, AudioRecorderCellMessage ntcm) {
        if (ntcm.isRecording()) {
            logger.info("set recording volume of " + callId + " to " + ntcm.getVolume());

            getVoiceHandler().setMasterVolume(callId, ntcm.getVolume());

            /*
             * The volume is global so send a message to all the clients.
             */
            //Not sure why we would do this, as this as we don't need to let other clients know (do we?)
            
            /*AudioRecorderMessage msg = new AudioRecorderMessage(getSetupData().isRecording(),
                    getSetupData().isPlaying(), getSetupData().getUserName(), arcm.getVolume());

            Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
            sessions.remove(client);
            getCellChannel().send(sessions, msg.getBytes());*/
        } else {
            /*
             * Set the private volume for this client for playback
             */
            String clientName = client.getName();

            DefaultSpatializer spatializer = new DefaultSpatializer();

            spatializer.setAttenuator(ntcm.getVolume());

            logger.info(clientName + " setting private playback volume for " + callId + " volume " + ntcm.getVolume());

            getVoiceHandler().setPrivateSpatializer(clientName, callId, spatializer);
        }
    }
    
    
    public void callStatusChanged(CallStatus status) {
	switch(status.getCode()) {
	case CallStatus.TREATMENTDONE:
            getSetupData().setPlaying(false);

            AudioRecorderMessage msg = AudioRecorderMessage.playbackDone();

	    /*
	     * Send message to all clients
	     */
            getCellChannel().send(getCellChannel().getSessions(), msg.getBytes());
	    break;
	}
    }

    /**
     * Set up the properties of this cell GLO from a JavaBean.  After calling
     * this method, the state of the cell GLO should contain all the information
     * represented in the given cell properties file.
     *
     * @param setupData contains the data to set up thie FLO
     */
    public void setupCell(CellGLOSetup setupData) {
        setup = (BasicCellGLOSetup<AudioRecorderCellSetup>) setupData;

	rotation = setup.getRotation();
	origin = setup.getOrigin();
	scale = setup.getScale();

	getSetupData().setBaseURL(WonderlandConfig.getBaseURL());
        getSetupData().setTapes(tapes);
        getSetupData().setSelectedTape(selectedTape);
  
        // Handle configuring this cell's discretionary access controls
        //
        // TW
        setCellAccessOwner(BasicCellGLOHelper.getCellAccessOwner(setup));  // TW
        setCellAccessGroup(BasicCellGLOHelper.getCellAccessGroup(setup));  // TW
        setCellAccessGroupPermissions(BasicCellGLOHelper.getCellAccessGroupPermissions(setup)); // TW
        setCellAccessOtherPermissions(BasicCellGLOHelper.getCellAccessOtherPermissions(setup)); // TW
        
        // Also, setup the name of the cell (this is passed to the
        // client for display in the WonderDAC GUI).
        // TW
        setCellName(BasicCellGLOHelper.getCellName(setup));  // TW   
        
        // And, setup the cell's width and height.  TW
        setCellWidth(BasicCellGLOHelper.getCellWidth(setup)); // TW
        setCellHeight(BasicCellGLOHelper.getCellHeight(setup)); // TW
    }

    @Override
    protected void addParentCell(ManagedReference parent) {
        super.addParentCell(parent);

        AxisAngle4d aa = new AxisAngle4d(rotation);
        Matrix3d rot = new Matrix3d();
        rot.set(aa);
        Vector3d localOrigin = new Vector3d(this.origin);

        Matrix4d o = new Matrix4d(rot, localOrigin, scale);
        setOrigin(o);

        if (setup.getBoundsType().equals("SPHERE")) {
            setBounds(createBoundingSphere(localOrigin, (float) setup.getBoundsRadius()));
        } else {
            throw new RuntimeException("Unimplemented bounds type");
        }

        setupRecorder();
    }
 
    /*
     * Called when the properties of a cell have changed.
     *
     * @param setup a Java bean with updated properties
     */
    public void reconfigureCell(CellGLOSetup setupData) {
        setupCell(setupData);
    }

    /**
     * Write the cell's current state to a JavaBean.
     * @return a JavaBean representing the current state
     */
    public CellGLOSetup getCellGLOSetup() {
        return new BasicCellGLOSetup<AudioRecorderCellSetup>(getBounds(),
                getOrigin(), getClass().getName(),
                getSetupData());
    }

    private void setPlaying(boolean isPlaying) {
        if (getSetupData().isPlaying()) {
            //Already playing
            if (!isPlaying) {
                stopPlaying();
            }
        } else {
            //Not playing
            if (isPlaying) {
                //Start playing
                startPlaying();
            }
        }
        getSetupData().setPlaying(isPlaying);
    }

    private void setRecording(boolean isRecording) {
        if (getSetupData().isRecording()) {
            //Already recording
            if (!isRecording) {
                //Stop recording
                stopRecording();
            }
        } else {
            //Not recording
            if (isRecording) {
                //Start recording
                startRecording();
            }
        }
        getSetupData().setRecording(isRecording);
        
    }

    private void setupRecorder() {
        Vector3d currentPosition = new Vector3d();
        getOriginWorld().get(currentPosition);

        try {
            getVoiceHandler().setupRecorder(callId, currentPosition.getX(), currentPosition.getY(), currentPosition.getZ(), "/tmp");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void startPlaying() {
        logger.info("Start Playing");
        try {
            getVoiceHandler().playRecording(callId, getRecorderFilename());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void startRecording() {
        logger.info("Start Recording");
        try {
            getVoiceHandler().startRecording(callId, getRecorderFilename());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void stopPlaying() {
        try {
            logger.info("Stop Playing");
            getVoiceHandler().stopPlayingRecording(callId, getRecorderFilename());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void stopRecording() {
        logger.info("Stop Recording");
        try {
            getVoiceHandler().stopRecording(callId);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private VoiceHandler getVoiceHandler() {
        return VoiceHandlerImpl.getInstance();
    }
    
    private String getRecorderFilename() {
        //MUST end in '.au'
        //return getClass().getSimpleName() + instanceNumber + ".au";
        return selectedTape.getTapeName() + ".au";
    }
    
}
