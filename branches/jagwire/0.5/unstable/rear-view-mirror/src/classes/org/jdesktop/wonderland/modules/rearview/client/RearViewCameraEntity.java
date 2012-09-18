/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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
package org.jdesktop.wonderland.modules.rearview.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
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
 * can track the movements of a Cell. This class assumes a primary view cell
 * exists before it is created -- if not, this class will do nothing.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class RearViewCameraEntity extends Entity implements RenderUpdater {

    // The error logger
    private static Logger LOGGER =
            Logger.getLogger(RearViewCameraEntity.class.getName());

    // The buffer into which the camera is renderer, so we can copy it into
    // a BufferedImage
    private TextureRenderBuffer textureBuffer = null;

    // The JComponent into which the camera scene should be drawn
    private CaptureJComponent captureComponent = null;

    // The scene graph node for the position of the camera
    private Node cameraNode = null;

    // The primary view cell currently in use
    private ViewCell viewCell = null;

    // The transform change listener on the view Cell
    private TransformChangeListener listener = null;

    // The height of the camera above the origin of the view Cell
    private static final float HEIGHT = 2.0f;

    // The byte array into which the camera data is copied
    int[] pixels = null;

    /**
     * Default constructor, takes the JComponent into which it should draw
     * the camera scene.
     */
    public RearViewCameraEntity(CaptureJComponent capture) {
        super("Top Camera Entity");
        this.captureComponent = capture;

        // Look up the primary view cell. Assume it exists, if not, log an
        // error and return (never creating the map).
        viewCell = ViewManager.getViewManager().getPrimaryViewCell();
        if (viewCell == null) {
            LOGGER.warning("Unable to find primary view cell, is null.");
            return;
        }

        // Creates the scene graph and attach to the entity
        createTopMap();
    }

    /**
     * Sets whether the camera is enabled (true) or disabled (false).
     *
     * @param isEnabled True to enable the camera, false to disable
     */
    public void setCameraEnabled(boolean isEnabled) {
        // If it is enabled, then turn on the rendering and the buffer
        if (textureBuffer != null) {
            textureBuffer.setEnable(isEnabled);
        }
    }

    /**
     * Dispose of any resources associated with this entity. Once called, the
     * Entity can no longer be used.
     */
    public void dispose() {
        // Remove the listener from the view cell, if there is one
        if (viewCell != null) {
            viewCell.removeTransformChangeListener(listener);
            listener = null;
        }

        // Remove the render buffer to clean it up
        if (textureBuffer != null) {
            WorldManager wm = ClientContextJME.getWorldManager();
            RenderManager rm = wm.getRenderManager();
            rm.removeRenderBuffer(textureBuffer);
        }
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
        pixels = new int[width*height];
        textureBuffer = (TextureRenderBuffer) rm.createRenderBuffer(
                RenderBuffer.Target.TEXTURE_2D, width, height);
        textureBuffer.setIncludeOrtho(false);

        // Figure out what the initial position of the camera should be from
        // the primary view Cell. This assumes the view cell is not null, which
        // is checked in the constructor.
        CellTransform transform = viewCell.getWorldTransform();
        Vector3f translation = transform.getTranslation(null);
        float x = translation.x;
        float y = translation.y + HEIGHT;
        float z = translation.z;

        // Create the new camera and a node to hold it. We attach this to the
        // Entity
        cameraNode = new Node();
        cameraNode.setLocalTranslation(x, y, z);

        // We need to turn the camera facing backwards. We also need to flip
        // it right-side up.
        float angle = (float)Math.toRadians(180.0f);
        Quaternion rotation = new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y);
        Quaternion rot = new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Z);
        rotation.multLocal(rot);
        cameraNode.setLocalRotation(rotation);

        CameraNode cn = new CameraNode("Rear View Camera", null);
        cameraNode.attachChild(cn);

        // Create a camera component and associated with the texture buffer we
        // have created.
        CameraComponent cc = rm.createCameraComponent(
                cameraNode,            // The Node of the camera scene graph
                cn,                    // The Camera
                width,                 // Viewport width
                height,                // Viewport height
                90.0f,                 // Field of view
                (float)width / height, // Aspect ratio
                1.0f,                  // Front clip
                3000.0f,               // Rear clip
                false                  // Primary?
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

        // Associated the texture buffer with the render manager, but keep it
        // off initially.
        textureBuffer.setEnable(false);
        textureBuffer.setCameraComponent(cc);
        rm.addRenderBuffer(textureBuffer);
        textureBuffer.setRenderUpdater(this);

        // Listener for changes in the position of the view cell and update
        // the camera position as a result.
        listener = new TransformChangeListener() {
            public void transformChanged(Cell cell, ChangeSource source) {
                // Find the world transform of the view cell. We take the
                // (x, z) of the translation and the rotation.
                CellTransform transform = cell.getWorldTransform();
                final Vector3f translation = transform.getTranslation(null);
                final Quaternion rotation = transform.getRotation(null);

                // We need to point the camera backwards and flip it right-side
                // up.
                float angle = (float) Math.toRadians(180.0f);
                Quaternion rot = new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y);
                rotation.multLocal(rot);
                rot = new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Z);
                rotation.multLocal(rot);

                // Update the translation and rotation of the camera node.
                // We must do this in an MT Game render thead.
                SceneWorker.addWorker(new WorkCommit() {
                    public void commit() {
                        float x = translation.getX();
                        float y = translation.getY() + HEIGHT;
                        float z = translation.getZ();
                        cameraNode.setLocalTranslation(x, y, z);
                        cameraNode.setLocalRotation(rotation);
                        
                        wm.addToUpdateList(cameraNode);
                    }
                });
            }
        };
        viewCell.addTransformChangeListener(listener);

        // Add the camera component to the Entity
        addComponent(CameraComponent.class, cc);
    }

    /**
     * Takes a BufferedImage and a ByteBuffer and fills the values found in the
     * byte buffer into the buffered image, assuming consecutive RGB values.
     */
    private void fill(BufferedImage bi, ByteBuffer bb, int width, int height) {
        bb.rewind();
        int pi = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = (y * width + x) * 3;
                int b = bb.get(index);
                int g = bb.get(index + 1);
                int r = bb.get(index + 2);

                pixels[pi++] = ((r & 255) << 16) | ((g & 255) << 8) |
                        ((b & 255)) | 0xff000000;
                //bi.setRGB(x, (height - y) - 1, pixel);
            }
        }
        bi.setRGB(0, 0, width, height, pixels, 0, width);
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

        // OWL issue #153: do this on the AWT event thread to prevent
        // a deadlock on Linux
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                captureComponent.repaint();
            }
        });
    }
}
