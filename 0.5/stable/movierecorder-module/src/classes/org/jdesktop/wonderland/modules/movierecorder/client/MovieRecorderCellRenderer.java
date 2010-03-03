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

package org.jdesktop.wonderland.modules.movierecorder.client;

import org.jdesktop.wonderland.client.utils.AudioResource;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Spatial.LightCombineMode;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
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
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.jdesktop.mtgame.RenderManager;
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
    /** The depth of the powerButtonBox that displays the image */
    private static final float IMAGE_DEPTH = 0.00001f;
    private static final float IMAGE_SCALE_FACTOR = 0.001f;

    //Use 16:9 aspect ratio
    public static final float WIDTH = 1.6f; //x-extent
    public static final float HEIGHT = 0.9f ; //y-extent
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
    private Spatial videoSpatial, videoSpatialOn, recordStatus, recordStatusOn, stillSpatial, stillSpatialOn, powerButtonBox;
    private Node viewfinderNode;
    private AudioResource recorderStart, recorderStop, cameraShutter;

    public MovieRecorderCellRenderer(Cell cell) {
        super(cell);
        recorderStart = createAudioResource("movierecorder-start.au");
        recorderStop = createAudioResource("movierecorder-stop.au");
        cameraShutter = createAudioResource("Camera_Shutter.au");
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

    void setRemoteRecording(boolean b) {
        videoSpatial.setVisible(!b);
        videoSpatialOn.setVisible(!b);
    }

    private void attachRecordingDevice(Node device, Entity entity) {
        try {
            addCameraModel(device, entity);
        } catch (IOException ex) {
            rendererLogger.log(Level.SEVERE, "Failed to load camera model", ex);
        }
        entity.addEntity(createViewfinder(device));
        entity.addEntity(createPowerButton(device));
    }

    private void addCameraModel(Node device, Entity entity) throws IOException {
        //Load the cameramodel and add it to the scenegraph
        LoaderManager manager = LoaderManager.getLoaderManager();
        URL url = AssetUtils.getAssetURL("wla://movierecorder/pwl_3d_videorecorder_009.dae/pwl_3d_videorecorder_009.dae.gz.dep", this.getCell());
        DeployedModel dm = manager.getLoaderFromDeployment(url);
        Node cameraModel = dm.getModelLoader().loadDeployedModel(dm, entity);
        device.attachChild(cameraModel);

        //Get the record status nodes and set it to off
        recordStatus = ScenegraphUtils.findNamedNode(cameraModel, "vrRecordStatus_002-vrRecordStatus");
        recordStatusOn = ScenegraphUtils.findNamedNode(cameraModel, "vrRecordStatusOn-Geometry-vrRecordStatusOn");
        recordStatusOn.setVisible(false);

        //Get the video buttons
        videoSpatial = ScenegraphUtils.findNamedNode(cameraModel, "vrBtnVideo_002-vrBtnVideo");
        videoSpatialOn = ScenegraphUtils.findNamedNode(cameraModel, "vrBtnVideoOn-Geometry-vrBtnVideoOn");
        //locate "on" button so that it appears "pressed"
        videoSpatialOn.setLocalTranslation(0, -0.015f, 0);
        //"on" button is initially invisible
        videoSpatialOn.setVisible(false);        
        //create a listener to control the appearance of the video buttons
        ((MovieRecorderCell)cell).getVideoButtonModel().addItemListener(new VideoButtonListener());

        //Get the still buttons
        stillSpatial = ScenegraphUtils.findNamedNode(cameraModel, "vrBtnStill_002-vrBtnStill");
        stillSpatialOn = ScenegraphUtils.findNamedNode(cameraModel, "vrBtnStillOn-Geometry-vrBtnStillOn");
        //locate "on" button so that it appears "pressed"
        stillSpatialOn.setLocalTranslation(0, -0.015f, 0);
        //"on" button is initially invisible
        stillSpatialOn.setVisible(false);
        //create a listener to control the appearance of the still buttons
        ButtonModel stillButtonModel = ((MovieRecorderCell)cell).getStillButtonModel();
        StillButtonListener buttonListener = new StillButtonListener();
        stillButtonModel.addChangeListener(buttonListener);
        stillButtonModel.addItemListener(buttonListener);

        //Listen for mouse events
        CameraListener listener = new CameraListener();
        listener.addToEntity(entity);
    }

    private Entity createViewfinder(Node device) {
        WorldManager wm = ClientContextJME.getWorldManager();
        //Node for the viewfinder
        viewfinderNode = new Node("viewfinder");
        //Qhad to render the viewfinder
        Quad viewfinderQuad = new Quad("viewfinder", WIDTH, HEIGHT);
        viewfinderQuad.setLightCombineMode(LightCombineMode.Off);
        viewfinderQuad.updateRenderState();
        //Entity for the quad
        Entity viewfinderEntity = new Entity("viewfinder ");
        //Attach the quad to the node
        viewfinderNode.attachChild(viewfinderQuad);
        //Set the quad node position so that it is directly in front of the camera model
        //To give the appearance of an LCD panel
        viewfinderNode.setLocalTranslation(0.0f, -0.15f,  -0.045f);
        //Create the texture buffer
        textureBuffer = (TextureRenderBuffer) wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.TEXTURE_2D, IMAGE_WIDTH, IMAGE_HEIGHT);
        textureBuffer.setIncludeOrtho((false));
        //Disable the viewfinder
        //setViewfinderEnabled(false);
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
        cameraSG.setLocalTranslation(0f, 0.5f, -0.5f);
        //Create a camera component
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cn, IMAGE_WIDTH, IMAGE_HEIGHT, 45.0f, (float) IMAGE_WIDTH/ (float) IMAGE_HEIGHT, 1f, 2000f, false);
        //Set the camera for the render buffer
        textureBuffer.setCameraComponent(cc);
        // Associated the texture buffer with the render manager, but keep it
        // off initially.
        wm.getRenderManager().addRenderBuffer(textureBuffer);
        textureBuffer.setRenderUpdater(this);
        //textureBuffer.setEnable(false);

        //Add the camera component to the quad entity
        viewfinderEntity.addComponent(CameraComponent.class, cc);

        //Create a texture state
        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        ts.setEnabled(true);
        //Set its texture to be the texture of the render buffer
        ts.setTexture(textureBuffer.getTexture());
        viewfinderQuad.setRenderState(ts);

        RenderComponent quadRC = wm.getRenderManager().createRenderComponent(viewfinderNode);
        viewfinderEntity.addComponent(RenderComponent.class, quadRC);

        device.attachChild(viewfinderNode);
        device.attachChild(cameraSG);         

        createCaptureComponent(IMAGE_WIDTH, IMAGE_HEIGHT);

        //Create a listener to monitor the state of the power button
        ItemListener powerButtonListener = new PowerButtonListener();
        ((MovieRecorderCell)cell).getPowerButtonModel().addItemListener(powerButtonListener);

        return viewfinderEntity;
    }

    private Entity createPowerButton(Node aNode) {
        Node powerButtonNode = new Node("Power Button");
        //Entity for the quad
        Entity powerButtonEntity = new Entity("Power Button");
        String textureURI = "wla://movierecorder/IEC5009_Standby_Symbol.png";
        // Convert the uri given to a proper url to download
        URL url = null;
        try {
            url = getAssetURL(textureURI);
        } catch (MalformedURLException ex) {
            logger.log(Level.WARNING, "Unable to form asset url from " +
                    textureURI, ex);
            return powerButtonEntity;
        }
        // Load the texture first to get the image size
        Texture texture = TextureManager.loadTexture(url);
        texture.setWrap(Texture.WrapMode.BorderClamp);
        texture.setTranslation(new Vector3f());
        // Figure out what the size of the texture is, scale it down to something
        // reasonable.
        Image image = texture.getImage();
        float width = image.getWidth() * IMAGE_SCALE_FACTOR;
        float height = image.getHeight() * IMAGE_SCALE_FACTOR;

        // Create a box of suitable dimensions
        powerButtonBox = new Box("Box", new Vector3f(0, 0, 0), width, height, IMAGE_DEPTH);
        powerButtonNode.attachChild(powerButtonBox);
        powerButtonBox.setLightCombineMode(LightCombineMode.Off);
        powerButtonBox.updateRenderState();
        //Set the powerbutton node position so that it is directly in front of the viewfinder
        //At the bottom right
        powerButtonNode.setLocalTranslation((WIDTH/2) - (2*width), -0.15f - (HEIGHT/2) + (2*height),  -0.035f); //-0.045f
        powerButtonNode.setModelBound(new BoundingSphere());
        powerButtonNode.updateModelBound();

        // Set the texture on the node
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        TextureState ts = (TextureState)rm.createRendererState(StateType.Texture);
        ts.setTexture(texture);
        ts.setEnabled(true);
        powerButtonBox.setRenderState(ts);

        // Set the blend state so that transparent images work properly
        BlendState bs = (BlendState)rm.createRendererState(StateType.Blend);
        bs.setBlendEnabled(false);
        bs.setReference(0.5f);
        bs.setTestFunction(BlendState.TestFunction.GreaterThan);
        bs.setTestEnabled(true);
        powerButtonNode.setRenderState(bs);

        // issue #1109: prevent flickering by removing back faces
        CullState cs = (CullState)rm.createRendererState(StateType.Cull);
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);
        powerButtonNode.setRenderState(cs);

        // Make sure we do not cache the texture in memory, this will mess
        // up asset caching with WL (if the URL stays the same, but the
        // underlying asset changes).
        TextureManager.releaseTexture(texture);

        RenderComponent powerRC = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(powerButtonNode);
        powerButtonEntity.addComponent(RenderComponent.class, powerRC);
        aNode.attachChild(powerButtonNode);


        return powerButtonEntity;
    }

    /**
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

    /**
     * Sets whether the viewfinder is enabled (true) or disabled (false).
     *
     * @param isEnabled True to enable the camera, false to disable
     */
    public void setViewfinderEnabled(boolean isEnabled) {
        // If it is enabled, then turn on the rendering and the buffer
        if (textureBuffer != null) {
            //rendererLogger.info("setViewfinderEnabled: " + isEnabled);
            textureBuffer.setEnable(isEnabled);
            viewfinderNode.setVisible(isEnabled);
        }
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
        captureImage = createBufferedImage(textureBuffer.getTextureData());
                    
         try {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    captureComponent.repaint();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot capture texture buffer image");
        }
             

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

    private AudioResource createAudioResource(String audioResource) {
        URL url = MovieRecorderCell.class.getResource("resources/" + audioResource);
        return new AudioResource(url);
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
            //ignore any mouse button that isn't the left one
            if (mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }
            TriMesh mesh = mbe.getPickDetails().getTriMesh();
            //rendererLogger.info("mesh: " + mesh);
            switch (mbe.getID()) {
                case MouseEvent.MOUSE_PRESSED:
                    mousePressed(mesh);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    mouseReleased(mesh);
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    mouseClicked(mesh);
                    break;
                default:
                    rendererLogger.warning("Unhandled event: " + mbe);
            }
        }

        private void mouseClicked(TriMesh mesh) {
            if (mesh == videoSpatial || mesh == videoSpatialOn) {
                //rendererLogger.info("video button clicked");
                if (!((MovieRecorderCell) cell).getPowerButtonModel().isSelected()) {
                    //no power, can't record
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    DefaultButtonModel videoButtonModel = ((MovieRecorderCell) cell).getVideoButtonModel();
                    if (videoButtonModel.isEnabled()) {
                        videoButtonModel.setSelected(!videoButtonModel.isSelected());
                    }
                }
            }
            if (mesh == powerButtonBox) {
                MovieRecorderCell mrCell = (MovieRecorderCell) cell;
                //rendererLogger.info("clicked power button");
                if (mrCell.isLocalRecording()) {
                    //Can't turn off the power when recording
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    boolean power = mrCell.getPowerButtonModel().isSelected();
                    mrCell.getPowerButtonModel().setSelected(!power);
                }
            }
        }

        private void mousePressed(TriMesh mesh) {
            if (mesh == stillSpatial || mesh == stillSpatialOn) {
                if (!((MovieRecorderCell) cell).getPowerButtonModel().isSelected()) {
                    //no power, can't take a snapshot
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    DefaultButtonModel stillButtonModel = ((MovieRecorderCell) cell).getStillButtonModel();
                    if (stillButtonModel.isEnabled()) {
                        stillButtonModel.setPressed(true);
                        stillButtonModel.setSelected(true);
                    }
                }
            }
        }

        private void mouseReleased(TriMesh mesh) {
            if (mesh == stillSpatial || mesh == stillSpatialOn) {
                //rendererLogger.info("still button released");
                DefaultButtonModel stillButtonModel = ((MovieRecorderCell)cell).getStillButtonModel();
                if (stillButtonModel.isEnabled()) {
                    stillButtonModel.setPressed(false);
                    stillButtonModel.setSelected(false);
                }
            }
        }
    }

    class VideoButtonListener implements ItemListener {

        public void itemStateChanged(ItemEvent event) {
            //update the renderer
            //rendererLogger.info("event: " + event);
            WorldManager wm = ClientContextJME.getWorldManager();
            if (event.getStateChange() == ItemEvent.SELECTED) {
                recorderStart.play();
                videoSpatial.setVisible(false);
                videoSpatialOn.setVisible(true);
                recordStatus.setVisible(false);
                recordStatusOn.setVisible(true);
            } else {
                recorderStop.play();
                videoSpatial.setVisible(true);
                videoSpatialOn.setVisible(false);
                recordStatus.setVisible(false);
                recordStatusOn.setVisible(false);
            }
            wm.addToUpdateList(videoSpatial);
            wm.addToUpdateList(videoSpatialOn);
        }

        
    }

    class StillButtonListener implements ChangeListener, ItemListener {

        public void itemStateChanged(ItemEvent event) {
            //rendererLogger.info("event: " + event);
            if (event.getStateChange() == ItemEvent.SELECTED) {
                cameraShutter.play();
            }
        }

        public void stateChanged(ChangeEvent event) {
            //rendererLogger.info("event: " + event);
            WorldManager wm = ClientContextJME.getWorldManager();
            DefaultButtonModel stillButtonModel = ((MovieRecorderCell)cell).getStillButtonModel();
            if (stillButtonModel.isPressed()) {
                stillSpatial.setVisible(false);
                stillSpatialOn.setVisible(true);
            } else {
                stillSpatial.setVisible(true);
                stillSpatialOn.setVisible(false);
            }
            wm.addToUpdateList(stillSpatial);
            wm.addToUpdateList(stillSpatialOn);
        }
    }

    class PowerButtonListener implements ItemListener {

        public void itemStateChanged(ItemEvent event) {
            //update the renderer
            //rendererLogger.info("power button model:" + event);
            WorldManager wm = ClientContextJME.getWorldManager();
            if (event.getStateChange() == ItemEvent.SELECTED) {
                setViewfinderEnabled(true);
            } else {
                setViewfinderEnabled(false);
                captureImage = null;
            }
            wm.addToUpdateList(viewfinderNode);
        }
    }
}
