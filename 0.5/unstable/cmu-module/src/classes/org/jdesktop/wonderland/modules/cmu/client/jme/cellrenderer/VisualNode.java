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
package org.jdesktop.wonderland.modules.cmu.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.Triangle;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureKey;
import com.jme.util.TextureManager;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes.VisualRepoIdentifier;

/**
 * Node encapsulating a visual element of the CMU scene.  These nodes are
 * assigned unique IDs so that transformation updates can be sent by the CMU
 * program player.
 * @author kevin
 */
public class VisualNode extends VisualParent {

    private final NodeID nodeID;     // Unique ID for this node
    private BoundingBox bound = null;
    private static final Map<VisualRepoIdentifier, TextureKey> keyMap = new HashMap<VisualRepoIdentifier, TextureKey>();
    private static final String[] groundPlaneNames = {
        "Ground.m_sgVisual", // Suffixes
        "Surface.m_sgVisual"};

    public enum VisualType {

        ANY_VISUAL,
        GROUND,
    }

    /**
     * Constructs this visual with the properties
     * contained in the given VisualMessage.
     * @param message The message to be used in creating this node
     */
    public VisualNode(NodeID nodeID) {
        super();
        this.nodeID = nodeID;
    }

    public boolean isType(VisualType type) {
        if (type == VisualType.ANY_VISUAL) {
            return true;
        } else if (type == VisualType.GROUND) {
            for (String planeName : groundPlaneNames) {
                if (this.getName().endsWith(planeName)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * Apply the properties contained in this VisualMessage (i.e.
     * geometries, textures, etc.) to this node.
     * @param message The message to apply
     */
    public void applyVisual(VisualAttributes attributes) {
        // Set name
        this.setName(attributes.getName());

        // Apply geometries, compute bounding box
        bound = null;
        for (TriMesh mesh : attributes.getMeshes()) {
            this.attachChild(mesh);

            // Merge this bounding box with the cumulative bound
            Triangle[] tris = new Triangle[mesh.getTriangleCount()];

            BoundingBox currentBound = new BoundingBox();
            currentBound.computeFromTris(mesh.getMeshAsTriangles(tris), 0, tris.length);

            if (bound == null) {
                bound = currentBound;
            } else {
                bound.mergeLocal(currentBound);
            }
        }
        if (bound != null) {
            this.setModelBound(bound);
        }

        // Apply texture
        this.applyTexture(attributes);
    }

    /**
     * Get the unique node ID for this visual.
     * @return This visual's node ID
     */
    public NodeID getNodeID() {
        return this.nodeID;
    }

    /**
     * Apply the given transformation to this node if it matches this node's
     * ID.  Call this function recursively on this node's children.
     * @param transformation {@inheritDoc}
     */
    @Override
    public synchronized VisualNode applyTransformationToChild(TransformationMessage transformation) {
        if (transformation.getNodeID().equals(this.getNodeID())) {
            this.applyTransformation(transformation);
            return this;
        } else {
            return super.applyTransformationToChild(transformation);
        }
    }

    /**
     * Recursively remove the node with ID given by the VisualDeletedMessage.
     * @param deleted Message specifying the node to remove.
     * @return True if this node should be deleted
     */
    @Override
    public synchronized boolean removeChild(NodeID nodeID) {
        super.removeChild(nodeID);
        if (nodeID.equals(this.getNodeID())) {
            return true;
        }
        return false;
    }

    @Override
    public synchronized void applyVisibilityToChild(VisualType type, boolean visible) {
        super.applyVisibilityToChild(type, visible);
        if (this.isType(type)) {
            this.setPartOfWorld(visible);
        }
    }

    public synchronized void setPartOfWorld(boolean partOfWorld) {
        setVisible(partOfWorld);
        setModelBound(partOfWorld ? bound : null);
    }

    /**
     * Schedule the transformation to be applied in a RenderUpdater to this
     * node.
     * @param transformation The transformation to be applied
     */
    protected void applyTransformation(TransformationMessage transformation) {

        for (Spatial mesh : VisualNode.this.getChildren()) {
            mesh.setLocalScale(transformation.getScale());
            mesh.setLocalTranslation(transformation.getTranslation());
            mesh.setLocalRotation(transformation.getRotation());
        }
    }

    /**
     * Apply the given texture to this node.
     * @param texture The texture to apply, as a standard Image
     */
    protected void applyTexture(VisualAttributes attributes) {
        TextureState ts = (TextureState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.Texture);
        Texture t = getTexture(attributes);
        t.setWrap(Texture.WrapMode.Repeat);
        ts.setTexture(t);
        ts.setEnabled(true);
        this.setRenderState(ts);
    }

    /**
     * Get the texture associated with the given attributes; uses TextureKey's
     * in a map to take advantage of the TextureManager's built-in caching.
     * @param attributes The VisualAttributes to extract a texture from
     * @return The extracted texture
     */
    private static Texture getTexture(VisualAttributes attributes) {
        //TODO: clean up textures
        synchronized (keyMap) {
            Texture toReturn = null;
            if (keyMap.containsKey(attributes.getID())) {
                toReturn = TextureManager.loadTexture(keyMap.get(attributes.getID()));
            }
            if (toReturn == null) {
                Image textureImage = attributes.getTexture();
                toReturn = null;
                toReturn = TextureManager.loadTexture(textureImage, Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear, false);
                keyMap.put(attributes.getID(), toReturn.getTextureKey());
            }
            return toReturn;
        }
    }
}
