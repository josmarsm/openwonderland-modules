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
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.StationaryCellGLO;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderMessage;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderMessage.RecorderAction;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderCellMessage;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.VoiceHandler;
import com.sun.mpk20.voicelib.impl.app.VoiceHandlerImpl;
import java.io.IOException;
import java.util.logging.Logger;


public class AudioRecorderGLO extends StationaryCellGLO 
        implements BeanSetupGLO, CellMessageListener {
    private static final Logger logger = Logger.getLogger(AudioRecorderGLO.class.getName()); 
    
    private double[] rotation;
    private double[] origin;
    private double scale;

    private BasicCellGLOSetup<AudioRecorderCellSetup> setup;
            
    public AudioRecorderGLO() {
        this(null, null);
    }
    
    public AudioRecorderGLO(Bounds bounds, Matrix4d center) {
        super(bounds, center); 
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

    public void receivedMessage(ClientSession client, CellMessage message) {
        AudioRecorderCellMessage ntcm = (AudioRecorderCellMessage) message;

	if (ntcm.getAction().equals(RecorderAction.SETUP_RECORDER)) {
            setRecording(ntcm.isRecording());
            setPlaying(ntcm.isPlaying());
            getSetupData().setUserName(ntcm.getUserName());

            // send a message to all clients except the sender to notify of 
            // the updated selection
            AudioRecorderMessage msg = new AudioRecorderMessage(getSetupData().isRecording(), 
		getSetupData().isPlaying(), getSetupData().getUserName());

            Set<ClientSession> sessions = new HashSet<ClientSession>(getCellChannel().getSessions());
            sessions.remove(client);
            getCellChannel().send(sessions, msg.getBytes());
	} else if (ntcm.getAction().equals(RecorderAction.SET_VOLUME)) {
	    if (ntcm.isRecording()) {
		logger.warning("set recording volume of " + getCellID().toString()
		    + " to " + ntcm.getVolume());

		getVoiceHandler().setMasterVolume(getCellID().toString(), ntcm.getVolume());
		/*
		 * XXX Need to send message to all clients so volume is updated.
		 */
	    } else {
	        /*
	         * Set the private volume for this client for playback
	         */
	        String clientName = client.getName();

                DefaultSpatializer spatializer = new DefaultSpatializer();

                spatializer.setAttenuator(ntcm.getVolume());

	        logger.warning(clientName + " setting private playback volume for "
                    + getCellID().toString() + " volume " + ntcm.getVolume());

                getVoiceHandler().setPrivateSpatializer(clientName, getCellID().toString(), 
		    spatializer);
	    }
	}
    }
    
    /**
     * Set up the properties of this cell GLO from a JavaBean.  After calling
     * this method, the state of the cell GLO should contain all the information
     * represented in the given cell properties file.
     *
     * @param setup the Java bean to read setup information from
     */
    public void setupCell(CellGLOSetup setupData) {
        setup = (BasicCellGLOSetup<AudioRecorderCellSetup>) setupData;

	rotation = setup.getRotation();
	origin = setup.getOrigin();
	scale = setup.getScale();
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
        if (!getSetupData().isPlaying()) {
            //Not already playing
            if (isPlaying) {
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
            getVoiceHandler().setupRecorder(getCellID().toString(), currentPosition.getX(), currentPosition.getY(), currentPosition.getZ(), "/tmp");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void startPlaying() {
        logger.info("Start Playing");
        try {
            getVoiceHandler().playRecording(getCellID().toString(), "test.au");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void startRecording() {
        logger.info("Start Recording");
        try {
            getVoiceHandler().startRecording(getCellID().toString(), "test.au");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void stopRecording() {
        logger.info("Stop Recording");
        try {
            getVoiceHandler().stopRecording(getCellID().toString());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private VoiceHandler getVoiceHandler() {
        return VoiceHandlerImpl.getInstance();
    }
    
}
