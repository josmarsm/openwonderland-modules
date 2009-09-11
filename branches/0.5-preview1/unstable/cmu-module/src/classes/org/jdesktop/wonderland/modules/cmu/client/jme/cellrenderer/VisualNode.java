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
import com.jme.bounding.BoundingVolume;
import com.jme.image.Texture;
import com.jme.renderer.Renderer;
import com.jme.scene.Geometry;
import com.jme.scene.Spatial;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureKey;
import com.jme.util.TextureManager;
import java.awt.Image;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.cmu.client.CMUCell;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.VisualType;
import org.jdesktop.wonderland.modules.cmu.common.jme.TexturedGeometry;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.AppearancePropertyMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.GeometryUpdateMessage;
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
    private boolean partOfWorld = false;
    private final Object partOfWorldLock = new Object();
    private BoundingVolume bound = null;
    private final Collection<Geometry> changingGeometries = new Vector<Geometry>();
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
        for (Geometry geometry : attributes.getGeometries()) {
            addGeometry(geometry, true);
        }

        // Apply texture
        this.applyTexture(attributes);
    }

    /**
     * Add a geometry to this node, and update bounds appropriately.
     * @param geometry The geometry to add
     * @param persistent Whether the geometry is persistent (i.e. false
     * if it is allowed to change via a GeometryUpdateMessage)
     */
    protected void addGeometry(Geometry geometry, boolean persistent) {

        if (geometry instanceof TexturedGeometry) {
            Image textureImage = ((TexturedGeometry) geometry).getTexture();
            Texture texture = TextureManager.loadTexture(textureImage, Texture.MinificationFilter.BilinearNoMipMaps, Texture.MagnificationFilter.Bilinear, false);
            TextureState ts = (TextureState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.Texture);
            ts.setTexture(texture);
            ts.setEnabled(true);
            geometry.setRenderState(ts);
            geometry.updateRenderState();
        }
        this.attachChild(geometry);

        updateBound();
        if (!persistent) {
            synchronized (changingGeometries) {
                changingGeometries.add(geometry);
            }
        }
    }

    /**
     * Remove a geometry from this node, freeing any textures associated
     * with the geometry, and updating bounds appropriately.
     * @param geometry The geometry to remove; must be a child of this node
     */
    protected void removeGeometry(Geometry geometry) {
        assert geometry.getParent() == this;

        geometry.removeFromParent();

        TextureState textureState = (TextureState) geometry.getRenderState(StateType.Texture);
        if (textureState != null) {
            Texture texture = textureState.getTexture();
            TextureManager.releaseTexture(texture);
        }

        updateBound();
    }

    /**
     * Update the saved bounding box for this node based on all geometries which
     * are attached to it; make this the active bounding box if this node
     * is currently part of the world.
     */
    protected void updateBound() {
        bound = new BoundingBox();
        for (Spatial child : getChildren()) {
            if (child instanceof Geometry) {
                BoundingVolume currentBound = new BoundingBox();
                currentBound.computeFromPoints(((Geometry) child).getVertexBuffer());
                bound.mergeLocal(currentBound);
            }
        }
        if (isPartOfWorld()) {
            this.setModelBound(bound);
        }
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
            } else if (message instanceof GeometryUpdateMessage) {
                this.applyGeometryUpdate((GeometryUpdateMessage) message);
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
    public void removeDescendant(VisualDeletedMessage visualDeletedMessage) {
        super.removeDescendant(visualDeletedMessage);
        if (visualDeletedMessage.getNodeID().equals(this.getNodeID())) {
            this.removeFromParent();
        }
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
        synchronized (partOfWorldLock) {
            this.partOfWorld = partOfWorld;
        }
        setVisible(partOfWorld);
        setModelBound(partOfWorld ? bound : null);
    }

    /**
     * Find out whether this node is visible and bounded in the world.
     * @return Whether this node is part of the world
     */
    public boolean isPartOfWorld() {
        synchronized (partOfWorldLock) {
            return partOfWorld;
        }
    }

    /**
     * Apply the given transformation to this node.
     * @param transformation The transformation to be applied
     */
    protected void applyTransformation(TransformationMessage transformation) {
        setLocalTranslation(transformation.getTranslation());
        setLocalRotation(transformation.getRotation());
    }

    /**
     * Apply the given visual properties from the CMU scene to this node.
     * @param properties The properties to be applied
     */
    protected void applyVisualProperties(VisualPropertyMessage properties) {
        setLocalScale(properties.getScale());
        setVisibleInCMU(properties.isVisible());
    }

    /**
     * Apply the given appearance properties from the CMU scene to this node.
     * @param appearanceProperties The properties to be applied
     */
    protected void applyAppearanceProperties(AppearancePropertyMessage appearanceProperties) {

        MaterialState materialState = (MaterialState) this.getRenderState(StateType.Material);
        materialState.setAmbient(appearanceProperties.getAmbientColor());
        materialState.setDiffuse(appearanceProperties.getDiffuseColor());
        materialState.setSpecular(appearanceProperties.getSpecularColor());
        materialState.setEmissive(appearanceProperties.getEmissiveColor());

        materialState.setShininess(appearanceProperties.getSpecularExponent());

        materialState.getDiffuse().a = appearanceProperties.getOpacity();

        this.setRenderState(materialState);

        this.updateRenderState();
    }

    /**
     * Reload the modifiable geometries for this node, based on the update
     * message provided.
     * @param geometryUpdate The message from which to take geometries
     */
    protected void applyGeometryUpdate(GeometryUpdateMessage geometryUpdate) {
        synchronized (changingGeometries) {
            for (Geometry geometry : changingGeometries) {
                removeGeometry(geometry);
            }
            changingGeometries.clear();
            for (Geometry geometry : geometryUpdate.getGeometries()) {
                addGeometry(geometry, false);
            }
        }
    }

    /**
     * Extract a texture from the given attributes, and apply it to this node;
     * take advantage of built-in texture caching.
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
            this.updateRenderState();
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
