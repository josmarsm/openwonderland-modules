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

import com.jme.renderer.ColorRGBA;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;

/**
 * Message containing properties related to a model's appearance - colors,
 * opacity, etc.  Texture is omitted from the message due to performance
 * considerations (textures are instead stored in VisualAttributes objects,
 * which are posted directly to the content repository).
 * @author kevin
 */
public class AppearancePropertyMessage extends NodeUpdateMessage {

    private float opacity = 0.0f;
    private ColorRGBA ambientColor = null;
    private ColorRGBA diffuseColor = null;
    private ColorRGBA specularColor = null;
    private ColorRGBA emissiveColor = null;

    /**
     * Standard constructor.
     * @param nodeID ID for the model to which this message applies
     */
    public AppearancePropertyMessage(NodeID nodeID) {
        super(nodeID);
    }

    /**
     * Copy constructor.
     * @param toCopy The message to copy
     */
    public AppearancePropertyMessage(AppearancePropertyMessage toCopy) {
        super(toCopy);
        setOpacity(toCopy.getOpacity());
        setAmbientColor(toCopy.getAmbientColor());
        setDiffuseColor(toCopy.getDiffuseColor());
        setEmissiveColor(toCopy.getEmissiveColor());
        setSpecularColor(toCopy.getSpecularColor());
    }

    /**
     * Get the opacity for the model.
     * @return Opacity for the model
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Set the opacity for the model.
     * @param opacity Opacity for the model
     */
    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    /**
     * Get the ambient color for the model.
     * @return Ambient color for the model
     */
    public ColorRGBA getAmbientColor() {
        return ambientColor;
    }

    /**
     * Set the ambient color for the model.
     * @param ambientColor Ambient color for the model
     */
    public void setAmbientColor(ColorRGBA ambientColor) {
        this.ambientColor = ambientColor;
    }

    /**
     * Get the diffuse color for the model.
     * @return Diffuse color for the model
     */
    public ColorRGBA getDiffuseColor() {
        return diffuseColor;
    }

    /**
     * Set the diffuse color for the model.
     * @param diffuseColor Diffuse color for the model
     */
    public void setDiffuseColor(ColorRGBA diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    /**
     * Get the emissive color for the model.
     * @return Emissive color for the model
     */
    public ColorRGBA getEmissiveColor() {
        return emissiveColor;
    }

    /**
     * Set the emissive color for the model.
     * @param emissiveColor Emissive color for the model
     */
    public void setEmissiveColor(ColorRGBA emissiveColor) {
        this.emissiveColor = emissiveColor;
    }

    /**
     * Get the specular color for the model.
     * @return Specular color for the model
     */
    public ColorRGBA getSpecularColor() {
        return specularColor;
    }

    /**
     * Set the specular color for the model,
     * @param specularColor Specular color for the model
     */
    public void setSpecularColor(ColorRGBA specularColor) {
        this.specularColor = specularColor;
    }
}
