/*
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

package org.jdesktop.wonderland.modules.marbleous.client.ui;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.BillboardNode;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.BlendState.TestFunction;

import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import java.awt.image.BufferedImage;

import org.jdesktop.wonderland.modules.marbleous.client.ui.SampleDisplayEntity.DisplayMode;
import java.util.logging.Logger;
import com.jme.util.TextureManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.marbleous.common.trace.SampleInfo;


/**
 * A billboarding node that attaches graphics representing an a sample info. Will
 * generate different graphics based on display mode.
 *
 * Heavily modified from AnnotationNode and TextLabel2D 
 * @author mabonner, deronj
 */
public class SampleDisplayNode extends BillboardNode {

    private static Logger logger = Logger.getLogger(SampleDisplayNode.class.getName());

    private static final float SAMPLE_ENTITY_Y_OFFSET = 0.6f;

    // TODO: shouldn't hard code this here
    private static final float MARBLE_RADIUS = 0.25f;

    // Default Colors
    public static Color DEFAULT_BACKGROUND_COLOR = new Color(0.5f, 0.5f, 0.5f);
    public static Color DEFAULT_FONT_COLOR = Color.BLACK;
    public static Color DEFAULT_SHADOW_COLOR = Color.WHITE;
    // Default alpha
    public static int DEFAULT_ALPHA = 200;
  
    private Color bgColor = DEFAULT_BACKGROUND_COLOR;
    private Color fontColor = DEFAULT_FONT_COLOR;
    private Color shadowColor = DEFAULT_SHADOW_COLOR;
    
    // TODO: not used
    private Color borderColorTransient = Color.BLACK;
    private ColorRGBA borderColorRgbaTransient = new ColorRGBA(0f, 0f, 0f, 1f);

    private Color borderColorCurrentAndPinned = new Color(0, 0.75f, 0.75f);
    private Color borderColorCurrent = new Color(0, 0.75f, 0f);
    private Color borderColorPinned  = new Color(0, 0, 0.75f);

    private ColorRGBA borderColorRgbaCurrentAndPinned = new ColorRGBA(0, 0.75f, 0.75f, 1f);
    private ColorRGBA borderColorRgbaCurrent = new ColorRGBA(0, 0.75f, 0f, 1f);
    private ColorRGBA borderColorRgbaPinned  = new ColorRGBA(0, 0, 0.75f, 1f);

    /** base font */
    private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    /** controls the graphics generation of this SampleDisplayNode */
    DisplayMode mode;

    /** controls size of font */
    private float fontSizeModifier;

    // graphical settings used only by the node
    private float blurIntensity = 0.1f;
    private int kernelSize = 5;
    private ConvolveOp blur;

    private final int SHADOW_OFFSET_X = 2;
    private final int SHADOW_OFFSET_Y = 2;

    // padding between text and edges
    private final int PADDING_LEFT = 30;
    private final int PADDING_RIGHT = 30;
    private final int PADDING_TOP = /*5*/ 20;
    private final int PADDING_BOTTOM = /*5*/20;
    /** padding between Author and Title */
    private final int PADDING_LINE = 5;

    /** width of border */
    private final int BORDER_WIDTH = 6;

    private final int MIN_WIDTH = 475;

    private SampleInfo sampleInfo;
    private boolean current;
    private boolean pinned;

    // strings pulled from annotation this node represents
    String time;
    String position;
    String text;

    private Quad quad;
    private Cylinder descender;

    private float height3D;

    public SampleDisplayNode(SampleInfo sampleInfo, DisplayMode displayMode, float fontMod,
                             boolean current, boolean pinned){
        super("Sample info display node for time " + sampleInfo.getTime());
        this.sampleInfo = sampleInfo;
        mode = displayMode;
        fontSizeModifier = fontMod;
        this.current = current;
        this.pinned = pinned;

        // set pieces
        time = "Time (t): " + format(sampleInfo.getTime());
        position = getPosition();
        text = getText();
        updateKernel();
        // done if the node is hidden
        if(displayMode == DisplayMode.HIDDEN){
            logger.info("[sample node] hidden, not filling with anything");
            return;
        }

        update();
    }
  
    public void setCurrent (boolean current) {
        if (this.current == current) return;
        this.current = current;
        update();
    }

    public void setPinned (boolean pinned) {
        if (this.pinned == pinned) return;
        this.pinned = pinned;
        update();
    }

    public synchronized void setDescenderColor(final ColorRGBA color) {
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                if (descender != null) {
                    MaterialState ms = (MaterialState) descender.getRenderState(RenderState.StateType.Material);
                    if (ms == null) {
                        ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
                        descender.setRenderState(ms);
                    }
                    ms.setAmbient(new ColorRGBA(color));
                    ms.setDiffuse(new ColorRGBA(color));
                    ClientContextJME.getWorldManager().addToUpdateList(descender);
                }
            }
        }, null); 
    }

    private void update () {
        // build child
        if (quad != null) {
            detachChild(quad);
        }
        quad = getQuad();
        attachChild(quad);


        float descenderHeight = SAMPLE_ENTITY_Y_OFFSET + MARBLE_RADIUS;
        descender = new Cylinder("Descender cyl", 10, 10, 0.02f, descenderHeight, true);
        Quaternion quat = new Quaternion();
        quat.fromAngleAxis((float)Math.toRadians(90f), new Vector3f(1f, 0, 0 ));
        descender.setLocalRotation(quat);
        descender.setLocalTranslation(new Vector3f(0f, -height3D/2f - descenderHeight/2f, 0f));

        if (current && pinned) {
            setDescenderColor(borderColorRgbaCurrentAndPinned);
        } else if (current) {
            setDescenderColor(borderColorRgbaCurrent);
        } else if (pinned) {
            setDescenderColor(borderColorRgbaPinned);
        } else {
            setDescenderColor(borderColorRgbaTransient);
        }

        attachChild(descender);

        // set bounds to make pickable
        setModelBound(new BoundingBox());
        updateModelBound();
    }

    private static DecimalFormat decimalFormat = new DecimalFormat("#.###");

    private String format (float f) {
        return decimalFormat.format(f);
    }

    private String getPosition () {
        Vector3f p = sampleInfo.getPosition();
        return "Position (X): [" + format(p.x) + "," + format(p.y) + "," + format(p.z) + "]";
    }

    private String getText () {
        Vector3f p = sampleInfo.getPosition();
        Vector3f v = sampleInfo.getVelocity();
        Vector3f a = sampleInfo.getAcceleration();
        Vector3f f = sampleInfo.getForce();
        Vector3f m = sampleInfo.getMomentum();
        float pe = sampleInfo.getPotentialEnergy();
        float ke = sampleInfo.getKineticEnergy();
        float te = pe + ke;
        float mass = sampleInfo.getMass();
        float g = sampleInfo.getGravity();

        String ret = "Velocity (V):  [" + format(v.x) + "," + format(v.y) + "," + format(v.z) + "]\n" +
                     "Acceleration (a):  [" + format(a.x) + "," + format(a.y) + "," + format(a.z) + "]\n";

        if (mode == DisplayMode.VERBOSE) {

            ret += "Net Force (F): ma = " + mass + " x [" + format(a.x) + "," + format(a.y) + "," + format(a.z) + 
                "] = [" + format(f.x) + "," + format(f.y) + "," + format(f.z) + "]\n" +

                "Momentum (P):  mV = " + format(mass) + " x [" + format(v.x) + "," + format(v.y) + "," + format(v.z) + 
                "] = [" + format(m.x) + "," + format(m.y) + "," + format(m.z) + "]\n" +

                "Potential Energy (U): mGy = (" + format(mass) + ") x (" + format(g) + ") x (" + format(p.y) + ") = " +  
                format(pe) + "\n" +

                "Kinetic Energy (K): (1/2)mV^2 = (" + format(0.5f * mass) + ") x (" + format(v.lengthSquared()) + 
                ") = " + format(ke) + "\n" +

                "Total Energy (E):  pe + ke = " + format(pe) + " + " + format(ke) + " = " + format(te);
        } else {
            ret += "Net Force (F):  [" + format(f.x) + "," + format(f.y) + "," + format(f.z) + "]\n" +
                "Momentum (P):  [" + format(m.x) + "," + format(m.y) + "," + format(m.z) + "]\n" +
                "Potential Energy (U): [" + format(pe) + "]\n" +
                "Kinetic Energy (K): [" + format(ke) + "]\n" +
                "Total Energy (E):  [" + format(te) + "]";
        }

        return ret;
    }


    /**
     * Generate an image of the label
     */
    private BufferedImage getImage() {
        // calculate the size of the label text rendered with the specified font
        FontRenderContext frc = getFontRenderContext();
    
        TextLayout timeLayout = new TextLayout(time, font, frc);
        Rectangle2D timeRect = timeLayout.getBounds();

        // and for position line
        if(position == null || position.length() == 0){
            position = " ";
        }
        TextLayout positionLayout = new TextLayout(position, font, frc);
        Rectangle2D positionRect = positionLayout.getBounds();

        // calculate the width of the label with shadow and blur
        // width depends on which is longer, position or time name
        int totalWidth = getImageWidth(timeRect, positionRect);
        // prepare and split up text if displaying in large mode
        ArrayList<TextLayout> chunks = null;
        if(totalWidth * fontSizeModifier < MIN_WIDTH * fontSizeModifier){
            totalWidth = MIN_WIDTH;
        }
        // split into lines
        String [] lines = text.split("\n");
        // make each line fit into desired width
        int singleLineWidth = totalWidth - PADDING_LEFT - PADDING_RIGHT;
        chunks = new ArrayList<TextLayout>();
        for(String s:lines){
            splitText(chunks, singleLineWidth, s);
        }

        // now we can do the heights
        // calculate the maximum height of the text including the ascents and
        // descents of the characters, both lines, padding between lines
        int totalHeight = getImageHeight(timeLayout, positionLayout, chunks);

        int actualTimeHeight = 0;
        int actualTextLineHeight = 0;
        int actualPositionHeight = 0;
        // small - get height of time
        // medium - get heights of time and position
        if (mode == DisplayMode.BASIC){
            actualTimeHeight= (int)(timeLayout.getAscent() + timeLayout.getDescent());
            actualPositionHeight = (int)(positionLayout.getAscent() + positionLayout.getDescent());
        }
        // non-basic - get heights of time, position and text
        else {
            actualTimeHeight= (int)(timeLayout.getAscent() + timeLayout.getDescent());
            actualPositionHeight = (int)(positionLayout.getAscent() + positionLayout.getDescent());
            TextLayout aLine = chunks.get(0);
            actualTextLineHeight = (int)(aLine.getAscent() + aLine.getDescent());
        }

        //logger.info("[sample node] actual height/width:" + totalHeight + "/" + totalWidth);
        //logger.info("[sample node] desired height/width:" + totalHeight + "/" + totalWidth);
        //logger.info("[sample node] time:" + time);
        //logger.info("[sample node] position:" + position);
        //logger.info("[sample node] text:" + text);

        // create an image to render the text onto
        BufferedImage tmp0 = new BufferedImage(totalWidth+BORDER_WIDTH*2, totalHeight+BORDER_WIDTH*2, BufferedImage.TYPE_INT_ARGB);
        //logger.info("[sample node] image height: " + tmp0.getHeight());
        //logger.info("[sample node] image width: " + tmp0.getWidth());
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
        g2d.setFont(font);

        // draw background
        int x = 0 + BORDER_WIDTH;
        int y = 0 + BORDER_WIDTH;

        int h = totalHeight;
        int w = totalWidth;

        int arc = 60;

        // draw background rectangle
        g2d.setColor(bgColor);
        //logger.info("[sample node] w: " + w);
        //logger.info("[sample node] w - bw2: " + (w-BORDER_WIDTH*2));
        g2d.fillRoundRect(x, y, w, h, arc, arc);

        // draw background rectangle's gradient
        Paint op = g2d.getPaint();
        Color dg = new Color(10,10,10,180);
        Color lg = new Color(100,100,100,125);
        GradientPaint p = new GradientPaint(0, (h * 0.20f), lg, 0, (h), dg);
        g2d.setPaint(p);
        //logger.info("[sample node] filling rounded rec: x y w h " + x + " " + y + " " + w+ " " +h + " ");
        g2d.fillRoundRect(x, y, w, h, arc, arc);

        // reset paint
        g2d.setPaint(op);

        // draw border
        g2d.setStroke(new BasicStroke(BORDER_WIDTH));
        if (current && pinned) {
            g2d.setColor(borderColorCurrentAndPinned);
        } else if (current) {
            g2d.setColor(borderColorCurrent);
        } else if (pinned) {
            g2d.setColor(borderColorPinned);
        } else {
            g2d.setColor(borderColorTransient);
        }

        g2d.setPaintMode();
        g2d.drawRoundRect(x, y, w, h, arc, arc);
        // The left and right edges of the rectangle are at x and xÊ+Êwidth, respectively.
        // The top and bottom edges of the rectangle are at y and yÊ+Êheight.

        // used to draw text
        int textX = 0;
        int textY = 0;
        // used to blur shadow
        BufferedImage ret = tmp0;

        // draw time text and shadow always
        //logger.info("[sample node] draw time");
        textX = 0 + PADDING_LEFT;
        textY = actualTimeHeight + PADDING_TOP;// + paddingTop + borderWidth;

        // draw the shadow of the text
        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(shadowColor);
        //System.out.println("shadow x and y: " + textX + " " + textY);
        //System.out.println("offsets: " + SHADOW_OFFSET_X + " " + SHADOW_OFFSET_Y);
        //System.out.println("desired heights, time subj: " + actualTimeHeight + " " + actualPositionHeight);
        g2d.drawString(time, textX + SHADOW_OFFSET_X, textY + SHADOW_OFFSET_Y);


        // blur the shadows
        ret = blur.filter(tmp0, null);
        // draw the text over the shadow
        g2d = (Graphics2D) ret.getGraphics();
        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(fontColor);
        //System.out.println("the TEXT x and y: " + textX + " " + textY);
        g2d.drawString(time, textX, textY);
    
        // draw position string always
        //logger.info("[sample node] draw position");

        // draw position text
        // make same left-justification, but different y
        textY += actualPositionHeight + PADDING_LINE;
      
        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(fontColor);
        g2d.drawString(position, textX, textY);

        // draw the message text if necessary
        if(mode != DisplayMode.BASIC){
            //logger.info("[sample node] draw message");
            textY += actualPositionHeight + PADDING_LINE;
      
            g2d.setFont(font);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(fontColor);
            for(TextLayout t:chunks){
                t.draw(g2d, textX, textY);
                //logger.info("[sample node] drawing string:" + t.toString());
                textY += actualTextLineHeight + PADDING_LINE;
            }
        }

        return ret;
    }







    // -------------------------------------------
    // helper functions for getImage
    // -------------------------------------------
    /**
     * Calculate the appropriate width of the image based on the current DisplayMode
     *
     * Used by getImage.
     * @return the actual width the image should have
     * @param timeRect rectangle bounding the time text
     * @param positionRect rectangle bounding the position text
     */
    private int getImageWidth(Rectangle2D timeRect, Rectangle2D positionRect){
        int actualWidth = PADDING_LEFT + PADDING_RIGHT; // 18
        if(timeRect.getWidth() > positionRect.getWidth()){
            //logger.info("an: time had larger width " + timeRect.getWidth() + " vs " + positionRect.getWidth());
            actualWidth += timeRect.getWidth();
        }
        else{
            //logger.info("an: position had equal or larger width " + positionRect.getWidth() + " " +  timeRect.getWidth());
            actualWidth += positionRect.getWidth();
        }
        return actualWidth;
    }

    /**
     * Calculate the appropriate height of the image based on the current DisplayMode
     *
     * Used by getImage.
     * @return the actual height the image should have
     * @param timeLayout TextLayout of time
     * @param positionLayout TextLayout of time
     * @param chunks contains annotation's text, broken up into lines
     */
    private int getImageHeight(TextLayout timeLayout, TextLayout positionLayout, ArrayList<TextLayout> chunks) {
        int ret = PADDING_BOTTOM + PADDING_TOP;
        // add position and text to height for medium and large versions
        //logger.info("[sample node] display mode here is: " + mode);
        ret += (int) (timeLayout.getAscent() + timeLayout.getDescent() +
                      positionLayout.getAscent() + positionLayout.getDescent() +
                      kernelSize + 1 + SHADOW_OFFSET_Y + PADDING_LINE);

        // also add lines of text from chunks to height for large versions
        if(mode != DisplayMode.BASIC){
            //logger.info("[sample node] large, adding chunks inside");
            for(TextLayout t:chunks){
                //        logger.info("chunk: " + t.getAscent() + " " + t.getDescent());
                ret += (int)(t.getAscent() + t.getDescent());
                ret += PADDING_LINE;
            }
        }
        //logger.info("[sample node] ret is finally: " + ret);
        return ret;
    }

    /**
     * Calculates width of text, splits onto multiple lines of maximum length
     * lineWidth if necessary. Stores line(s) in the chunks ArrayList.
     * @param chunks ArrayList to store line(s) in
     * @param lineWidth maximum length of each line
     * @param str string to split
     */
    private void splitText(ArrayList<TextLayout> chunks, int lineWidth, String str){
        if(str.length() == 0){
            str = " ";
        }
        FontRenderContext frc = getFontRenderContext();
        TextLayout textLayout = new TextLayout(str,
                                               font, frc);
        Rectangle2D textRect = textLayout.getBounds();
        // does text need to be split?
        if(textRect.getWidth() > lineWidth){

            AttributedString asText = new AttributedString(str);
            asText.addAttribute(TextAttribute.FONT, font);
            AttributedCharacterIterator asItr = asText.getIterator();

            int start = asItr.getBeginIndex();
            int end = asItr.getEndIndex();

            LineBreakMeasurer line = new LineBreakMeasurer(asItr, frc);
            //          LineBreakMeasurer line = new LineBreakMeasurer(asItr, frc);
            line.setPosition(start);
            // Get lines from lineMeasurer until the entire
            // paragraph has been displayed.
            while (line.getPosition() < end) {

                // Retrieve next layout.
                // width = maximum line width
                TextLayout layout = line.nextLayout(lineWidth);
                chunks.add(layout);
            }
        }
        else{
            chunks.add(textLayout);
        }
    }

    /**
     * A quad to display the image created in getImage
     * @return
     */
    private Quad getQuad() {
        final BufferedImage img = getImage();
        if(img == null){
            logger.severe("[sample node] image is null!!!");
        }

        float w = img.getWidth();
        float h = img.getHeight();
        float height = 1f;
        //    float factor = height / h;
        float factor = 0.005524862f;
    
        height3D = h * fontSizeModifier;
        //    Quad ret = new Quad("anno node", w * factor, h * factor);
        final Quad ret = new Quad("anno node", w * fontSizeModifier, height3D);
        //logger.info("[sample node] width, height of quad:" + w + " " + h + "mod size is: " + fontSizeModifier);
        //logger.info("[sample node] factored width, height of quad:" + w*factor + " " + h*factor + " factor is:" + factor);

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
                Texture tex = TextureManager.loadTexture(img, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);
                
                ts.setTexture(tex);
                ts.setEnabled(true);
                ret.setRenderState(ts);

                ret.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);

                BlendState as = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
                as.setBlendEnabled(true);
                as.setTestEnabled(true);
                as.setTestFunction(TestFunction.GreaterThan);
                as.setEnabled(true);
                ret.setRenderState(as);

                ret.setLightCombineMode(LightCombineMode.Off);
                ret.updateRenderState();

                float y = height3D/2f + SAMPLE_ENTITY_Y_OFFSET;
                setLocalTranslation(new Vector3f(0f, y, 0f));

                ClientContextJME.getWorldManager().addToUpdateList(SampleDisplayNode.this);
            }
        }, null);

        return ret;
    }

    private void updateKernel() {
        float[] kernel = new float[kernelSize * kernelSize];
        Arrays.fill(kernel, blurIntensity);
        blur = new ConvolveOp(new Kernel(kernelSize, kernelSize, kernel));
    }

    private FontRenderContext getFontRenderContext(){
        BufferedImage tmp0 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
        return g2d.getFontRenderContext();
    }
}
