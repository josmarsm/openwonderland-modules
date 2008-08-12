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

import com.sun.j3d.utils.image.TextureLoader;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.Texture;
import javax.vecmath.Point3d;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;


import javax.vecmath.Vector3f;
import javax.vecmath.Matrix4d;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.SessionId;
import java.awt.Container;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.media.j3d.Alpha;
import javax.media.j3d.Appearance;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
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
import org.jdesktop.lg3d.wonderland.audiorecorder.common.AudioRecorderCellSetup;
import org.jdesktop.lg3d.wonderland.audiorecorder.common.Tape;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;

import org.jdesktop.lg3d.wonderland.scenemanager.CellMenuManager;

import org.jdesktop.j3d.util.SceneGraphUtil;
import org.jdesktop.lg3d.wonderland.scenemanager.AssetManager;

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
    private DefaultListModel tapeListModel;
    private ListSelectionModel tapeSelectionModel;
    private ReelForm reelForm;
  
    private String baseURL;
    
    public AudioRecorderCell(CellID cellID, String channelName, Matrix4d cellOrigin) {
        super(cellID, channelName, cellOrigin);
        
    }
    
    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }
    
    // Create the buttons
    @Override
    public void setup(CellSetup setup) {
        AudioRecorderCellSetup rdcSetup = (AudioRecorderCellSetup) setup;

	baseURL = rdcSetup.getBaseURL();
        Set tapes = rdcSetup.getTapes();
        Tape selectedTape = rdcSetup.getSelectedTape();
        createTapeModels(tapes, selectedTape);

        addRecordingDevice();        
        // handle initial selection
        setRecording(rdcSetup.isRecording());
        setPlaying(rdcSetup.isPlaying());
        stopButton.setSelected(!(isPlaying || isRecording));
        enableBehaviors(isPlaying || isRecording);
        userName = rdcSetup.getUserName();
        reelForm = new ReelForm(this);
    }

    
    
    private void createTapeModels(Set<Tape> tapes, Tape selectedTape) {
        List sortedTapes = new ArrayList(tapes);
        Collections.sort(sortedTapes);
        tapeListModel = new DefaultListModel();
        for (Iterator it = sortedTapes.iterator(); it.hasNext();) {
            tapeListModel.addElement(it.next());
        }
        
        tapeSelectionModel = new DefaultListSelectionModel();
        tapeSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        int selectionIndex = sortedTapes.indexOf(selectedTape);
        tapeSelectionModel.setSelectionInterval(0, selectionIndex);
    }
    
    void selectedTapeChanged() {
            int index = tapeSelectionModel.getMaxSelectionIndex();
            if (index >= 0) {
                Tape selectedTape = (Tape) tapeListModel.elementAt(index);
                AudioRecorderCellMessage msg = AudioRecorderCellMessage.tapeSelected(getCellID(), selectedTape.getTapeName());
                ChannelController.getController().sendMessage(msg);
            }
    }
    
    private void addRecordingDevice() {
        Transform3D transform = new Transform3D();
        //Adjust the scale to fit the world from Justin's original model
        transform.setScale(0.5f);
        TransformGroup tg = new TransformGroup(transform);
        BranchGroup bg = new BranchGroup();
        addOuterCasing(bg);
        addReel(new Vector3f(-0.2f, 0.2f, 0.17f), "models/audiorecorder/audiorecorder_leftreel.j3s.gz", bg);
        addReel(new Vector3f(0.2f, 0.2f, 0.17f), "models/audiorecorder/audiorecorder_rightreel.j3s.gz", bg);
        addRecordButton(bg);
        addStopButton(bg);
        addPlayButton(bg);
        tg.addChild(bg);
        cellLocal.addChild(tg);
    }
  
   
    private void addReel(Vector3f position, String modelFilename, BranchGroup bg) {
        // Create the root of the branch graph 
        J3dLgBranchGroup reelBG = new J3dLgBranchGroup();
        
        Transform3D reelTransform = new Transform3D();
        TransformGroup objTrans = new TransformGroup(reelTransform);        
        
        TransformGroup spinTg = new TransformGroup();
	spinTg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        BranchGroup reel = AssetManager.getAssetManager().loadGraph(baseURL, modelFilename, "");
        spinTg.addChild(reel);
        
        Transform3D zAxis = new Transform3D();
        zAxis.rotX(Math.toRadians(90));
        zAxis.setTranslation(position);
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
        
        bg.addChild(reelBG); 
        
        addReelEvents(reelBG, reel);
    }    
    
    private void addReelEvents(J3dLgBranchGroup recorderBG, BranchGroup reel) {
        recorderBG.setCapabilities();
        recorderBG.setMouseEventEnabled(true);
        recorderBG.setMouseEventSource(MouseButtonEvent3D.class, true);
        recorderBG.addListener(new ReelListener(reel, this));        
    }
    
    private void addButtonEvents(J3dLgBranchGroup recorderBG, Button aButton) {
        recorderBG.setCapabilities();
        recorderBG.setMouseEventEnabled(true);
        recorderBG.setMouseEventSource(MouseButtonEvent3D.class, true);
        recorderBG.addListener(new ButtonListener(aButton, this));        
    }
    
       
    public void receivedMessage(ClientChannel client, SessionId session, byte[] input) {
        AudioRecorderMessage message = Message.extractMessage(input, AudioRecorderMessage.class);
        switch (message.getAction()) {
            case SET_VOLUME:
                AudioRecorderCellMenu menu = AudioRecorderCellMenu.getInstance();
                menu.volumeChanged(getCellID().toString(), message.getVolume());
                break;
            case PLAYBACK_DONE:
                setPlaying(false);
                break;
            case PLAY:
                setPlaying(message.isPlaying());
                userName = message.getUserName();
                break;
            case RECORD:
                setRecording(message.isRecording());
                userName = message.getUserName();
                break;
            case TAPE_USED:
                setTapeUsed(message.getTapeName());
                break;
            case NEW_TAPE:
                Tape newTape = new Tape(message.getTapeName());
                tapeListModel.addElement(newTape);
                break;
            case TAPE_SELECTED:
                selectTape(message.getTapeName());
                break;
            default:
                logger.severe("Unknown action type: " + message.getAction());
        
        }

    }    
    
    public void leftChannel(ClientChannel arg0) {       
    }    

    private void addOuterCasing(BranchGroup bg) {
        BranchGroup casing = AssetManager.getAssetManager().loadGraph(baseURL, "models/audiorecorder/audiorecorder_body.j3s.gz", "");
        // Set capability bits for collision system
	SceneGraphUtil.setCapabilitiesGraph(casing, false);
        bg.addChild(casing);        
    }

    private void addRecordButton(BranchGroup bg) {
        recordButton = addButton("models/audiorecorder/audiorecorder_recordbutton.j3s.gz", "textures/audiorecorder/audiorecorder_recordlit.png", bg);
    }
    
    private void addStopButton(BranchGroup bg) {
        stopButton = addButton("models/audiorecorder/audiorecorder_stopbutton.j3s.gz", "textures/audiorecorder/audiorecorder_stoplit.png", bg); 
    }
    
    private void addPlayButton(BranchGroup bg) {
        playButton = addButton("models/audiorecorder/audiorecorder_playbutton.j3s.gz", "textures/audiorecorder/audiorecorder_playlit.png", bg); 
    }

    private Button addButton(String modelFilename, String litTextureFilename, BranchGroup bg) {
        
        J3dLgBranchGroup buttonBG = new J3dLgBranchGroup();
        Button aButton = new Button(modelFilename, litTextureFilename);                 
        
        addButtonEvents(buttonBG, aButton);
        
        buttonBG.addChild(aButton);
	// Set capability bits for collision system
	SceneGraphUtil.setCapabilitiesGraph(buttonBG, false);
        bg.addChild(buttonBG);
        return aButton;
    }

    private void selectTape(String tapeName) {
        Enumeration tapes = tapeListModel.elements();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            if (aTape.getTapeName().equals(tapeName)) {
                reelForm.selectTape(aTape);
            }
        }
    }

    private void setTapeUsed(String tapeName) {
        Enumeration tapes = tapeListModel.elements();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            if (aTape.getTapeName().equals(tapeName)) {
                aTape.setUsed();
            }
        }
    }

    
    
    private void startRecording() {
        if (!isPlaying) {
            Tape selectedTape = getSelectedTape();
            if (selectedTape == null) {
                logger.warning("Can't record when there's no selected tape");
                return;
            }
            if (!selectedTape.isFresh()) {
                logger.warning("Overwriting existing recording");
            }
            setUsed(selectedTape);
            userName = getCurrentUserName();
            setRecording(true);
            AudioRecorderCellMessage msg = AudioRecorderCellMessage.recordingMessage(getCellID(), isRecording, userName);
            ChannelController.getController().sendMessage(msg);
        } else {
            logger.warning("Can't start recording when already playing");
        }
    }
    
    private void setUsed(Tape aTape) {
        aTape.setUsed();
        AudioRecorderCellMessage msg = AudioRecorderCellMessage.setTapeUsed(getCellID(), aTape.getTapeName());
        ChannelController.getController().sendMessage(msg);
    }
    
    private void startPlaying() {
        if (!isRecording) {
            Tape selectedTape = getSelectedTape();
            if (selectedTape == null) {
                logger.warning("Can't playback when there's no selected tape");
                return;
            }
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
    
    ListModel getTapeListModel() {
        return tapeListModel;
    }
    
   ListSelectionModel getTapeSelectionModel() {
       return tapeSelectionModel;
   }
   
   private Tape getSelectedTape() {
       int selectionIndex = tapeSelectionModel.getMaxSelectionIndex();
       if (selectionIndex == -1) {
           return null;
       } else {
           return (Tape) tapeListModel.elementAt(selectionIndex);
       }
   }
   
   Set<String> getTapeNames() {
       Enumeration tapes = tapeListModel.elements();
       Set<String> tapeNames = new HashSet<String>();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            tapeNames.add(aTape.getTapeName());
        }
       return tapeNames;
   }
   
   Tape addTape(String tapeName) {
        Tape newTape = new Tape(tapeName);
        tapeListModel.addElement(newTape);
        AudioRecorderCellMessage msg = AudioRecorderCellMessage.newTape(getCellID(), tapeName);
        ChannelController.getController().sendMessage(msg);
        return newTape;
    }
    

    class ButtonListener implements LgEventListener {
        private Button button;
        private Cell cell;
        
        public ButtonListener(Button button, Cell cell) {
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
    
    class Button extends J3dLgBranchGroup {
        private boolean isSelected;
        private Appearance defaultAppearance = null;
        private Appearance selectedAppearance;
        
        Button(String modelFilename, String litTextureFilename) {
            BranchGroup buttonModel = AssetManager.getAssetManager().loadGraph(baseURL, modelFilename, "");
            addChild(buttonModel);
            try {
                URL textureURL = new URL(baseURL + '/' + litTextureFilename);
                TextureLoader tLoader = new TextureLoader(textureURL, new Container());
                Texture litTexture = tLoader.getTexture();
                selectedAppearance = new Appearance();
                selectedAppearance.setTexture(litTexture);
            } catch (MalformedURLException ex) {
                Logger.getLogger(AudioRecorderCell.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        boolean isSelected() {
            return isSelected;
        }
        
        void setSelected(boolean selected) {
            this.isSelected = selected;
            updateAppearance();
        }
        
        private void updateAppearance() {
            setDefaultAppearance(); //Should only be called once
            if (isSelected) {
                setAppearance(selectedAppearance);                         
            } else {
                setAppearance(defaultAppearance);
            }
        }
        
        private void setAppearance(Appearance anAppearance) {
            Shape3D buttonShape = getButtonShape();
            if (buttonShape != null) {
                buttonShape.setAppearance(anAppearance);
            }
        }
        
        private void setDefaultAppearance() {
            //nasty hack
            //Ideally we'd be told when the node was added to the scenegraph (like addNotify in AWT)
            //Or have two textures, one for on and one for off
            if (defaultAppearance != null) {
                return;
            }
            Shape3D buttonShape = getButtonShape();
            if (buttonShape != null) {
                defaultAppearance = buttonShape.getAppearance();
            }
            
        }
        
        private Shape3D getButtonShape() {
            //Nasty hack. Really, really tight coupling with Justin's model.
            BranchGroup bg = (BranchGroup) getChild(0);
            if (bg.numChildren() == 1) {
                //The model has been loaded
                BranchGroup buttonModel = (BranchGroup) bg.getChild(0);
                TransformGroup tg = (TransformGroup) buttonModel.getChild(0);
                return (Shape3D) tg.getChild(0);
            } else {
                return null;
            }
        }
    }
    
    class ReelListener implements LgEventListener {
        private BranchGroup reel;
        private Cell cell;
        
        public ReelListener(BranchGroup reel, Cell cell) {
            this.reel = reel;
            this.cell = cell;
        }
        
        public void processEvent(LgEvent lge) {
            if (lge instanceof MouseButtonEvent3D) {                  // Handle Mouse Clicks
                MouseButtonEvent3D mbe3D = (MouseButtonEvent3D) lge;
                
                if (mbe3D.isClicked()) {
                        reelForm.setVisible(true);
		    }
                }
            }

        public Class[] getTargetEventClasses() {
            return new Class[] {MouseButtonEvent3D.class};
        }
               

        
    }
}
