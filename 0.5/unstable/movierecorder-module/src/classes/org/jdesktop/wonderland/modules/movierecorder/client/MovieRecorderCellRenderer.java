/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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

package org.jdesktop.wonderland.modules.movierecorder.client;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;


import java.nio.ByteBuffer;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.TextureRenderBuffer;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.RenderUpdater;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.JComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.jme.utils.ScenegraphUtils;

/**
 * Renderer for the movie recorder cell. Includes the code to create the camera, download the model and create the "LCD".
 * @author Bernard Horan
 */
public class MovieRecorderCellRenderer extends BasicRenderer implements RenderUpdater {

    private static final Logger rendererLogger = Logger.getLogger(MovieRecorderCellRenderer.class.getName());
    private static final SimpleDateFormat STILL_IMAGE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HH.mm.ss");


    //Use 16:9 aspect ratio
    public static final float WIDTH = 0.8f; //x-extent
    public static final float HEIGHT = 0.45f ; //y-extent
    public static final float DEPTH = 0.05f; //z-extent
    private static final int IMAGE_HEIGHT = 360;
    private static final int IMAGE_WIDTH = 640;
    /**
     *Counter for naming images
     **/
    private int imageCounter;
    /**
     *Counter for frames
     **/
    private int frameCounter;

    private TextureRenderBuffer textureBuffer = null;
    private CaptureComponent captureComponent = null;
    private BufferedImage captureImage = null;
    private Spatial videoSpatial, videoSpatialOn, recordStatus, recordStatusOn, stillSpatial, stillSpatialOn;

    public MovieRecorderCellRenderer(Cell cell) {
        super(cell);
    }

    protected Node createSceneGraph(Entity entity) {
        /* Create the scene graph object*/
        Node root = new Node("Movie Recorder Root");
        attachRecordingDevice(root, entity);
        root.setModelBound(new BoundingBox());
        root.updateModelBound();
        //Set the name of the buttonRoot node
        root.setName("Cell_" + cell.getCellID() + ":" + cell.getName());
        
        
        return root;
    }

    JComponent getCaptureComponent() {
        return captureComponent;
    }

    int getFrameCounter() {
        return frameCounter;
    }

    private void attachRecordingDevice(Node device, Entity entity) {
        try {
            addCameraModel(device, entity);
        } catch (IOException ex) {
            rendererLogger.log(Level.SEVERE, "Failed to load camera model", ex);
        }
        entity.addEntity(createLCDPanel(device));
    }

    private void addCameraModel(Node device, Entity entity) throws IOException {
        //Load the cameramodel and add it to the scenegraph
        LoaderManager manager = LoaderManager.getLoaderManager();
        URL url = AssetUtils.getAssetURL("wla://movierecorder/pwl_3d_videorecorder_009.dae/pwl_3d_videorecorder_009.dae.gz.dep", this.getCell());
        DeployedModel dm = manager.getLoaderFromDeployment(url);
        Node cameraModel = dm.getModelLoader().loadDeployedModel(dm, entity);
        device.attachChild(cameraModel);

        //Get the record status nodes and set it to off
        recordStatus = ScenegraphUtils.findNamedNode(cameraModel, "combinedMesh_vrRecordStatus_002-vrRecordStatus");
        recordStatusOn = ScenegraphUtils.findNamedNode(cameraModel, "combinedMesh_vrRecordStatusOn-Geometry-vrRecordStatusOn");
        recordStatusOn.setVisible(false);

        //Get the video buttons and set it to off
        videoSpatial = ScenegraphUtils.findNamedNode(cameraModel, "combinedMesh_vrBtnVideo_002-vrBtnVideo");
        videoSpatialOn = ScenegraphUtils.findNamedNode(cameraModel, "combinedMesh_vrBtnVideoOn-Geometry-vrBtnVideoOn");
        videoSpatialOn.setVisible(false);
        
        //create a listener to control the appearance of the video buttons
        ((MovieRecorderCell)cell).getVideoButtonModel().addItemListener(new VideoButtonListener());

        //Get the still buttons and set it to off
        stillSpatial = ScenegraphUtils.findNamedNode(cameraModel, "combinedMesh_vrBtnStill_002-vrBtnStill");
        stillSpatialOn = ScenegraphUtils.findNamedNode(cameraModel, "combinedMesh_vrBtnStillOn-Geometry-vrBtnStillOn");
        stillSpatialOn.setVisible(false);

        //Listen for mouse events
        CameraListener listener = new CameraListener();
        listener.addToEntity(entity);
    }

    private Entity createLCDPanel(Node device) {
        WorldManager wm = ClientContextJME.getWorldManager();
        //Node for the quad
        Node quadNode = new Node("quad node");
        //Geometric
        Quad quadGeo = new Quad("Ortho", 2 * WIDTH, 2 * HEIGHT);
        //Entity for the quad
        Entity quadEntity = new Entity("Ortho ");
        //Attach the geometric to the node
        quadNode.attachChild(quadGeo);
        //Set the quad node position so that it is directly in front of the camera model
        //To give the appearance of an LCD panel
        quadNode.setLocalTranslation(0.0f, -0.15f,  -0.045f);
        //Create the texture buffer
        textureBuffer = (TextureRenderBuffer) wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.TEXTURE_2D, IMAGE_WIDTH, IMAGE_HEIGHT);
        textureBuffer.setIncludeOrtho((false));
        //Create a camera node
        CameraNode cn = new CameraNode("MyCamera", null);
        //Create a node for the camera
        Node cameraSG = new Node("cameraSG");
        //Attach the camera to the node
        cameraSG.attachChild(cn);
        //Rotate the camera through 180 degrees about the Y-axis
        float angleDegrees = 180;
        float angleRadians = (float) Math.toRadians(angleDegrees);
        Quaternion quat = new Quaternion().fromAngleAxis(angleRadians, new Vector3f(0,1,0));
        cameraSG.setLocalRotation(quat);
        //Translate the camera so it's in front of the model
        cameraSG.setLocalTranslation(0f, 0f, -0.5f);
        //Create a camera component
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cn,
                IMAGE_WIDTH, IMAGE_HEIGHT, 90.0f, 16/9, 0.1f, 10000f, false);
        //Set the camera for the render buffer
        textureBuffer.setCameraComponent(cc);
        //Add the render buffer to the render manager
        wm.getRenderManager().addRenderBuffer(textureBuffer);
        textureBuffer.setRenderUpdater(this);

        //Add the camera component to the quad entity
        quadEntity.addComponent(CameraComponent.class, cc);

        //Create a texture state
        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        ts.setEnabled(true);
        //Set its texture to be the texture of the render buffer
        ts.setTexture(textureBuffer.getTexture());
        quadGeo.setRenderState(ts);

        RenderComponent quadRC = wm.getRenderManager().createRenderComponent(quadNode);
        quadEntity.addComponent(RenderComponent.class, quadRC);

        device.attachChild(quadNode);
        device.attachChild(cameraSG);         

        createCaptureComponent(IMAGE_WIDTH, IMAGE_HEIGHT);

        return quadEntity;
    }

    /**x
     * Reset the counter that's used to name the images
     */
    void resetImageCounter() {
        imageCounter = 1000000;
    }

    /**
     * Reset the field that counts the number of frames captured.
     */
    void resetFrameCounter() {
        frameCounter = 0;
    }

    private void createCaptureComponent(int width, int height) {
        captureComponent = new CaptureComponent();
        captureComponent.setPreferredSize(new Dimension(width, height));
    }    

    public class CaptureComponent extends JComponent {
        public CaptureComponent() {
            setBorder(BorderFactory.createLineBorder(Color.black));
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            if (captureImage != null) {
                g.drawImage(captureImage, 0, 0, null);
            }
        }
    }

    private BufferedImage createBufferedImage(ByteBuffer bb) {
        int width = textureBuffer.getWidth();
        int height = textureBuffer.getHeight();

        bb.rewind();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                int index = (y*width + x)*3;
                int b = bb.get(index);
                int g = bb.get(index+1);
                int r = bb.get(index+2);

                int pixel = ((r&255)<<16) | ((g&255)<< 8) | ((b&255)) | 0xff000000;

                bi.setRGB(x, (height-y)-1, pixel);
            }
        }
        return (bi);
    }

    public void update(Object arg0) {
        //System.err.println("Update object: " + arg0);
        captureImage = createBufferedImage(textureBuffer.getTextureData());
        captureComponent.repaint();       

        if (((MovieRecorderCell) cell).isLocalRecording()) {
                BufferedImage outputImage = createBufferedImage(textureBuffer.getTextureData());
            // write to disk....
            try {
                File outputFile = new File(MovieRecorderCell.getImageDirectory()  + File.separator + imageCounter + ".jpg");
                if (outputFile != null) {
                    ImageIO.write(outputImage, "jpg", outputFile);
                } else {
                    rendererLogger.warning("Failed to create temporary image file");
                }
            } catch (FileNotFoundException e) {
                rendererLogger.log(Level.WARNING, "failed to write temporary file", e);
            } catch ( IOException e ) {
                System.err.println("I/O exception in update: " + e);
                e.printStackTrace();
                ((MovieRecorderCell) cell).stopRecording();
            }

            imageCounter++;
            frameCounter++;
        }
    }

    void captureImage(String stillCaptureDirectory) {
        BufferedImage outputImage = createBufferedImage(textureBuffer.getTextureData());
        Calendar calendar = Calendar.getInstance();
        String imageFilename = "Wonderland_" + STILL_IMAGE_DATE_FORMAT.format(calendar.getTime()) + ".jpg";
        try {
            File outputFile = new File(stillCaptureDirectory + File.separator + imageFilename);
            ImageIO.write(outputImage, "jpg", outputFile);
        } catch (IOException e) {
            System.err.println("I/O exception in update: " + e);
            e.printStackTrace();
        }
    }

    class CameraListener extends EventClassListener {

        CameraListener() {
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
            if (!mbe.isClicked()) {
                return;
            }
            //ignore any mouse button that isn't the left one
            if (mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }
            TriMesh mesh = mbe.getPickDetails().getTriMesh();
            rendererLogger.info("mesh: " + mesh);
            if (mesh == videoSpatial || mesh == videoSpatialOn) {
                rendererLogger.info("video button pressed");
                DefaultButtonModel videoButtonModel = ((MovieRecorderCell)cell).getVideoButtonModel();
                if (videoButtonModel.isEnabled()) {
                    videoButtonModel.setSelected(!videoButtonModel.isSelected());
                }
                //done
                return;
            }
            if (mesh == stillSpatial || mesh == stillSpatialOn) {
                rendererLogger.info("still button pressed");
                DefaultButtonModel stillButtonModel = ((MovieRecorderCell)cell).getStillButtonModel();
                if (stillButtonModel.isEnabled()) {
                    stillButtonModel.setSelected(!stillButtonModel.isSelected());
                }
                //done
                return;
            }
        }
    }

    class VideoButtonListener implements ItemListener {

        public void itemStateChanged(ItemEvent event) {
            //update the renderer
            rendererLogger.info("event: " + event);
            WorldManager wm = ClientContextJME.getWorldManager();
            if (event.getStateChange() == ItemEvent.SELECTED) {
                playAudioResource("movierecorder-start.au");
                videoSpatial.setVisible(false);
                videoSpatialOn.setVisible(true);
                recordStatus.setVisible(false);
                recordStatusOn.setVisible(true);
            } else {
                playAudioResource("movierecorder-stop.au");
                videoSpatial.setVisible(true);
                videoSpatialOn.setVisible(false);
                recordStatus.setVisible(false);
                recordStatusOn.setVisible(false);
            }
            wm.addToUpdateList(videoSpatial);
            wm.addToUpdateList(videoSpatialOn);
        }

        private void playAudioResource(String audioResource) {
            AudioInputStream audioInputStream = null;
                try {
                    URL url = MovieRecorderCell.class.getResource("resources/" + audioResource);
                    audioInputStream = AudioSystem.getAudioInputStream(url);
                    AudioFormat audioFormat = audioInputStream.getFormat();
                    DataLine.Info dataLineInfo = new DataLine.Info(Clip.class, audioFormat);
                    Clip clip = (Clip) AudioSystem.getLine(dataLineInfo);
                    clip.open(audioInputStream);
                    clip.start();
                } catch (UnsupportedAudioFileException ex) {
                    rendererLogger.log(Level.SEVERE, null, ex);
                } catch (LineUnavailableException ex) {
                    rendererLogger.log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    rendererLogger.log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        audioInputStream.close();
                    } catch (IOException ex) {
                        rendererLogger.log(Level.SEVERE, null, ex);
                    }
                }
        }

    }
}
