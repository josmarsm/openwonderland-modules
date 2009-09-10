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
 * jME geometry to represent text in a CMU scene.  Most code taken
 * from TextLabel2D.
 * @author kevin
 */
public class CMUText extends Quad implements TexturedGeometry {

    // Defaults
    private static final Color DEFAULT_FOREGROUND = new Color(1f, 1f, 1f);
    private static final Font DEFAULT_FONT = Font.decode("Sans PLAIN 40");

    // Text information
    private String text = null;
    private Font font;

    // Other rendering information
    private Color foreground = null;
    private transient FontRenderContext fontRenderContext = null;

    public CMUText(String text) {
        this(text, DEFAULT_FONT);
    }

    public CMUText(String text, Font font) {
        this(text, font, DEFAULT_FOREGROUND);
    }

    public CMUText(String text, Font font, Color foreground) {
        super(text);
        setLocalRotation(new Matrix3f(-1, 0, 0, 0, -1, 0, 0, 0, 1));
        setFont(font);
        setText(text, foreground);
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

        fontRenderContext = g2d.getFontRenderContext();
    }

    public void setText(String text, Color foreground) {
        this.text = text;
        this.foreground = foreground;

        BufferedImage img = getTexture(new Vector2f());

        float newHeight = getActualHeight();

        float w = img.getWidth();
        float h = img.getHeight();
        float factor = newHeight / h;

        float newWidth = w * factor;
//
//        System.out.println("Image: " + w + " x " + h);
//        System.out.println("Actual: " + getActualWidth() + " x " + getActualHeight());
//        System.out.println("Resizing to: " + newWidth + " x " + newHeight);

        this.resize(newWidth, newHeight);
    }

    protected int getActualWidth() {
        // calculate the size of the label text rendered with the specified font
        TextLayout layout = new TextLayout(text, font, fontRenderContext);
        Rectangle2D b = layout.getBounds();

        // calculate the width of the label
        return (int) (b.getWidth());
    }

    protected int getActualHeight() {
        // calculate the size of the label text rendered with the specified font
        TextLayout layout = new TextLayout(text, font, fontRenderContext);

        // calculate the maximum height of the text including the ascents and
        // descents of the characters
        return (int) (layout.getAscent() + layout.getDescent() + 1f);
    }

    /**
     * Generate an image of the label
     *
     * @param scaleFactors is set to the factors needed to adjust texture coords
     * to the next power-of-two-sized resulting image
     */
    @Override
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
        g2d.setFont(font);


        // center the text on the label
        int scaledWidth = (int) (actualWidth * scaleFactors.x);
        int textX = desiredWidth / 2 - scaledWidth / 2;
        int textY = desiredHeight / 2;

        // draw the text
        g2d = (Graphics2D) ret.getGraphics();
        g2d.setFont(font);
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(foreground);
        g2d.drawString(text, textX, textY);


        return ret;
    }
}
