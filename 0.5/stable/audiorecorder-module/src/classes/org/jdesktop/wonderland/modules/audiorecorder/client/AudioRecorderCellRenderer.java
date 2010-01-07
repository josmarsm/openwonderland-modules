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

package org.jdesktop.wonderland.modules.audiorecorder.client;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.sun.scenario.animation.Animation;
import com.sun.scenario.animation.Clip;
import com.sun.scenario.animation.Clip.RepeatBehavior;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;


import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.jme.utils.ScenegraphUtils;

/**
 * Renderer for the AudioRecorder Cell
 * @author Bernard Horan
 */
public class AudioRecorderCellRenderer extends BasicRenderer {

    private static final Logger rendererLogger = Logger.getLogger(AudioRecorderCellRenderer.class.getName());

    public static final float WIDTH = 0.6f; //x-extent
    public static final float HEIGHT = 0.2f ; //y-extent
    public static final float DEPTH = WIDTH; //z-extent
    private Button recordButton;
    private Button stopButton;
    private Button playButton;
    private Set<Button> buttons = new HashSet<Button>();
    private Set<Animation> animations = new HashSet<Animation>();

    public AudioRecorderCellRenderer(Cell cell) {
        super(cell);
    }

    protected Node createSceneGraph(Entity entity) {
        /* Create the scene graph object*/
        Node root = new Node("Audio Recorder Root");
        attachRecordingDevice(root, entity);
        root.setModelBound(new BoundingBox());
        root.updateModelBound();
        //Set the name of the buttonRoot node
        root.setName("Cell_" + cell.getCellID() + ":" + cell.getName());

        //Set the state of the buttons
        boolean isRecording = ((AudioRecorderCell)cell).isRecording();
        boolean isPlaying = ((AudioRecorderCell)cell).isPlaying();
        setRecording(isRecording);
        setPlaying(isPlaying);
        stopButton.setSelected(!(isPlaying || isRecording));
        enableAnimations(isPlaying || isRecording);
        return root;
    }

    private void attachRecordingDevice(Node device, Entity entity) {
        try {
            addRecorderModel(device, entity);
        } catch (IOException ex) {
            rendererLogger.log(Level.SEVERE, "Failed to load audio recorder model", ex);
        }
    }

    private void addRecorderModel(Node device, Entity entity) throws IOException {
        LoaderManager manager = LoaderManager.getLoaderManager();
        URL url = AssetUtils.getAssetURL("wla://audiorecorder/pwl_3d_audiorecorder_012a.dae/pwl_3d_audiorecorder_012a.dae.gz.dep", this.getCell());
        DeployedModel dm = manager.getLoaderFromDeployment(url);
        Node cameraModel = dm.getModelLoader().loadDeployedModel(dm, entity);
        //translate the model so that it's in the centre of the cell
        cameraModel.setLocalTranslation(0, 0, 0.3f);
        device.attachChild(cameraModel);

        //Create the record button and set to off
        Spatial recordSpatialOn = ScenegraphUtils.findNamedNode(cameraModel, "btnRecordOn_001-btnOn-symbol");
        Spatial recordSpatialOff = ScenegraphUtils.findNamedNode(cameraModel, "btnRecord_001-arUV-symbol");
        recordButton = new Button(recordSpatialOn, recordSpatialOff, false);
        buttons.add(recordButton);
        
        //Get the play button and set to off
        Spatial playSpatialOn = ScenegraphUtils.findNamedNode(cameraModel, "btnPlayOn_001-btnOn-symbol");
        Spatial playSpatialOff = ScenegraphUtils.findNamedNode(cameraModel, "btnPlay_001-arUV-symbol");
        playButton = new Button(playSpatialOn, playSpatialOff, false);
        buttons.add(playButton);

        //Get the stop button and set to on
        Spatial stopSpatialOn = ScenegraphUtils.findNamedNode(cameraModel, "btnStopOn_001-btnOn-symbol");
        Spatial stopSpatialOff = ScenegraphUtils.findNamedNode(cameraModel, "btnStop_001-arUV-symbol");
        stopButton = new Button(stopSpatialOn, stopSpatialOff, true);
        buttons.add(stopButton);

        //set up the animations
        Spatial spool1 = ScenegraphUtils.findNamedNode(cameraModel, "spoolLg1-Geometry-arUV-symbol");
        createSpool(spool1, entity);
        Spatial spool2 = ScenegraphUtils.findNamedNode(cameraModel, "spoolLg2-Geometry-arUV-symbol");
        createSpool(spool2, entity);
        Spatial spool3 = ScenegraphUtils.findNamedNode(cameraModel, "spoolSm_001-spoolSm-symbol");
        createSpool(spool3, entity);
        Spatial spool4 = ScenegraphUtils.findNamedNode(cameraModel, "spoolSm1-Geometry-spoolSm-symbol");
        createSpool(spool4, entity);
        Spatial spool5 = ScenegraphUtils.findNamedNode(cameraModel, "spoolSm2-Geometry-spoolSm-symbol");
        createSpool(spool5, entity);

        //Listen for mouse events
        RecorderListener listener = new RecorderListener();
        listener.addToEntity(entity);
    }

    private void createSpool(Spatial spool, Entity entity) {
        RotationAnimationProcessor spinner = new RotationAnimationProcessor(entity, spool, 0f, 360, new Vector3f(0f,1f,0f));
        Clip clip = Clip.create(1000, Clip.INDEFINITE, spinner);
        clip.setRepeatBehavior(RepeatBehavior.LOOP);
        clip.start();
        animations.add(clip);
    }

    void setRecording(boolean b) {
        recordButton.setSelected(b);
        stopButton.setSelected(!b);
        enableAnimations(b);
    }

    void setPlaying(boolean b) {
        playButton.setSelected(b);
        stopButton.setSelected(!b);
        enableAnimations(b);
    }

    private void enableAnimations(boolean b) {
        for (Animation anim : animations) {
            if (b) {
                anim.resume();
            } else {
                anim.pause();
            }
        }
    }

   class RecorderListener extends EventClassListener {

        RecorderListener() {
            super();
        }

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        // Note: we don't override computeEvent because we don't do any computation in this listener.
        @Override
        public void commitEvent(Event event) {
            //rendererLogger.info("commit " + event + " for " + this);
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            //ignore any mouse button that isn't the left one
            if (mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }
            //Ignore any mouse event that isn't a click
            if (mbe.getID() != MouseEvent.MOUSE_CLICKED) {
                return;
            }
            TriMesh mesh = mbe.getPickDetails().getTriMesh();
            Button selectedButton = getSelectedButton(mesh);
            if (selectedButton == null) {
                rendererLogger.warning("no button for mesh: " + mesh);
                return;
            }

            if (selectedButton == playButton) {
                rendererLogger.info("play button clicked");
                ((AudioRecorderCell)cell).startPlaying();
                return;
            }
            if (selectedButton == stopButton) {
                rendererLogger.info("stop button clicked");
                ((AudioRecorderCell)cell).stop();
                return;
            }
            if (selectedButton == recordButton) {
                rendererLogger.info("record button clicked");
                ((AudioRecorderCell)cell).startRecording();
                return;
            }

        }

        private Button getSelectedButton(TriMesh mesh) {
            for (Button button : buttons) {
                if (button.usesMesh(mesh)) {
                    return button;
                }
            }
            return null;
        }

        
    }

    class Button {

        private Spatial onSpatial,  offSpatial;

        Button(Spatial onSpatial, Spatial offSpatial, boolean selected) {
            this.onSpatial = onSpatial;
            this.offSpatial = offSpatial;
            //locate "on" button so that it appears "pressed"
            onSpatial.setLocalTranslation(0, 0.015f, 0);
            setBasicSelected(selected);
        }

        private void setBasicSelected(boolean selected) {
            onSpatial.setVisible(selected);
            offSpatial.setVisible(!selected);
        }

        void setSelected(boolean selected) {
            setBasicSelected(selected);
            WorldManager wm = ClientContextJME.getWorldManager();
            wm.addToUpdateList(offSpatial);
            wm.addToUpdateList(onSpatial);
        }

        private boolean usesMesh(TriMesh mesh) {
            if (mesh == onSpatial) {
                return true;
            }
            if (mesh == offSpatial) {
                return true;
            }
            return false;
        }
    }
        

    
}
