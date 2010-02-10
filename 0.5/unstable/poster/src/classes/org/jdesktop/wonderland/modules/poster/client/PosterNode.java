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
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.poster.client;

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
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import java.awt.Image;
import java.util.logging.Logger;

/**
 * Renders an image using JME.<br>
 * Adapyed from TextLabel2D
 *
 * @author Bernard Horan
 */
public class PosterNode extends Node {

    private static final Logger posterNodeLogger = Logger.getLogger(PosterNode.class.getName());
    private Image image;
    private Quad quad;
    private int height;
    private float imgWidth = 0f;
    private float imgHeight = 0f;
    private float imgFactor = 0f;

    public PosterNode(Image image) {
        this(image, false);
    }

    public PosterNode(Image image, boolean billboard) {
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
        float factor = height / h;
        factor = 0.0078125f; // 1/128
        factor = factor * 2;
        Quad ret;

        if (imgWidth == w && imgHeight == h && imgFactor == factor) {
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
            ret = new Quad("posternode", w * factor, h * factor);
            TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            Texture tex = TextureManager.loadTexture(image, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);

            ts.setTexture(tex);
            ts.setEnabled(true);
            ret.setRenderState(ts);

            BlendState as = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
            as.setBlendEnabled(false);
            as.setReference(0.5f);
            as.setTestFunction(BlendState.TestFunction.GreaterThan);
            as.setTestEnabled(true);
            ret.setRenderState(as);

            ret.setLightCombineMode(LightCombineMode.Off);
            ret.updateRenderState();
            this.quad = ret;
            imgWidth = w;
            imgHeight = h;
            imgFactor = factor;
        }

        return ret;
    }

    private BillboardNode getBillboard() {
        BillboardNode bb = new BillboardNode("bb");
        bb.attachChild(getQuad());
        return bb;
    }
}
