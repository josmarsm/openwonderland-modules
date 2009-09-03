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
 *
 * @author kevin
 */
public class AppearancePropertyMessage extends NodeUpdateMessage {

    private float opacity = 0.0f;
    private ColorRGBA ambientColor = null;
    private ColorRGBA diffuseColor = null;
    private ColorRGBA specularColor = null;
    private ColorRGBA emissiveColor = null;

    public AppearancePropertyMessage(NodeID nodeID) {
        super(nodeID);
    }

    public AppearancePropertyMessage(AppearancePropertyMessage toCopy) {
        super(toCopy);
        setOpacity(toCopy.getOpacity());
        setAmbientColor(toCopy.getAmbientColor());
        setDiffuseColor(toCopy.getDiffuseColor());
        setEmissiveColor(toCopy.getEmissiveColor());
        setSpecularColor(toCopy.getSpecularColor());
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public ColorRGBA getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientColor(ColorRGBA ambientColor) {
        this.ambientColor = ambientColor;
    }

    public ColorRGBA getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(ColorRGBA diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    public ColorRGBA getEmissiveColor() {
        return emissiveColor;
    }

    public void setEmissiveColor(ColorRGBA emissiveColor) {
        this.emissiveColor = emissiveColor;
    }

    public ColorRGBA getSpecularColor() {
        return specularColor;
    }

    public void setSpecularColor(ColorRGBA specularColor) {
        this.specularColor = specularColor;
    }
}
