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
import com.jme.renderer.Renderer;
import com.jme.scene.BillboardNode;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.BlendState.TestFunction;

import com.jme.system.DisplaySystem;
import java.awt.image.BufferedImage;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.marbleous.client.ui.SampleDisplayEntity.DisplayMode;
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
import java.util.ArrayList;
import java.util.Arrays;
import org.jdesktop.wonderland.modules.marbleous.client.SampleInfo;

/**
 * A billboarding node which displays the physics simulation trace sample info. Will
 * generate different graphics based on display mode.
 *
 * Heavily modified from TextLabel2D and AnnotationNode
 * @author mabonner, deronj
 */
public class SampleDisplayNode extends BillboardNode {

    private static Logger logger = Logger.getLogger(SampleDisplayNode.class.getName());

    private SampleInfo sampleInfo;

    /** controls the graphics generation of this SampleDisplayNode */
    DisplayMode mode;

    private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

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
    private final int PADDING_TOP = 5;
    private final int PADDING_BOTTOM = 5;
    /** padding between Author and Title */
    private final int PADDING_LINE = 5;

    /** width of border */
    private final int BORDER_WIDTH = 6;

    private final int MIN_WIDTH = 475;

    public static Color DEFAULT_BACKGROUND_COLOR = Color.DARK_GRAY;
    public static Color DEFAULT_FONT_COLOR = Color.WHITE;
    public static Color DEFAULT_SHADOW_COLOR = Color.BLACK;
    // Default alpha
    public static int DEFAULT_ALPHA = 200;
  
    private Color bgColor = DEFAULT_BACKGROUND_COLOR;
    private Color fontColor = DEFAULT_FONT_COLOR;
    private Color shadowColor = DEFAULT_SHADOW_COLOR;

    public SampleDisplayNode(SampleInfo sampleInfo, DisplayMode displayMode, float fontMod) {
        super("SampleDisplayNode for Time " + sampleInfo.getTime());
        this.sampleInfo = sampleInfo;

        mode = displayMode;
        fontSizeModifier = fontMod;

        updateKernel();

        // done if the node is hidden
        if(displayMode == DisplayMode.HIDDEN){
            logger.info(" hidden, not filling with anything");
            return;
        }

        // build child
        attachChild(getQuad());
        // set bounds to make pickable
        setModelBound(new BoundingBox());
        updateModelBound();
    }
  


    /**
     * Generate an image of the label
     */
    private BufferedImage getImage() {
        // calculate the size of the label text rendered with the specified font
        FontRenderContext frc = getFontRenderContext();
    
        String time = Float.toString(sampleInfo.getTime());
        TextLayout timeLayout = new TextLayout(time, font, frc);
        Rectangle2D timeRect = timeLayout.getBounds();

        String position = sampleInfo.getPosition().toString();
        TextLayout positionLayout = new TextLayout(position, font, frc);
        Rectangle2D positionRect = positionLayout.getBounds();

        // calculate the width of the label with shadow and blur
        // width depends on which is longer
        int totalWidth = getImageWidth(timeRect, positionRect);

        String text = sampleInfo.getVelocity().toString();
        // prepare and split up text if displaying in large mode
        ArrayList<TextLayout> chunks = null;
        //if(mode == DisplayMode.LARGE){
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
        //}

        // now we can do the heights
        // calculate the maximum height of the text including the ascents and
        // descents of the characters, both lines, padding between lines
        int totalHeight = getImageHeight (timeLayout, positionLayout, chunks);

        int actualTimeHeight = 0;
        int actualTextLineHeight = 0;
        int actualPositionHeight = 0;
        // small - get height of author
        // if(mode == DisplayMode.SMALL){
        //    actualAuthorHeight= (int)(authorLayout.getAscent() + authorLayout.getDescent());
        //}
        // medium - get heights of author and subject
        //else if(mode == DisplayMode.MEDIUM){
        //    actualAuthorHeight= (int)(authorLayout.getAscent() + authorLayout.getDescent());
        //    actualSubjectHeight = (int)(subjectLayout.getAscent() + subjectLayout.getDescent());
        //}
        // large - get heights of author, subject and text
        //else if(mode == DisplayMode.LARGE){
        actualTimeHeight= (int)(timeLayout.getAscent() + timeLayout.getDescent());
        actualPositionHeight = (int)(positionLayout.getAscent() + positionLayout.getDescent());
        TextLayout aLine = chunks.get(0);
        actualTextLineHeight = (int)(aLine.getAscent() + aLine.getDescent());
        //}

        logger.info(" actual height/width:" + totalHeight + "/" + totalWidth);
        logger.info(" desired height/width:" + totalHeight + "/" + totalWidth);
        logger.info(" time:" + time);
        logger.info(" position:" + position);


        // create an image to render the text onto
        BufferedImage tmp0 = new BufferedImage(totalWidth+BORDER_WIDTH*2, totalHeight+BORDER_WIDTH*2, BufferedImage.TYPE_INT_ARGB);
        logger.info(" image height: " + tmp0.getHeight());
        logger.info(" image width: " + tmp0.getWidth());
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
        logger.info(" w: " + w);
        logger.info(" w - bw2: " + (w-BORDER_WIDTH*2));
        g2d.fillRoundRect(x, y, w, h, arc, arc);

        // draw background rectangle's gradient
        Paint op = g2d.getPaint();
        Color dg = new Color(10,10,10,180);
        Color lg = new Color(100,100,100,125);
        GradientPaint p = new GradientPaint(0, (h * 0.20f), lg, 0, (h), dg);
        g2d.setPaint(p);
        logger.info(" filling rounded rec: x y w h " + x + " " + y + " " + w+ " " +h + " ");
        g2d.fillRoundRect(x, y, w, h, arc, arc);

        // reset paint
        g2d.setPaint(op);

        // draw border
        g2d.setStroke(new BasicStroke(BORDER_WIDTH));
        g2d.setColor(Color.BLACK);
        g2d.setPaintMode();
        g2d.drawRoundRect(x, y, w, h, arc, arc);
        // The left and right edges of the rectangle are at x and x�+�width, respectively.
        // The top and bottom edges of the rectangle are at y and y�+�height.

        // used to draw text
        int textX = 0;
        int textY = 0;
        // used to blur shadow
        BufferedImage ret = tmp0;

        // draw author text and shadow always
        logger.info(" draw author");
        textX = 0 + PADDING_LEFT;
        textY = actualTimeHeight + PADDING_TOP;// + paddingTop + borderWidth;

        // draw the shadow of the text
        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(shadowColor);
        System.out.println("shadow x and y: " + textX + " " + textY);
        System.out.println("offsets: " + SHADOW_OFFSET_X + " " + SHADOW_OFFSET_Y);
        System.out.println("desired heights, time, position: " + actualTimeHeight + " " + actualPositionHeight);
        g2d.drawString(time, textX + SHADOW_OFFSET_X, textY + SHADOW_OFFSET_Y);


        // blur the shadows
        ret = blur.filter(tmp0, null);
        // draw the text over the shadow
        g2d = (Graphics2D) ret.getGraphics();
        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(fontColor);
        System.out.println("the TEXT x and y: " + textX + " " + textY);
        g2d.drawString(time, textX, textY);
    
        /*
        // draw subject text if necessary
        if(mode == DisplayMode.MEDIUM || mode == DisplayMode.LARGE){
            logger.info(" draw subject");

            // draw subject text
            // make same left-justification, but different y
            textY += actualPositionHeight + PADDING_LINE;
      
            g2d.setFont(gfxConfig.getPositionFont());
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(gfxConfig.getFontColor());
            g2d.drawString(position, textX, textY);
        }
        // draw the message text if necessary
        if(mode == DisplayMode.LARGE){
        */
            logger.info(" draw message");
            textY += actualPositionHeight + PADDING_LINE;
      
            g2d.setFont(font);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(fontColor);
            for(TextLayout t:chunks){
                t.draw(g2d, textX, textY);
                logger.info(" drawing string:" + t.toString());
                textY += actualTextLineHeight + PADDING_LINE;
            }
        //}

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
     * @param authorRect rectangle bounding the author text
     * @param subjectRect rectangle bounding the subject text
     */
    private int getImageWidth(Rectangle2D timeRect, Rectangle2D positionRect){
        int actualWidth = PADDING_LEFT + PADDING_RIGHT; // 18
        /*
        if(mode == DisplayMode.SMALL){
            // display only the author
            actualWidth += authorRect.getWidth();
        }
        // the maximal length for a line of text
        else 
        */
        if(timeRect.getWidth() > positionRect.getWidth()){
            logger.info("an: time had larger width " + timeRect.getWidth() + " vs " + positionRect.getWidth());
            actualWidth += timeRect.getWidth();
        }
        else{
            logger.info("an: position had equal or larger width " + positionRect.getWidth() + " " +  timeRect.getWidth());
            actualWidth += positionRect.getWidth();
        }
        return actualWidth;
    }

    /**
     * Calculate the appropriate height of the image based on the current DisplayMode
     *
     * Used by getImage.
     * @return the actual height the image should have
     * @param authorLayout TextLayout of author
     * @param subjectLayout TextLayout of author
     * @param chunks contains annotation's text, broken up into lines
     */
    private int getImageHeight(TextLayout timeLayout, TextLayout positionLayout, ArrayList<TextLayout> chunks) {
        int ret = PADDING_BOTTOM + PADDING_TOP;
        // add position and text to height for medium and large versions
        logger.info(" display mode here is: " + mode);
        /*
        if(mode == DisplayMode.SMALL){
            ret += (int) (timeLayout.getAscent() + timeLayout.getDescent() +
                          kernelSize + 1 + SHADOW_OFFSET_Y);
        }
        else if(mode == DisplayMode.MEDIUM || mode == DisplayMode.LARGE){
        */
            ret += (int) (timeLayout.getAscent() + timeLayout.getDescent() +
                          positionLayout.getAscent() + positionLayout.getDescent() +
                          kernelSize + 1 + SHADOW_OFFSET_Y + PADDING_LINE);
        /*
        }
        */

        // also add lines of text from chunks to height for large versions
        //if(mode == DisplayMode.LARGE){
            logger.info(" large, adding chunks inside");
            for(TextLayout t:chunks){
                //        logger.info("chunk: " + t.getAscent() + " " + t.getDescent());
                ret += (int)(t.getAscent() + t.getDescent());
                ret += PADDING_LINE;
            }
        //}
        logger.info(" ret is finally: " + ret);
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
        TextLayout textLayout = new TextLayout(str, font, frc);
                                               
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
        BufferedImage img = getImage();
        if(img == null){
            logger.severe(" image is null!!!");
        }

        float w = img.getWidth();
        float h = img.getHeight();
        float height = 1f;
        //    float factor = height / h;
        float factor = 0.005524862f;
    
        Quad ret = new Quad("SampleDisplay Quad", w * fontSizeModifier, h * fontSizeModifier);
        logger.info(" width, height of quad:" + w + " " + h + "mod size is: " + fontSizeModifier);
        logger.info(" factored width, height of quad:" + w*factor + " " + h*factor + " factor is:" + factor);

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
