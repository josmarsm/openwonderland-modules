/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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

package org.jdesktop.wonderland.modules.programmingdemo.client.jme.cellrenderer;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState.StateType;
import java.awt.Color;
import java.util.logging.Logger;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.programmingdemo.client.jme.cellrenderer.SortCellRenderer.SortHoverListener;

/**
 * An object to sort
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class SortSphere extends Node {
    private static final Logger logger =
            Logger.getLogger(SortSphere.class.getName());

    private boolean highlighted;
    private Box highlight;
    private SortHoverListener hoverListener;
    private float colorPercentage;

    public SortSphere(String text, Color color, float colorPercentage) {
        super (text);

        this.colorPercentage = colorPercentage;

        setupMesh(text);
        setupColor(color, colorPercentage);
        setupHighlight();
    }

    /**
     * Set whether or not this object is highlighted. This is assumed to be
     * called from the render commit thread.
     *
     * @param highlighted true to highlight this object, false to turn off
     * highlighting.
     */
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;

        if (highlighted) {
            attachChild(highlight);
        } else {
            detachChild(highlight);
        }
        
        ClientContextJME.getWorldManager().addToUpdateList(highlight);
        ClientContextJME.getWorldManager().addToUpdateList(this);
    }

    /**
     * Determine if this object should be highlighted
     * @return whether or not the object is highlighted
     */
    public boolean isHightlighted() {
        return highlighted;
    }

    /**
     * Set the color of this sphere
     * @param color the color to show
     */
    public void setColor(Color color) {
        ColorRGBA colorRGBA = convert(color, colorPercentage);

        // set the sphere color
        MaterialState ms = (MaterialState) getRenderState(StateType.Material);
        ms.setAmbient(colorRGBA);
        ms.setDiffuse(colorRGBA);
    }

    /**
     * Setup the color
     * @param color the base color to set
     * @param colorPercent the percentage of that color to use
     */
    public void setupColor(Color color, float colorPercent) {
        ColorRGBA colorRGBA = convert(color, colorPercent);

        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        MaterialState ms = (MaterialState) rm.createRendererState(StateType.Material);
        ms.setAmbient(colorRGBA);
        ms.setDiffuse(colorRGBA);
        ms.setSpecular(ColorRGBA.white);
        ms.setShininess(100.0f);
        ms.setEmissive(ColorRGBA.black);
        ms.setEnabled(true);
        setRenderState(ms);
    }

    /**
     * Setup a sphere mesh
     */
    private void setupMesh(String text) {
        Sphere sphere = new Sphere(text, Vector3f.ZERO, 25, 25, 0.5f);
        attachChild(sphere);
        
        setModelBound(new BoundingSphere(0.5f, Vector3f.ZERO));
        updateModelBound();
    }

    /**
     * Setup the wireframe state for highlighting.
     */
    private void setupHighlight() {
        highlight = new Box("highlight", Vector3f.ZERO,
                            0.5f, 0.5f, 0.5f);
    }

    /**
     * Set the hover listener associated with this spehere
     * @param listener the hover listener
     */
    void setHoverListener(SortHoverListener listener) {
        this.hoverListener = listener;
    }

    /**
     * Get the hover listener associated with this sphere
     * @return the hover listener
     */
    SortHoverListener getHoverListener() {
        return hoverListener;
    }

    /**
     * Convert a Color into a ColorRGBA
     * @param color the color to convert
     * @param colorPercentage the relative amount of the color to show
     * @return a ColorRGBA associated with
     */
    private static ColorRGBA convert(Color color, float colorPercentage) {
        float r = (color.getRed() / 255f) * colorPercentage;
        float g = (color.getGreen() / 255f) * colorPercentage;
        float b = (color.getBlue() / 255f) * colorPercentage;
        float a = (color.getAlpha() / 255f);

        return new ColorRGBA(r, g, b, a);
    }
}
