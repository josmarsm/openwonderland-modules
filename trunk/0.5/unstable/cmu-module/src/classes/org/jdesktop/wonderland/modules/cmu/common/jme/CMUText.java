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
package org.jdesktop.wonderland.modules.cmu.common.jme;

import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Vector2f;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * jME geometry to represent text in a CMU scene.  Most code taken
 * from TextLabel2D.
 * @author kevin
 */
public class CMUText extends Quad implements TexturedGeometry {

    // Defaults
    private static final Color DEFAULT_FOREGROUND = new Color(1f, 1f, 1f);
    private static final Color DEFAULT_BACKGROUND = new Color(0f, 0f, 0f);
    private static final float DEFAULT_HEIGHT = 0.3f;
    private static final Font DEFAULT_FONT = Font.decode("Sans PLAIN 40");

    // Text information
    private String text = null;
    private Font font;
    private Font drawFont;

    // Other rendering information
    private Color foreground = null;
    private Color background = null;
    private float blurIntensity = 0.1f;
    private int kernelSize = 5;
    private float fontResolution = 40f;
    private int shadowOffsetX = 2;
    private int shadowOffsetY = 2;
    private transient FontRenderContext fontRenderContext = null;

    public CMUText(String text) {
        this(text, DEFAULT_FOREGROUND, DEFAULT_BACKGROUND, DEFAULT_HEIGHT, DEFAULT_FONT);
    }

    public CMUText(String text, Color foreground, Color background, float height, Font font) {
        super(text, 0.0f, height);
        setFont(font);
        updateKernel();
        setText(text, foreground, background);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // Restore the font render context
        setFont(font);
    }

    public void setFont(Font font) {
        this.font = font;
        BufferedImage tmp0 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
        drawFont = font.deriveFont(fontResolution);

        fontRenderContext = g2d.getFontRenderContext();
    }

    public void setText(String text, Color foreground, Color background) {
        this.text = text;
        this.foreground = foreground;
        this.background = background;

        TextureState texState = (TextureState) this.getRenderState(StateType.Texture);
        if (texState != null) {
            Texture oldTex = texState.getTexture();
            TextureManager.releaseTexture(oldTex);
        }

        Vector2f scales = new Vector2f();
        BufferedImage img = getTexture(scales);

        this.clearRenderState(StateType.Texture);
        this.clearRenderState(StateType.Blend);

        float w = img.getWidth();
        float h = img.getHeight();
        float factor = this.height / h;

        this.resize(w * factor, this.height);
/*
        TextureState ts = (TextureState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Texture);
        Texture tex = TextureManager.loadTexture(img, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);

        ts.setTexture(tex);
        ts.setEnabled(true);
        this.setRenderState(ts);

        this.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);

        
        BlendState as = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        as.setBlendEnabled(true);
        as.setTestEnabled(true);
        as.setTestFunction(TestFunction.GreaterThan);
        as.setEnabled(true);
        this.setRenderState(as);

        this.setLightCombineMode(LightCombineMode.Off);
*/
         this.updateRenderState();
    }

    public void setShadowOffsetX(int offsetPixelX) {
        shadowOffsetX = offsetPixelX;
    }

    public void setShadowOffsetY(int offsetPixelY) {
        shadowOffsetY = offsetPixelY;
    }

    public void setBlurSize(int kernelSize) {
        this.kernelSize = kernelSize;
        updateKernel();
    }

    public void setBlurStrength(float strength) {
        this.blurIntensity = strength;
        updateKernel();
    }

    public void setFontResolution(float fontResolution) {
        this.fontResolution = fontResolution;
    }

    private void updateKernel() {
        //float[] kernel = new float[kernelSize * kernelSize];
        //Arrays.fill(kernel, blurIntensity);
        //blur = new ConvolveOp(new Kernel(kernelSize, kernelSize, kernel));
    }

    @Override
    public BufferedImage getTexture() {
        return getTexture(new Vector2f());
    }

    /**
     * Generate an image of the label
     *
     * @param scaleFactors is set to the factors needed to adjust texture coords
     * to the next power-of-two-sized resulting image
     */
    private BufferedImage getTexture(Vector2f scaleFactors) {

        // calculate the size of the label text rendered with the specified font
        TextLayout layout = new TextLayout(text, font, fontRenderContext);
        Rectangle2D b = layout.getBounds();

        // calculate the width of the label with shadow and blur
        int actualWidth = (int) (b.getWidth() + kernelSize + 1 + shadowOffsetX);

        // calculate the maximum height of the text including the ascents and
        // descents of the characters
        int actualHeight = (int) (layout.getAscent() + layout.getDescent() + kernelSize + 1 + shadowOffsetY);

        // determine the closest power of two bounding box
        //
        // NOTE: we scale the text height to fit the nearest power or two, and
        // then scale the text width equally to maintain the correct aspect
        // ratio:
        int desiredHeight = FastMath.nearestPowerOfTwo(actualHeight);
        int desiredWidth = FastMath.nearestPowerOfTwo((int) (((float) desiredHeight / (float) actualHeight) * actualWidth));

        // set the scale factors for scaling the text to fit the nearest power
        // of two bounding box:
        if (scaleFactors != null) {
            // scale the text vertically to fit the height
            scaleFactors.y = (float) desiredHeight / actualHeight;
            // scale the text an equal amount horizontally to maintain aspect ratio
            scaleFactors.x = scaleFactors.y;
        }

        // create an image to render the text onto
        BufferedImage tmp0 = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
        g2d.setFont(drawFont);

//        // draw debugging text alignment lines
//        g2d.setColor(Color.YELLOW);
//        g2d.drawLine(0, desiredHeight / 2, desiredWidth, desiredHeight / 2);
//        g2d.drawLine(desiredWidth / 2, 0, desiredWidth / 2, desiredHeight);

        // center the text on the label
        int scaledWidth = (int) (actualWidth * scaleFactors.x);
        int textX = desiredWidth / 2 - scaledWidth / 2;// + kernelSize / 2;
        int textY = desiredHeight / 2;

//        // draw debugging text left and right bounds lines
//        g2d.setColor(Color.RED);
//        g2d.drawLine(textX, 0, textX, desiredHeight);
//        g2d.drawLine(desiredWidth / 2 + scaledWidth / 2, 0, desiredWidth / 2 + scaledWidth / 2, desiredHeight);

        // draw the shadow of the text
        g2d.setFont(drawFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(background);
        g2d.drawString(text, textX + shadowOffsetX, textY + shadowOffsetY);

        // blur the text
        //BufferedImage ret = blur.filter(tmp0, null);
        BufferedImage ret = tmp0;

        // draw the blurred text over the shadow
        g2d = (Graphics2D) ret.getGraphics();
        g2d.setFont(drawFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(foreground);
        g2d.drawString(text, textX, textY);

        return ret;
    }
}
