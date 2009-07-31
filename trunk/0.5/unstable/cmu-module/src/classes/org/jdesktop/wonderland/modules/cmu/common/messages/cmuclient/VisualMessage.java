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
package org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient;

import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.scene.TriMesh;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serializable information about a CMU visual; stores geometry and texture
 * information, as well as transformation information (as a complete
 * TransformationMessage.  Associated with a unique node ID.
 * @author kevin
 */
public class VisualMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Collection<TriMesh> meshes = new Vector<TriMesh>();
    private final TransformationMessage transformation;
    private int[] texturePixels;
    private int textureWidth,  textureHeight;

    /**
     * Basic constructor.
     */
    public VisualMessage() {
        transformation = new TransformationMessage();
    }

    /**
     * Constructor with ID.
     * @param nodeID The node ID of the relevant visual
     */
    public VisualMessage(int nodeID) {
        this();
        transformation.setNodeID(nodeID);
    }

    /**
     * Get the node ID of the relevant visual.
     * @return Current ID
     */
    public int getNodeID() {
        return this.transformation.getNodeID();
    }

    /**
     * Set the node ID of the relevant visual (also updates the underlying
     * TransformationMessage).
     * @param nodeID New ID
     */
    public void setNodeID(int nodeID) {
        this.transformation.setNodeID(nodeID);
    }

    /**
     * Get transformation info as a TransformationMessage, which can be updated
     * directly.
     * @return Updatable transformation information
     */
    public TransformationMessage getTransformation() {
        return this.transformation;
    }

    /**
     * Get the TriMesh'es associated with this visual (generally one per
     * CMU geometry).
     * @return Updatable collection of TriMesh'es
     */
    public Collection<TriMesh> getMeshes() {
        synchronized(this.meshes) {
            return this.meshes;
        }
    }

    /**
     * Add a mesh to this visual (generally one mesh per CMU geometry
     * is added).
     * @param mesh The mesh to add
     */
    public void addMesh(TriMesh mesh) {
        synchronized(this.meshes) {
            this.meshes.add(mesh);
        }
    }

    /**
     * Set the texture for this visual.  The image is converted to and stored
     * in serializable form.
     * @param texture The image to use as the texture
     * @param width Width of the image
     * @param height Height of the image
     */
    public void setTexture(Image texture, int width, int height) {
        texturePixels = new int[width * height];
        textureWidth = width;
        textureHeight = height;

        PixelGrabber pg = new PixelGrabber(texture, 0, 0, width, height, texturePixels, 0, width);

        try {
            pg.grabPixels();
        } catch (InterruptedException ex) {
            Logger.getLogger(VisualMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get the texture for this visual as an image.
     * @return Texture for this visual
     */
    public Image getTexture() {
        MemoryImageSource mis = new MemoryImageSource(textureWidth, textureHeight,
                texturePixels, 0, textureWidth);
        Toolkit tk = Toolkit.getDefaultToolkit();
        return tk.createImage(mis);
    }

    /**
     * Convenience method to set scale on the underlying transformation.
     * @param scale New scale
     */
    public void setScale(float scale) {
        this.transformation.setScale(scale);
    }

    /**
     * Convenience method to set translation on the underlying transformation.
     * @param translation New transformation
     */
    public void setTranslation(Vector3f translation) {
        this.transformation.setTranslation(translation);
    }

    /**
     * Convenience method to set rotation on the underlying transformation.
     * @param rotation New roation
     */
    public void setRotation(Matrix3f rotation) {
        this.transformation.setRotation(rotation);
    }

    @Override
    public String toString() {
        return "Visual message: [NodeID:" + getNodeID() + "]";
    }
}