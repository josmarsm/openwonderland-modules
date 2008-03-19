/**
 * Project Looking Glass
 *
 * $RCSfile: AudioRecorderCell.java,v $
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
 * $State: Exp $
 * $Id$
 */

package org.jdesktop.lg3d.wonderland.audiorecorder.client.cell;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.vecmath.Point3d;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;

import javax.media.j3d.*;
import javax.vecmath.Vector3f;
import javax.vecmath.Matrix4d;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.SessionId;
import org.jdesktop.lg3d.wg.event.LgEvent;
import org.jdesktop.lg3d.wg.event.LgEventListener;
import org.jdesktop.lg3d.wg.event.MouseButtonEvent3D;
import org.jdesktop.lg3d.wg.internal.j3d.j3dnodes.J3dLgBranchGroup;
import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.Cell;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderCellMessage;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderMessage;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderMessage.RecorderGLOAction;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderCellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;

import org.jdesktop.lg3d.wonderland.scenemanager.CellMenuManager;

import org.jdesktop.j3d.util.SceneGraphUtil;

/**
 * Client-side cell to represent old-fashioned reel-reel tape recorder.
 * @author Marc Davies
 * @author Bernard Horan
 * @author Joe Provino
 */
public class AudioRecorderCell extends Cell implements ExtendedClientChannelListener {

    private static final Logger logger = Logger.getLogger(AudioRecorderCell.class.getName());
    
    private Button recordButton;
    private Button stopButton;
    private Button playButton;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private Set<Behavior> behaviors = new HashSet<Behavior>();
    private String userName = null;
  
    private static final float WIDTH = 0.2f;
    private static final float HEIGHT = 0.15f;
    private static final float DEPTH = 0.025f;
    private static final float REEL_RADIUS = 0.08f;
    private static final float BUTTON_WIDTH = WIDTH / 3;
    private static final float BUTTON_HEIGHT = 0.025f;
    // Define appearances    
    private static final Appearance RECORD_BUTTON_DEFAULT = new Appearance();
    private static final Appearance RECORD_BUTTON_SELECTED = new Appearance();
    private static final Appearance STOP_BUTTON_DEFAULT = new Appearance();
    private static final Appearance STOP_BUTTON_SELECTED = new Appearance();
    private static final Appearance PLAY_BUTTON_DEFAULT = new Appearance();
    private static final Appearance PLAY_BUTTON_SELECTED = new Appearance();
    
 
    static {
        RECORD_BUTTON_DEFAULT.setColoringAttributes(new ColoringAttributes(
                0.5f, 0, 0, ColoringAttributes.SHADE_FLAT));
        RECORD_BUTTON_SELECTED.setColoringAttributes(new ColoringAttributes(
                1.0f, 0, 0, ColoringAttributes.SHADE_GOURAUD));
        STOP_BUTTON_DEFAULT.setColoringAttributes(new ColoringAttributes(
                0.2f, 0.2f, 0.2f, ColoringAttributes.SHADE_FLAT));
        STOP_BUTTON_SELECTED.setColoringAttributes(new ColoringAttributes(
                0, 0, 0, ColoringAttributes.SHADE_GOURAUD));
        PLAY_BUTTON_DEFAULT.setColoringAttributes(new ColoringAttributes(
                0, 0.5f, 0.2f, ColoringAttributes.SHADE_FLAT));
        PLAY_BUTTON_SELECTED.setColoringAttributes(new ColoringAttributes(
                0, 1.0f, 0, ColoringAttributes.SHADE_GOURAUD));
    }
    
    public AudioRecorderCell(CellID cellID, String channelName, Matrix4d cellOrigin) {
        super(cellID, channelName, cellOrigin);

    }
    
    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }
    
    // Create the buttons
    @Override
    public void setup(CellSetup setup) {
        addRecordingDevice();        
        // handle initial selection
        AudioRecorderCellSetup rdcSetup = (AudioRecorderCellSetup) setup;
        setRecording(rdcSetup.isRecording());
        setPlaying(rdcSetup.isPlaying());
        stopButton.setSelected(!(isPlaying || isRecording));
        enableBehaviors(isPlaying || isRecording);
        userName = rdcSetup.getUserName();
    }

    private void addRecordingDevice() {
        addOuterCasing();
        addReel(new Vector3f(0f, 0.07f, -0.1f));
        addReel(new Vector3f(0f, 0.07f, 0.1f));
        float d = -DEPTH;
        float h = BUTTON_HEIGHT - HEIGHT;
        float w = BUTTON_WIDTH - WIDTH;
        addRecordButton(new Vector3f(d, h, w)); //depth, height, width
        w = w + (BUTTON_WIDTH * 2);
        addStopButton(new Vector3f(d, h, w));
        w = w + (BUTTON_WIDTH * 2);
        addPlayButton(new Vector3f(d, h, w));
    }
  
   
    private void addReel(Vector3f position) {
        // Create the root of the branch graph 
        J3dLgBranchGroup reelBG = new J3dLgBranchGroup();
        
        Transform3D reelTransform = new Transform3D();
        reelTransform.rotZ(Math.toRadians(90));
        reelTransform.setTranslation(position); 
        TransformGroup objTrans = new TransformGroup(reelTransform);        
        
        TransformGroup spinTg = new TransformGroup();
	spinTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        Cylinder reel = new Cylinder(REEL_RADIUS, DEPTH); 
        spinTg.addChild(reel);
        
        Transform3D zAxis = new Transform3D();
        Alpha rotationAlpha = new Alpha(-1, 600);
	RotationInterpolator rotator =
	    new RotationInterpolator(rotationAlpha, spinTg, zAxis,
				     0.0f, (float) Math.PI*2.0f);
        behaviors.add(rotator);

	BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        rotator.setSchedulingBounds(bounds);

	// Add the behavior and the transform group to the object
	objTrans.addChild(rotator);
	objTrans.addChild(spinTg);     
        
        reelBG.addChild(objTrans);
        // Set capability bits for collision system
	SceneGraphUtil.setCapabilitiesGraph(reelBG, false);
        
        cellLocal.addChild(reelBG); 
    }    
    
    private void addMouseEvents(J3dLgBranchGroup recorderBG, Button aButton) {
        recorderBG.setCapabilities();
        recorderBG.setMouseEventEnabled(true);
        recorderBG.setMouseEventSource(MouseButtonEvent3D.class, true);
        recorderBG.addListener(new MouseSelectionListener(aButton, this));        
    }
    
       
    public void receivedMessage(ClientChannel client, SessionId session, byte[] input) {
        AudioRecorderMessage message = Message.extractMessage(input, AudioRecorderMessage.class);

	if (message.getAction().equals(RecorderGLOAction.SET_VOLUME)) {
	    AudioRecorderCellMenu menu = AudioRecorderCellMenu.getInstance();
	    menu.volumeChanged(getCellID().toString(), message.getVolume());
	} else if (message.getAction().equals(RecorderGLOAction.PLAYBACK_DONE)) {
	    setPlaying(false);
	} else {
            setRecording(message.isRecording());
            setPlaying(message.isPlaying());
            userName = message.getUserName();
	}
    }    
    
    public void leftChannel(ClientChannel arg0) {       
    }    

    private void addOuterCasing() {
        Appearance casingAppearance = new Appearance();
        casingAppearance.setTransparencyAttributes(new TransparencyAttributes(
                TransparencyAttributes.BLENDED,
                0.5f,
                TransparencyAttributes.BLEND_SRC_ALPHA,
                TransparencyAttributes.BLEND_ONE)); 
        Box casing = new Box(DEPTH, HEIGHT, WIDTH, Box.ENABLE_APPEARANCE_MODIFY, casingAppearance);
        // Set capability bits for collision system
	SceneGraphUtil.setCapabilitiesGraph(casing, false);
        cellLocal.addChild(casing);        
    }

    private void addRecordButton(Vector3f position) {
        recordButton = addButton(position);
        recordButton.setAppearance(RECORD_BUTTON_DEFAULT);
        recordButton.setSelectedAppearance(RECORD_BUTTON_SELECTED);
        recordButton.setDefaultAppearance(RECORD_BUTTON_DEFAULT);
    }
    
    private void addStopButton(Vector3f position) {
        stopButton = addButton(position); 
        stopButton.setAppearance(STOP_BUTTON_DEFAULT);
        stopButton.setSelectedAppearance(STOP_BUTTON_SELECTED);
        stopButton.setDefaultAppearance(STOP_BUTTON_DEFAULT);
    }
    
    private void addPlayButton(Vector3f position) {
        playButton = addButton(position); 
        playButton.setAppearance(PLAY_BUTTON_DEFAULT);
        playButton.setSelectedAppearance(PLAY_BUTTON_SELECTED);
        playButton.setDefaultAppearance(PLAY_BUTTON_DEFAULT);
    }

    private Button addButton(final Vector3f position) {
        
        J3dLgBranchGroup buttonBG = new J3dLgBranchGroup();
        Button aButton = new Button(0.01f, BUTTON_HEIGHT, BUTTON_WIDTH);                 
        
        
        addMouseEvents(buttonBG, aButton);
        
        TransformGroup buttonTransformGroup = new TransformGroup();
        buttonTransformGroup.setCapability(
                TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        Transform3D buttonTransform = new Transform3D();                          // Set the position relative to the centre of the cell
        //buttonTransform.rotZ(Math.toRadians(90));
        buttonTransform.setTranslation(position);                                 
        buttonTransformGroup.setTransform(buttonTransform);
        
        buttonTransformGroup.addChild(aButton);
        buttonBG.addChild(buttonTransformGroup);
	// Set capability bits for collision system
	SceneGraphUtil.setCapabilitiesGraph(buttonBG, false);
        cellLocal.addChild(buttonBG);
        return aButton;
    }

    
    
    private void startRecording() {
        if (!isPlaying) {
            userName = getCurrentUserName();
            setRecording(true);
            AudioRecorderCellMessage msg = AudioRecorderCellMessage.recordingMessage(getCellID(), isRecording, userName);
            ChannelController.getController().sendMessage(msg);
        } else {
            logger.warning("Can't start recording when already playing");
        }
    }
    
    private void startPlaying() {
        if (!isRecording) {
	    userName = getCurrentUserName();
	    setPlaying(true);
            AudioRecorderCellMessage msg = AudioRecorderCellMessage.playingMessage(getCellID(), isPlaying, userName);
            ChannelController.getController().sendMessage(msg);
        } else {
            logger.warning("Can't start playing when already recording");
        }
    }
    
    
    private void stop() {
	if (userName.equals(getCurrentUserName())) {
            AudioRecorderCellMessage msg = null;
            if (isRecording) {
                msg = AudioRecorderCellMessage.recordingMessage(getCellID(), false, userName);
            }
            if (isPlaying) {
                msg = AudioRecorderCellMessage.playingMessage(getCellID(), false, userName);
            }
            if (msg != null) {
                ChannelController.getController().sendMessage(msg);
            }
            setRecording(false);
            setPlaying(false);
        } else {
            logger.warning("Attempt to stop by non-initiating user");
        }
    }
    
    private void setRecording(boolean b) {
        logger.info("setRecording: " + b);
        recordButton.setSelected(b);
        stopButton.setSelected(!b);
        enableBehaviors(b);
        isRecording = b;
    }
    
    private void setPlaying(boolean b) {
        logger.info("setPlaying: " + b);
        playButton.setSelected(b);
        stopButton.setSelected(!b);
        enableBehaviors(b);
        isPlaying = b;
    }
    
    private String getCurrentUserName() {
        return ChannelController.getController().getLocalUserName();
    }
    
    private void enableBehaviors(boolean b) {
        for (Behavior beh : behaviors) {
            beh.setEnable(b);
        }
    }
    
    public void setVolume(String name, double volume) {
	AudioRecorderCellMessage msg = AudioRecorderCellMessage.volumeMessage(getCellID(), name, volume);
        ChannelController.getController().sendMessage(msg);
    }

    class MouseSelectionListener implements LgEventListener {
        private Button button;
        private Cell cell;
        
        public MouseSelectionListener(Button button, Cell cell) {
            this.button = button;
            this.cell = cell;
        }
        
        public void processEvent(LgEvent lge) {
            if (lge instanceof MouseButtonEvent3D) {                  // Handle Mouse Clicks
                MouseButtonEvent3D mbe3D = (MouseButtonEvent3D) lge;
                
                if (mbe3D.isClicked()) {
                    if (button == stopButton) {
			/*
			 * We always handle the stop button.
			 */
                        stop();
			return;
                    } 

                    //
                    //Only care about the case when the button isn't already selected'
                    if (!button.isSelected()) {
                        if (button == recordButton) {
                            startRecording();
                        } else if (button == playButton) {
                            startPlaying();
			}

                    } else {
			String callId = getCellID().toString();

			AudioRecorderCellMenu menu = AudioRecorderCellMenu.getInstance();

			menu.setCallId(callId);

                        if (button == playButton) {
			    CellMenuManager.getInstance().showMenu(cell, menu,
				"Playback volume " + callId);
			    return;
			} else if (button == recordButton) {
			    CellMenuManager.getInstance().showMenu(cell, menu,
				"Record volume " + callId);
			    return;
			}
		    }
                }
            }
        }
            
        @SuppressWarnings("unchecked")                                            
        public Class<LgEvent>[] getTargetEventClasses() {
            return new Class[] {MouseButtonEvent3D.class};            
        }        

        
    }
    
    class Button extends Box {
        private boolean isSelected;
        private Appearance defaultAppearance;
        private Appearance selectedAppearance;
        
        Button(float sizeX, float sizeY, float sizeZ) {
            super (sizeX, sizeY, sizeZ, Box.ENABLE_APPEARANCE_MODIFY, null);
        }
        
        boolean isSelected() {
            return isSelected;
        }
        
        void setSelectedAppearance(Appearance selectedAppearance) {
            this.selectedAppearance = selectedAppearance;
        }
        
        void setDefaultAppearance(Appearance defaultAppearance) {
            this.defaultAppearance = defaultAppearance;
        }
        
        void setSelected(boolean selected) {
            this.isSelected = selected;
            updateAppearance();
        }
        
        public void updateAppearance() {
            if (isSelected) {
                setAppearance(selectedAppearance);                         
            } else {
                setAppearance(defaultAppearance);
            }
        }
    }
}
