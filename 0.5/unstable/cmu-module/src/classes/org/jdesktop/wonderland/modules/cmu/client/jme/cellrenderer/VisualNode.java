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
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureKey;
import com.jme.util.TextureManager;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.cmu.client.CMUCell;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.VisualType;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.AppearancePropertyMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualPropertyMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.NodeUpdateMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualDeletedMessage;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes.VisualAttributesIdentifier;

/**
 * Node encapsulating a visual element of the CMU scene.  These nodes are
 * assigned unique IDs so that updates can be applied appropriately by the CMU
 * program player.
 * @author kevin
 */
public class VisualNode extends VisualParent {

    private final NodeID nodeID;     // Unique ID for this node
    private final CMUCell parentCell;
    private boolean visibleInCMU = false;
    private final Object visibleInCMULock = new Object();
    private BoundingBox bound = null;
    private static final Map<VisualAttributesIdentifier, TextureKey> keyMap = new HashMap<VisualAttributesIdentifier, TextureKey>();
    private static final String[] groundPlaneNames = {
        // Suffixes
        "Ground.m_sgVisual",
        "Surface.m_sgVisual",};

    /**
     * Standard constructor; no visual properties loaded.
     * @param nodeID The unique ID for this visual
     * @param parentCell The CMU cell which is the parent of this visual
     */
    public VisualNode(NodeID nodeID, CMUCell parentCell) {
        super();
        this.nodeID = nodeID;
        this.parentCell = parentCell;

        // Add light state
        LightState lightState = (LightState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Light);
        lightState.setEnabled(true);

        this.setRenderState(lightState);
        this.updateRenderState();

        // Add material state
        MaterialState materialState = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
        materialState.setEnabled(true);

        materialState.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);

        this.setRenderState(materialState);
        this.updateRenderState();

        // Add blend state
        BlendState alphaState = (BlendState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Blend);
        alphaState.setBlendEnabled(true);
        alphaState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alphaState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alphaState.setTestEnabled(true);
        alphaState.setTestFunction(BlendState.TestFunction.GreaterThan);

        alphaState.setEnabled(true);

        this.setRenderState(alphaState);
        this.updateRenderState();

        // Enable transparency
        this.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
    }

    /**
     * Find out whether the visual represented by this node fits into a
     * particular visual category.
     * @param type The visual category to check
     * @return Whether this node is in the given category
     */
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
     * Apply the properties contained in this VisualAttributes (i.e.
     * geometries, textures, etc.) to this node.
     * @param attributes The attributes to apply
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
     * Find out whether this node is visible in the CMU scene (note that
     * this is independent of its visibility in the CMU cell, i.e. a ground
     * plane may be showing in the CMU scene but not in the cell).
     * @return Whether this node is visible in the CMU scene
     */
    public boolean isVisibleInCMU() {
        synchronized (visibleInCMULock) {
            return visibleInCMU;
        }
    }

    /**
     * Notify this node of its visibility in the CMU scene.
     * @param visibleInCMU Whether this node is visible in the CMU scene
     */
    public void setVisibleInCMU(boolean visibleInCMU) {
        synchronized (visibleInCMULock) {
            this.visibleInCMU = visibleInCMU;
        }
    }

    /**
     * Apply the given update to this node if it matches this node's
     * ID.  Call this function recursively on this node's children.
     * @param message {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public VisualParent applyUpdateToDescendant(NodeUpdateMessage message) {
        if (message.getNodeID().equals(this.getNodeID())) {
            if (message instanceof TransformationMessage) {
                this.applyTransformation((TransformationMessage) message);
            } else if (message instanceof VisualPropertyMessage) {
                this.applyVisualProperties((VisualPropertyMessage) message);
            } else if (message instanceof AppearancePropertyMessage) {
                this.applyAppearanceProperties((AppearancePropertyMessage) message);
            } else {
                Logger.getLogger(VisualNode.class.getName()).severe("Unknown message: " + message);
            }
            return this;
        } else {
            return super.applyUpdateToDescendant(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeDescendant(VisualDeletedMessage visualDeletedMessage) {
        super.removeDescendant(visualDeletedMessage);
        if (visualDeletedMessage.getNodeID().equals(this.getNodeID())) {
            return true;
        }
        return false;
    }

    /**
     * Recursively update the visibility of this node's children; also determine
     * the current actual visibility of this node (based on its visibility in
     * both the CMU scene and its CMU cell), and set its visibility accordingly.
     */
    @Override
    public void updateVisibility() {
        super.updateVisibility();

        // Check visibility in both CMU and the associated cell
        setPartOfWorld(isVisibleInCMU() && parentCell.isVisibleInCell(this));
    }

    /**
     * Set the visibility of this node, as well as its bounds (so that
     * collision detection happens on and only on visible nodes).
     * @param partOfWorld Whether this node should be visible in the world
     */
    public void setPartOfWorld(boolean partOfWorld) {
        setVisible(partOfWorld);
        setModelBound(partOfWorld ? bound : null);
    }

    /**
     * Apply the given transformation to this node.
     * @param transformation The transformation to be applied
     */
    protected void applyTransformation(TransformationMessage transformation) {
        if (getChildren() != null) {
            for (Spatial mesh : getChildren()) {
                mesh.setLocalTranslation(transformation.getTranslation());
                mesh.setLocalRotation(transformation.getRotation());
            }
        }
    }

    /**
     * Apply the given visual properties from the CMU scene to this node.
     * @param properties The properties to be applied
     */
    protected void applyVisualProperties(VisualPropertyMessage properties) {
        if (getChildren() != null) {
            for (Spatial mesh : getChildren()) {
                mesh.setLocalScale(properties.getScale());
            }
        }
        setVisibleInCMU(properties.isVisible());
    }

    /**
     * Apply the given appearance properties from the CMU scene to this node.
     * @param appearanceProperties The properties to be applied
     */
    protected void applyAppearanceProperties(AppearancePropertyMessage appearanceProperties) {
        //TODO: Handle diffuse color with texture
        MaterialState materialState = (MaterialState) this.getRenderState(StateType.Material);
        materialState.setAmbient(appearanceProperties.getAmbientColor());
        materialState.setDiffuse(appearanceProperties.getDiffuseColor());
        materialState.setSpecular(appearanceProperties.getSpecularColor());
        materialState.setEmissive(appearanceProperties.getEmissiveColor());
        materialState.getDiffuse().a = appearanceProperties.getOpacity();

        this.setRenderState(materialState);

        this.updateRenderState();
    }

    /**
     * Apply the given texture to this node.
     * @param attributes The VisualAttributes obejct containing the texture
     */
    protected void applyTexture(VisualAttributes attributes) {
        Texture t = getTexture(attributes);
        if (t != null) {
            TextureState ts = (TextureState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.Texture);
            t.setWrap(Texture.WrapMode.Repeat);
            ts.setTexture(t);
            ts.setEnabled(true);
            this.setRenderState(ts);
        }
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
            if (attributes.hasTexture()) {
                if (keyMap.containsKey(attributes.getID())) {
                    toReturn = TextureManager.loadTexture(keyMap.get(attributes.getID()));
                }
                if (toReturn == null) {
                    Image textureImage = attributes.getTexture();
                    toReturn = TextureManager.loadTexture(textureImage, Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear, false);
                    keyMap.put(attributes.getID(), toReturn.getTextureKey());
                }
            }
            return toReturn;
        }
    }
}
