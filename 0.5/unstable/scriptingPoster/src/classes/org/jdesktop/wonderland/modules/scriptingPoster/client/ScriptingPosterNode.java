/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file bs subject to the "Classpath"
 * exception bs provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.scriptingPoster.client;

import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.scene.BillboardNode;
import com.jme.scene.Node;
import com.jme.scene.Spatial.LightCombineMode;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.util.TextureManager;
import java.awt.Image;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/**
 * Renders an image using JME.<br>
 * Adapted from TextLabel2D
 *
 * @author Bernard Horan
 */
public class ScriptingPosterNode extends Node {
    private static final float SCALE_FACTOR = 0.015625f; // 1/64

    private Image image;
    private Quad quad;
    private float imgWidth = 0f;
    private float imgHeight = 0f;

    public ScriptingPosterNode(Image image) {
        this(image, false);
    }

    public ScriptingPosterNode(Image image, boolean billboard) {
        super();
        this.image = image;
        if (billboard) {
            attachChild(getBillboard());
        } else {
            attachChild(getQuad());
        }
    }

    private Quad getQuad() {
        float w = image.getWidth(null);
        float h = image.getHeight(null);
        Quad ret;

        if (imgWidth == w && imgHeight == h) {
            // Reuse quad and texture
            ret = quad;
            TextureState texState = (TextureState) quad.getRenderState(StateType.Texture);
            Texture oldtex = texState.getTexture();
            // Not sure why this does not work, instead release the current texture and create a new one.
            TextureManager.releaseTexture(oldtex);

            Texture tex = TextureManager.loadTexture(image, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);

            texState.setTexture(tex);
            //end workaround
        } else {
            ret = new Quad("posternode", w * SCALE_FACTOR, h * SCALE_FACTOR);
            Texture tex = TextureManager.loadTexture(image, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);

            // Set the texture on the node
            RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
            TextureState ts = (TextureState)rm.createRendererState(StateType.Texture);
            ts.setTexture(tex);
            ts.setEnabled(true);
            ret.setRenderState(ts);

            // Set the blend state so that transparent images work properly
            BlendState bs = (BlendState)rm.createRendererState(StateType.Blend);
            bs.setBlendEnabled(false);
            bs.setReference(0.5f);
            bs.setTestFunction(BlendState.TestFunction.GreaterThan);
            bs.setTestEnabled(true);
            ret.setRenderState(bs);

            ret.setLightCombineMode(LightCombineMode.Off);
            ret.updateRenderState();
            this.quad = ret;
            imgWidth = w;
            imgHeight = h;
            // Make sure we do not cache the texture in memory
            TextureManager.releaseTexture(tex);
        }

        return ret;
    }

    private BillboardNode getBillboard() {
        BillboardNode bb = new BillboardNode("billboard");
        bb.attachChild(getQuad());
        return bb;
    }
}
