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
import com.jme.bounding.BoundingSphere;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.renderer.Camera;
import com.sun.scenario.animation.Animation;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;


import java.nio.ByteBuffer;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.TextureRenderBuffer;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.BufferUpdater;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.util.FrameBufferCapture;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;

/**
 *
 * @author bh37721
 */
public class MovieRecorderCellRenderer extends BasicRenderer implements BufferUpdater, RenderUpdater {

    private static final Logger rendererLogger = Logger.getLogger(MovieRecorderCellRenderer.class.getName());

    public static final float WIDTH = 0.6f; //x-extent
    public static final float HEIGHT = WIDTH /2 ; //y-extent was 0.3f
    public static final float DEPTH = 0.05f; //z-extent
    private static final int IMAGE_HEIGHT = 480;
    private static final int IMAGE_WIDTH = 640;
    /**
     *Counter for naming images
     **/
    private int imageCounter;
    /**
     *Counter for frames
     **/
    private int frameCounter;

    //private Vector3f cPos = new Vector3f(0.0f, 2.0f, -1.0f);
    //private Vector3f cUp = new Vector3f(0.0f, 1.0f, 0.0f);
    //private Vector3f cLook = new Vector3f(0.0f, 2.0f, -2.0f);
    TextureRenderBuffer textureBuffer = null;
    CaptureComponent captureComponent = null;
    BufferedImage captureImage = null;

    public MovieRecorderCellRenderer(Cell cell) {
        super(cell);
    }

    protected Node createSceneGraph(Entity entity) {
        /* Create the scene graph object*/
        Node root = new Node();
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
        addOuterCasing(device);
        entity.addEntity(createLCDPanel(device));
    }

    private void addOuterCasing(Node device) {
        Box casing = new Box("Movie Recorder Casing", new Vector3f(0, 0, 0), WIDTH, HEIGHT, DEPTH); //x, y, z
        casing.setModelBound(new BoundingBox());
        casing.updateModelBound();
        ColorRGBA casingColour = new ColorRGBA(0f, 0f, 1f, 0.2f);
        MaterialState matState = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
        matState.setDiffuse(casingColour);
        casing.setRenderState(matState);
        //casing.setLightCombineMode(Spatial.LightCombineMode.Off);
        BlendState as = (BlendState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Blend);
        as.setEnabled(true);
        as.setBlendEnabled(true);
        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        casing.setRenderState(as);

        CullState cs = (CullState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Cull);
        cs.setEnabled(true);
        cs.setCullFace(CullState.Face.Back);
        casing.setRenderState(cs);
        device.attachChild(casing);
    }

    private Entity createLCDPanel(Node device) {
        float aspect = 640 / 480;
        WorldManager wm = ClientContextJME.getWorldManager();
        //Node for the quad
        Node quadNode = new Node();
        //Geometric
        Quad quadGeo = new Quad("Ortho", 2 * WIDTH, 2 * HEIGHT);
        //Entity for the quad
        Entity quadEntity = new Entity("Ortho ");
        //Attach the geometric to the node
        quadNode.attachChild(quadGeo);
        //Set the quad node position at the +Z of the movie recorder
        quadNode.setLocalTranslation(0.0f, 0.0f,  DEPTH * 1.01f);
        
        textureBuffer = (TextureRenderBuffer) wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.TEXTURE_2D, IMAGE_WIDTH, IMAGE_HEIGHT);
        //Create a camera node
        CameraNode cn = new CameraNode("MyCamera", null);
        //Create a node for the camera
        Node cameraSG = new Node();
        //Attach the camera to the node
        cameraSG.attachChild(cn);
        //Rotate the camera through 180 degrees about the Y-axis
        float angleDegrees = 180;
        float angleRadians = (float) Math.toRadians(angleDegrees);
        Quaternion quat = new Quaternion().fromAngleAxis(angleRadians, new Vector3f(0,1,0));
        cameraSG.setLocalRotation(quat);
        //Create a camera component
        //NOT SURE ABOUT THE FRONT AND BACK CLIPPING
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cn,
                IMAGE_WIDTH, IMAGE_HEIGHT, 90.0f, aspect, 0.1f, 1000.0f, false);
        //Set the camera for the render buffer
        textureBuffer.setCameraComponent(cc);
        //Add the render buffer to the render manager
        wm.getRenderManager().addRenderBuffer(textureBuffer);
        textureBuffer.setRenderUpdater(this);
        textureBuffer.setBufferUpdater(this);

        //Add the camera component to the quad entity
        quadEntity.addComponent(CameraComponent.class, cc);

        //Create a texture state
        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        ts.setEnabled(true);
        //??Set its texture to be the texture of the render buffer??
        ts.setTexture(textureBuffer.getTexture());
        quadGeo.setRenderState(ts);

        RenderComponent quadRC = wm.getRenderManager().createRenderComponent(quadNode);
        //quadRC.setOrtho(false);
        //quadRC.setLightingEnabled(false);
        quadEntity.addComponent(RenderComponent.class, quadRC);

        device.attachChild(quadNode);
        device.attachChild(cameraSG);
         

        createPreviewFrame(IMAGE_WIDTH, IMAGE_HEIGHT);
        

        return quadEntity;
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

        /**
     * Create all of the Swing windows - and the 3D window
     */
    private void createPreviewFrame(int width, int height) {
        SwingFrame frame = new SwingFrame(width, height);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame
        //frame.setVisible(true);
    }

    class SwingFrame extends JFrame {

        JPanel contentPane;
        JPanel capturePanel = new JPanel();

        // Construct the frame
        public SwingFrame(int width, int height) {
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    dispose();
                    // TODO: Real cleanup
                    //System.exit(0);
                }
            });

            contentPane = (JPanel) this.getContentPane();
            contentPane.setLayout(new BorderLayout());

            captureComponent = new CaptureComponent();
            captureComponent.setPreferredSize(new Dimension(width, height));
            capturePanel.setLayout(new GridBagLayout());
            //capturePanel.add(captureComponent);
            //contentPane.add(capturePanel, BorderLayout.CENTER);

            pack();
        }
    }


    public class CaptureComponent extends JComponent {
        public CaptureComponent() {
            setBorder(BorderFactory.createLineBorder(Color.black));
        }
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 400, 300);
            if (captureImage != null) {
                g.drawImage(captureImage, 0, 0, null);
            }
        }
    }

    public void init(RenderBuffer rb) {
        //Camera camera = rb.getCameraComponent().getCamera();
        //camera.setLocation(cPos);
        //camera.lookAt(cLook, cUp);
    }

    BufferedImage createBufferedImage(ByteBuffer bb) {
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

    // Make this buttonEntity pickable by adding a collision component to it
    protected void makeEntityPickable(Entity entity, Node node) {
        JMECollisionSystem collisionSystem = (JMECollisionSystem) ClientContextJME.getWorldManager().getCollisionManager().
                loadCollisionSystem(JMECollisionSystem.class);

        CollisionComponent cc = collisionSystem.createCollisionComponent(node);
        entity.addComponent(CollisionComponent.class, cc);
    }

    // This is used in the texture Case
    public void update(Object arg0) {
        //System.err.println("Update object: " + arg0);
        captureImage = createBufferedImage(textureBuffer.getTextureData());
        captureComponent.repaint();
        

        if (((MovieRecorderCell) cell).isRecording()) {
                //System.err.println("Capturing image " + imageCounter);
                BufferedImage outputImage = createBufferedImage(textureBuffer.getTextureData());
                //System.err.println("image: " + outputImage);
                // write to disk....
            try {

                //FileOutputStream outputFile = new FileOutputStream(((MovieRecorderCell)cell).getImageDirectory()  + File.separator + imageCounter + ".jpg");
                //JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(outputFile);
                //JPEGEncodeParam jpegParam = jpegEncoder.getDefaultJPEGEncodeParam(outputImage);
                //jpegParam.setQuality(0.9f,false); // 90% quality JPEG

                //jpegEncoder.setJPEGEncodeParam(jpegParam);
                //jpegEncoder.encode(outputImage);
                File outputFile = new File(((MovieRecorderCell)cell).getImageDirectory()  + File.separator + imageCounter + ".jpg");
                ImageIO.write(outputImage, "jpg", outputFile);
                //outputFile.close();
            } catch ( IOException e ) {
                System.err.println("I/O exception in postSwap: " + e);
                e.printStackTrace();
                ((MovieRecorderCell) cell).stop();
            }

            imageCounter++;
            frameCounter++;
        }
    }
}
