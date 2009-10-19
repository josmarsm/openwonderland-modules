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

import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Vector2f;
import com.jme.scene.shape.Quad;
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
 * jME geometry to represent text in a CMU scene.  Modeled after
 * TextLabel2D; the geometry is simply an appropriately sized Quad, and
 * can be textured with an image of the text to be drawn.
 * @author kevin
 */
public class CMUText extends Quad implements TexturedGeometry {

    // Defaults
    private static final Color DEFAULT_COLOR = new Color(1f, 1f, 1f);
    private static final Font DEFAULT_FONT = Font.decode("Sans PLAIN 60");

    // User-provided information
    private String text = null;
    private Font userFont;
    private Color color = null;

    // Other rendering information
    private float fontResolution = 60f;
    private float drawHeight;
    private Font drawFont;
    private transient FontRenderContext fontRenderContext = null;

    /**
     * Constructor using default font/color.
     * @param text Text to display
     */
    public CMUText(String text) {
        this(text, DEFAULT_FONT);
    }

    /**
     * Constructor using default color.
     * @param text Text to display
     * @param font Font to use
     */
    public CMUText(String text, Font font) {
        this(text, font, DEFAULT_COLOR);
    }

    /**
     * Standard constructor.
     * @param text Text to display
     * @param font Font to use
     * @param color Font color to use
     */
    public CMUText(String text, Font font, Color color) {
        super(text);

        // Flip the geometry along the z-axis to account for differences in jME/CMU geometries
        setLocalRotation(new Matrix3f(-1, 0, 0, 0, -1, 0, 0, 0, 1));

        // This makes the text show up lighter....
        //setLightCombineMode(LightCombineMode.Off);

        // Set values
        setFont(font);
        setText(text, color);
    }

    /**
     * Additional handling for deserialization; restores the font render
     * context.
     * @param in The stream reading the object
     * @throws java.io.IOException If there is an error reading the stream
     * @throws java.lang.ClassNotFoundException If class information is not
     * attached to the stream object
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // Restore the font render context
        setFont(drawFont);
    }

    /**
     * Get the font specified by the user in drawing this text.
     * @return Font for this text
     */
    public Font getFont() {
        return userFont;
    }

    /**
     * Set the font with which to draw this text.  The actual font used
     * to draw the text will be a at a fixed resolution (determined by
     * getFontResolution()), but will be scaled to match the size of
     * the font provided here.
     * @param font The font with which to render this text
     */
    public void setFont(Font font) {
        this.drawFont = font.deriveFont(getFontResolution());
        this.userFont = font;
        BufferedImage tmp0 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
        fontRenderContext = g2d.getFontRenderContext();
        updateDrawScale();
    }

    /**
     * Get the text displayed by this geometry.
     * @return Text to be displayed
     */
    public String getText() {
        return text;
    }

    /**
     * Set the text to be displayed by this geometry.
     * @param text Text to be displayed
     * @param color Color for the text
     */
    public void setText(String text, Color color) {
        this.text = text;
        this.color = color;

        updateDrawScale();

        float newHeight = getDrawHeight();

        float w = getTextureWidth();
        float h = getTextureHeight();
        float factor = newHeight / h;

        float newWidth = w * factor;
        this.resize(newWidth, newHeight);
    }

    /**
     * Set the draw height based on the current text and the native size
     * of the current font.
     */
    protected void updateDrawScale() {
        if (getText() != null) {
            // Calculate the draw height based on the font size
            TextLayout layout = new TextLayout(getText(), getFont(), fontRenderContext);
            //Rectangle2D b = layout.getBounds();
            //setDrawWidth((float)b.getWidth());
            setDrawHeight(layout.getAscent() + layout.getDescent() + 1f);
        }
    }

    /**
     * Get the resolution used to draw text (the user-provided font will be
     * rendered at this resolution rather than its native resolution, and then
     * the geometry will be scaled to match the native resolution).
     * @return Resolution at which to render text
     */
    public float getFontResolution() {
        return fontResolution;
    }

    /**
     * Set the resolution used to draw text.
     * @param fontResolution Resolution at which to render text
     */
    public void setFontResolution(float fontResolution) {
        this.fontResolution = fontResolution;
    }

    /**
     * Get the height at which this text should be drawn, e.g. the height
     * of the text using the user-provided font.
     * @return Height to draw this text
     */
    protected float getDrawHeight() {
        return drawHeight;
    }

    /**
     * Set the height at which this text should be drawn.
     * @param drawHeight Height to draw this text
     */
    protected void setDrawHeight(float drawHeight) {
        this.drawHeight = drawHeight;
    }

    /**
     * Get the actual width of this text, with respect to the current draw
     * font.
     * @return Actual width of this text
     */
    protected int getActualWidth() {
        // Calculate the size of the label text rendered with the specified font
        TextLayout layout = new TextLayout(getText(), drawFont, fontRenderContext);
        Rectangle2D b = layout.getBounds();

        // Calculate the width of the label
        return (int) (b.getWidth());
    }

    /**
     * Get the actual height of this text, with respect to the current draw
     * font.
     * @return Actual height of the text
     */
    protected int getActualHeight() {
        // Calculate the size of the label text rendered with the specified font
        TextLayout layout = new TextLayout(getText(), drawFont, fontRenderContext);

        // Calculate the maximum height of the text including the ascents and
        // descents of the characters
        return (int) (layout.getAscent() + layout.getDescent() + 1f);
    }

    /**
     * Get the image representing the text to be drawn on this geometry.
     * @return Texture for the geometry
     */
    @Override
    public BufferedImage getTexture() {
        return getTexture(new Vector2f());
    }

    /**
     * Generate an image of the label.
     * @param scaleFactors Set to the factors needed to adjust texture coords
     * to the next power-of-two-sized resulting image
     * @return Texture for the geometry
     */
    public BufferedImage getTexture(Vector2f scaleFactors) {

        int actualWidth = getActualWidth();
        int actualHeight = getActualHeight();

        // determine the closest power of two bounding box
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
        BufferedImage ret = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) ret.getGraphics();
        g2d.setFont(drawFont);


        // center the text on the label
        int scaledWidth = (int) (actualWidth * scaleFactors.x);
        int textX = desiredWidth / 2 - scaledWidth / 2;
        int textY = desiredHeight / 2;

        // draw the text
        g2d = (Graphics2D) ret.getGraphics();
        g2d.setFont(drawFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.drawString(text, textX, textY);

        return ret;
    }

    /**
     * Get the height at which the texture for this geometry will be drawn.
     * @return Height for the texture
     */
    protected int getTextureHeight() {
        return FastMath.nearestPowerOfTwo(getActualHeight());
    }

    /**
     * Get the width at which the texture for this geometry will be drawn.
     * @return Width for the texture
     */
    protected int getTextureWidth() {
        return FastMath.nearestPowerOfTwo((int) (((float) getTextureHeight() / (float) getActualHeight()) * getActualWidth()));
    }
}
