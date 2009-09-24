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
package org.jdesktop.wonderland.modules.topmap.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.TextureRenderBuffer;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * An Entity that contains a Camera that can be attached to the world and that
 * can track the movements of a Cell.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class TopMapCameraEntity extends Entity implements RenderUpdater {

    // The buffer into which the camera is renderer, so we can copy it into
    // a BufferedImage
    private TextureRenderBuffer textureBuffer = null;

    // The JComponent into which the camera scene should be drawn
    private CaptureJComponent captureComponent = null;

    // The elevation of the camera node
    private float elevation = 0.0f;

    // The scene graph node for the position of the camera
    private Node cameraNode = null;

    /**
     * Default constructor, takes the JComponent into which it should draw
     * the camera scene.
     */
    public TopMapCameraEntity(CaptureJComponent capture, float elevation) {
        super("Top Camera Entity");
        this.captureComponent = capture;
        this.elevation = elevation;

        // Creates the scene graph and attach to the entity
        createTopMap();
    }

    /**
     * Sets the elevation of the camera.
     *
     * @param elevation The new elevation of the camera
     */
    public void setElevation(final float elevation) {
        // Update the elevation, we must alter the camera in the MT Game
        // render thread.
        this.elevation = elevation;
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                Vector3f translation = cameraNode.getLocalTranslation();
                float x = translation.getX();
                float y = elevation;
                float z = translation.getZ();
                cameraNode.setLocalTranslation(x, y, z);
                ClientContextJME.getWorldManager().addToUpdateList(cameraNode);
            }
        });
    }

    /**
     * Creates the scene graph for the camera map Entity.
     */
    private void createTopMap() {

        // Fetch the world and render managers for use
        final WorldManager wm = ClientContextJME.getWorldManager();
        final RenderManager rm = wm.getRenderManager();

        // Create a buffer into which the camera scene is renderer
        BufferedImage bufferedImage = captureComponent.getBufferedImage();
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        textureBuffer = (TextureRenderBuffer) rm.createRenderBuffer(
                RenderBuffer.Target.TEXTURE_2D, width, height);
        textureBuffer.setIncludeOrtho(false);

        // Figure out what the initial position of the camera should be.
        ViewCell viewCell = ViewManager.getViewManager().getPrimaryViewCell();
        float x = 0.0f, y = elevation, z = 0.0f;
        if (viewCell != null) {
            CellTransform transform = viewCell.getWorldTransform();
            Vector3f translation = transform.getTranslation(null);
            x = translation.x;
            z = translation.z;
        }

        // Create the new camera and a node to hold it. We attach this to the
        // Entity
        cameraNode = new Node();
        cameraNode.setLocalTranslation(x, y, z);
        float angle = (float)Math.toRadians(90.0f);
        Quaternion rot = new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_X);
        cameraNode.setLocalRotation(rot);
        CameraNode cn = new CameraNode("Top Camera", null);
        cameraNode.attachChild(cn);

        // Create a camera component and associated with the texture buffer we
        // have created.
        CameraComponent cc = rm.createCameraComponent(
                cameraNode,      // The Node of the camera scene graph
                cn,              // The Camera
                width,           // Viewport width
                height,          // Viewport height
                90.0f,           // Field of view
                1.0f,            // Aspect ratio
                1.0f,            // Front clip
                3000.0f,         // Rear clip
                false            // Primary?
                );

        // Create a camera component and associated with the texture buffer we
        // have created. This usage creates a parallel projection, the extent
        // of the scene is given by a (left, right, bottom, top) quad.
//        CameraComponent cc = rm.createCameraComponent(
//                cameraNode,      // The Node of the camera scene graph
//                cn,              // The Camera
//                width,           // Viewport width
//                height,          // Viewport height
//                1.0f,            // Front clip
//                3000.0f,         // Rear clip
//                -10.0f,          // Left extent
//                10.0f,           // Right extent
//                -10.0f,          // Botton extent
//                10.0f,           // Top extent
//                false            // Primary?
//                );

        textureBuffer.setCameraComponent(cc);
        rm.addRenderBuffer(textureBuffer);
        textureBuffer.setRenderUpdater(this);

        // Listener for changes in the position of the view cell and update
        // the camera position as a result.
        if (viewCell != null) {
            viewCell.addTransformChangeListener(new TransformChangeListener() {
                public void transformChanged(Cell cell, ChangeSource source) {
                    // Find the world transform of the view cell. We take the
                    // (x, z) of the translation and the rotation.
                   CellTransform transform = cell.getWorldTransform();
                   final Vector3f translation = transform.getTranslation(null);
                   final Quaternion rotation = transform.getRotation(null);

                   // Update the translation and rotation of the camera node.
                   // We must do this in an MT Game render thead.
                   SceneWorker.addWorker(new WorkCommit() {
                        public void commit() {
                            float x = translation.getX();
                            float y = elevation;
                            float z = translation.getZ();
                            cameraNode.setLocalTranslation(x, y, z);
                            wm.addToUpdateList(cameraNode);

//                            Quaternion q = cameraNode.getLocalRotation();
//                            cameraNode.setLocalRotation(q.mult(rotation));
                        }
                    });
                }
            });
        }

        // Add the camera component to the Entity
        addComponent(CameraComponent.class, cc);
    }

    /**
     * Takes a BufferedImage and a ByteBuffer and fills the values found in the
     * byte buffer into the buffered image, assuming consecutive RGB values.
     */
    private void fill(BufferedImage bi, ByteBuffer bb, int width, int height) {
        bb.rewind();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = (y * width + x) * 3;
                int b = bb.get(index);
                int g = bb.get(index + 1);
                int r = bb.get(index + 2);

                int pixel = ((r & 255) << 16) | ((g & 255) << 8) |
                        ((b & 255)) | 0xff000000;
                bi.setRGB(x, (height - y) - 1, pixel);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void update(Object arg0) {
        // Take the latest from the texture buffer into which we rendered
        // the camera and draw it into the buffered image and tell the
        // JComponent to repaint itself.
        BufferedImage bi = captureComponent.getBufferedImage();
        ByteBuffer bb = textureBuffer.getTextureData();
        fill(bi, bb, bi.getWidth(), bi.getHeight());
        captureComponent.repaint();
    }
}
