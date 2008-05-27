/**
 * Project Wonderland
 *
 * $RCSfile: RecordingDeviceCell.java,v $
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
 * $Revision: 1.1.2.10 $
 * $Date: 2008/03/11 14:04:21 $
 * $State: Exp $
 * $Id: RecordingDeviceCell.java,v 1.1.2.10 2008/03/11 14:04:21 bernard_horan Exp $
 */

package org.jdesktop.lg3d.wonderland.eventrecorder.client.cell;

import com.sun.j3d.utils.geometry.Primitive;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;

import javax.media.j3d.*;
import javax.vecmath.Vector3f;
import javax.vecmath.Matrix4d;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.SessionId;
import javax.vecmath.Vector3d;
import org.jdesktop.j3d.util.SceneGraphUtil;
import org.jdesktop.lg3d.wg.event.LgEvent;
import org.jdesktop.lg3d.wg.event.LgEventListener;
import org.jdesktop.lg3d.wg.event.MouseButtonEvent3D;
import org.jdesktop.lg3d.wg.event.MouseEnteredEvent3D;
import org.jdesktop.lg3d.wg.internal.j3d.j3dnodes.J3dLgBranchGroup;
import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.Cell;
import org.jdesktop.lg3d.wonderland.darkstar.common.AvatarBoundsManager;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.eventrecorder.EventRecorder;
import org.jdesktop.lg3d.wonderland.eventrecorder.common.EventRecorderCellMessage;
import org.jdesktop.lg3d.wonderland.eventrecorder.common.EventRecorderMessage;
import org.jdesktop.lg3d.wonderland.eventrecorder.common.EventRecorderCellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;

/**
 * 
 * @author Marc Davies
 * @author Bernard Horan
 */
public class EventRecorderCell extends Cell implements ExtendedClientChannelListener {
    private static final Logger logger = Logger.getLogger(EventRecorderCell.class.getName());
    
    private Button recordButton;
    private Button stopButton;
    private boolean isRecording = false;
    private Set<Behavior> behaviors = new HashSet<Behavior>();
    private EventRecorder eventRecorder = null;
    private String userName = null;
  
    private static final float WIDTH = 0.2f;
    private static final float HEIGHT = 0.15f;
    private static final float DEPTH = 0.025f;
    private static final float REEL_RADIUS = 0.08f;
    private static final float BUTTON_WIDTH = 0.05f;
    private static final float BUTTON_HEIGHT = 0.025f;
    // Define appearances    
    private static final Appearance RECORD_BUTTON_DEFAULT = new Appearance();
    private static final Appearance RECORD_BUTTON_SELECTED = new Appearance();
    private static final Appearance STOP_BUTTON_DEFAULT = new Appearance();
    private static final Appearance STOP_BUTTON_SELECTED = new Appearance();

    
 
    static {
        RECORD_BUTTON_DEFAULT.setColoringAttributes(new ColoringAttributes(
                0.5f, 0, 0, ColoringAttributes.SHADE_FLAT));
        RECORD_BUTTON_SELECTED.setColoringAttributes(new ColoringAttributes(
                1.0f, 0, 0, ColoringAttributes.SHADE_GOURAUD));
        STOP_BUTTON_DEFAULT.setColoringAttributes(new ColoringAttributes(
                0.2f, 0.2f, 0.2f, ColoringAttributes.SHADE_FLAT));
        STOP_BUTTON_SELECTED.setColoringAttributes(new ColoringAttributes(
                0, 0, 0, ColoringAttributes.SHADE_GOURAUD));
    }
    
    public EventRecorderCell(CellID cellID, String channelName, Matrix4d cellOrigin) {
        super(cellID, channelName, cellOrigin);
    }
    
    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }
    
    // Create the buttons
    public void setup(CellSetup setup) {
        addRecordingDevice();        
        // handle initial selection
        EventRecorderCellSetup rdcSetup = (EventRecorderCellSetup) setup;
        setRecording(rdcSetup.isRecording());
        userName = rdcSetup.getUserName();
    }

    private void addRecordingDevice() {
        addOuterCasing();
        addReel(new Vector3f(0f, 0.07f, -0.1f));
        addReel(new Vector3f(0f, 0.07f, 0.1f));
        addRecordButton(new Vector3f(-DEPTH, -0.075f, -0.15f)); //depth, height, width
        addStopButton(new Vector3f(-DEPTH, -0.075f-BUTTON_WIDTH, -0.15f));   
        SceneGraphUtil.setCapabilitiesGraph(cellLocal, false);
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
        
        cellLocal.addChild(reelBG); 
    }    
    
    private void addMouseEvents(J3dLgBranchGroup recorderBG, Button aButton) {
        recorderBG.setCapabilities();
        recorderBG.setMouseEventEnabled(true);
        recorderBG.setMouseEventSource(MouseButtonEvent3D.class, true);
        recorderBG.addListener(new MouseSelectionListener(aButton));        
    }
    
       
    public void receivedMessage(ClientChannel client, SessionId session, byte[] input) {
        EventRecorderMessage message = Message.extractMessage(input, EventRecorderMessage.class);
        switch (message.getActionType()) {
            case RECORDING:
                setRecording(message.isRecording());
                userName = message.getUserName();
                break;
            case SYNC_CELL:
                getEventRecorder().recordCellHierarchyMessage(message.getWrappedMessage());
                break;
            case START_SYNC:
                getEventRecorder().startSync();
                break;
            case END_SYNC:
                getEventRecorder().endSync();
                break;
            case START_SYNC_CELLS:
                getEventRecorder().startSyncCells();
                break;
            case END_SYNC_CELLS:
                getEventRecorder().endSyncCells();
                break;
            case START_SYNC_STATE:
                getEventRecorder().startSyncState();
                break;
            case END_SYNC_STATE:
                getEventRecorder().endSyncState();
                break;
            case SYNC_STATE:
                getEventRecorder().writeSynchronizationMessage(message.getCellID(), message.getChannelName(), message.getWrappedMessage());
                break;
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
        cellLocal.addChild(casing);        
    }

    private void addRecordButton(Vector3f position) {
        recordButton = addButton(position);
        recordButton.setAppearance(RECORD_BUTTON_DEFAULT);
    }
    
    private void addStopButton(Vector3f position) {
        stopButton = addButton(position); 
        stopButton.setAppearance(STOP_BUTTON_DEFAULT);
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
        cellLocal.addChild(buttonBG);
        return aButton;
    }
    
    private void startRecording() {
        logger.info("Start Recording");
        userName = getCurrentUserName();
        setRecording(true);
        EventRecorderCellMessage msg;
        //Tell the server (and other clients) that I'm recording
        msg = new EventRecorderCellMessage(getCellID(), isRecording, userName);
        ChannelController.getController().sendMessage(msg);
        
        //Request synchronisation of cells from the server
        msg = EventRecorderCellMessage.synchronizationRequest(getCellID());
        ChannelController.getController().sendMessage(msg);
        
        //DO NOT tell the event recorder to start recording here. 
        //Recording should not begin until synchronisation is complete
        //getEventRecorder().startRecording();
        
        
    }
    
    /*
     *Lazy instantiation and initialisation, in case this client doesn't need one
     */
    private EventRecorder getEventRecorder() {
        if (eventRecorder == null) {
            Vector3d currentPosition = new Vector3d();
            getCellOrigin().get(currentPosition);
            logger.info("Current position: " + currentPosition);
            Point3f origin = new Point3f((int) currentPosition.getX(), (int) currentPosition.getY(), (int) currentPosition.getZ());
            logger.info("origin: " + origin);
            Bounds recorderBounds = AvatarBoundsManager.getProximityBounds(origin);
            logger.info("recorderBounds: " + recorderBounds);
            Set<Cell> visibleCells = getWorldRootCell().getVisibleCells(recorderBounds);
            logger.info("Visible Cells: " + visibleCells);
            eventRecorder = new EventRecorder(visibleCells);
            eventRecorder.register();
        }
        return eventRecorder;
    }
    
    private void stopRecording() {
        logger.info("Stop Recording");
        if (userName.equals(getCurrentUserName())) {
            userName = null;
            getEventRecorder().stopRecording();
            setRecording(false);
        } else {
            logger.warning("Attempt to stop recorder by non-initiating user");
        }
        
        EventRecorderCellMessage msg = new EventRecorderCellMessage(getCellID(), isRecording, userName);

        // Send a message to the server indicating the new selection.
        // The server will repeat it out to all other clients.
        ChannelController.getController().sendMessage(msg);
    }
    
    private void setRecording(boolean b) {
        logger.info("setRecording: " + b);
        recordButton.setSelected(b);
        stopButton.setSelected(!b);
        enableBehaviors(b);
        isRecording = b;
    }
    
    private String getCurrentUserName() {
        return ChannelController.getController().getLocalUserName();
    }
    
    private void enableBehaviors(boolean b) {
        for (Behavior beh : behaviors) {
            beh.setEnable(b);
        }
    }
    
    class MouseSelectionListener implements LgEventListener {
        private Button button;
        
        public MouseSelectionListener(Button button) {
            this.button = button;
        }
        
        public void processEvent(LgEvent lge) {
            if (lge instanceof MouseButtonEvent3D) {                           // Handle Mouse Clicks
                MouseButtonEvent3D mbe3D = (MouseButtonEvent3D) lge;
                
                if (mbe3D.isClicked()) {
                    //
                    //Only care about the case when the button isn't already selected'
                    if (!button.isSelected()) {
                        if (button == recordButton) {
                            startRecording();
                        } else {
                            stopRecording();
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
        
        Button(float sizeX, float sizeZ, float sizeY) {
            super (sizeX, sizeZ, sizeY, Box.ENABLE_APPEARANCE_MODIFY, null);
        }
        
        boolean isSelected() {
            return isSelected;
        }
        
        void setSelected(boolean selected) {
            this.isSelected = selected;
            updateAppearance();
        }
        
        public void updateAppearance() {
            if (isSelected) {
                if (this == recordButton) {
                    setAppearance(RECORD_BUTTON_SELECTED);
                } else {
                    setAppearance(STOP_BUTTON_SELECTED);
                }                          
            } else {
                if (this == recordButton) {
                    setAppearance(RECORD_BUTTON_DEFAULT);
                } else {
                    setAppearance(STOP_BUTTON_DEFAULT);
                }               
            }
        }
    }
}