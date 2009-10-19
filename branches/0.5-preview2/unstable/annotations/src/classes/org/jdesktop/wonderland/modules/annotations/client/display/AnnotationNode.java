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

package org.jdesktop.wonderland.modules.annotations.client.display;

import org.jdesktop.wonderland.modules.annotations.common.AnnotationSettings;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.renderer.Renderer;
import com.jme.scene.BillboardNode;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.BlendState.TestFunction;

import com.jme.system.DisplaySystem;
import java.awt.image.BufferedImage;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.annotations.client.display.AnnotationEntity.DisplayMode;
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
import org.jdesktop.wonderland.modules.annotations.common.Annotation;


/**
 * A billboarding node that attaches graphics representing an Annotation. Will
 * generate different graphics based on display mode.
 *
 * Heavily modified from TextLabel2D
 * @author mabonner
 */
public class AnnotationNode extends BillboardNode {

  private static Logger logger = Logger.getLogger(AnnotationNode.class.getName());

  /** controls the graphics generation of this AnnotationNode */
  DisplayMode mode;

  /** controls size of font */
  private float fontSizeModifier;

  /** graphical configurations (fonts, colors..) */
  AnnotationSettings gfxConfig;

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


  // strings pulled from annotation this node represents
  String author;
  String subject;
  String text;


  public AnnotationNode(Annotation a, DisplayMode displayMode, AnnotationSettings pc, float fontMod){
    super("anno node");
    mode = displayMode;
    fontSizeModifier = fontMod;
    
    // set pieces
    author = a.getCreator();
    subject = a.getSubject();
    text = a.getText();
    updateKernel();
    // done if the node is hidden
    if(displayMode == DisplayMode.HIDDEN){
      logger.info("[anno node] hidden, not filling with anything");
      return;
    }

    // set up graphics configurations
    gfxConfig = pc;
    if (pc.getFont() == null) {
      logger.info("[anno node] font is null!");
      gfxConfig.setFont(Font.decode("Sans PLAIN 40"));
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
    
    TextLayout authorLayout = new TextLayout(author,
            gfxConfig.getAuthorFont(), frc);
    Rectangle2D authorRect = authorLayout.getBounds();

    // and for subject line
    if(subject == null || subject.length() == 0){
      subject = " ";
    }
    TextLayout subjectLayout = new TextLayout(subject,
            gfxConfig.getSubjectFont(), frc);
    Rectangle2D subjectRect = subjectLayout.getBounds();

    // calculate the width of the label with shadow and blur
    // width depends on which is longer, subject or author name
    int totalWidth = getImageWidth(authorRect, subjectRect);
    // prepare and split up text if displaying in large mode
    ArrayList<TextLayout> chunks = null;
    if(mode == DisplayMode.LARGE){
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
    }

    // now we can do the heights
    // calculate the maximum height of the text including the ascents and
    // descents of the characters, both lines, padding between lines
    int totalHeight = getImageHeight(authorLayout, subjectLayout, chunks);

    int actualAuthorHeight = 0;
    int actualTextLineHeight = 0;
    int actualSubjectHeight = 0;
    // small - get height of author
    if(mode == DisplayMode.SMALL){
      actualAuthorHeight= (int)(authorLayout.getAscent() + authorLayout.getDescent());
    }
    // medium - get heights of author and subject
    else if(mode == DisplayMode.MEDIUM){
      actualAuthorHeight= (int)(authorLayout.getAscent() + authorLayout.getDescent());
      actualSubjectHeight = (int)(subjectLayout.getAscent() + subjectLayout.getDescent());
    }
    // large - get heights of author, subject and text
    else if(mode == DisplayMode.LARGE){
      actualAuthorHeight= (int)(authorLayout.getAscent() + authorLayout.getDescent());
      actualSubjectHeight = (int)(subjectLayout.getAscent() + subjectLayout.getDescent());
      TextLayout aLine = chunks.get(0);
      actualTextLineHeight = (int)(aLine.getAscent() + aLine.getDescent());
    }

    logger.info("[anno node] actual height/width:" + totalHeight + "/" + totalWidth);
    logger.info("[anno node] desired height/width:" + totalHeight + "/" + totalWidth);
    logger.info("[anno node] author:" + author);
    logger.info("[anno node] subject:" + subject);
    logger.info("[anno node] author font:" + gfxConfig.getAuthorFont());
    logger.info("[anno node] subject font:" + gfxConfig.getSubjectFont());



    // create an image to render the text onto
    BufferedImage tmp0 = new BufferedImage(totalWidth+BORDER_WIDTH*2, totalHeight+BORDER_WIDTH*2, BufferedImage.TYPE_INT_ARGB);
    logger.info("[anno node] image height: " + tmp0.getHeight());
    logger.info("[anno node] image width: " + tmp0.getWidth());
    Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
    g2d.setFont(gfxConfig.getAuthorFont());

    // draw background
    int x = 0 + BORDER_WIDTH;
    int y = 0 + BORDER_WIDTH;

    int h = totalHeight;
    int w = totalWidth;

    int arc = 60;

    // draw background rectangle
    g2d.setColor(gfxConfig.getBackgroundColor());
    logger.info("[anno node] w: " + w);
    logger.info("[anno node] w - bw2: " + (w-BORDER_WIDTH*2));
    g2d.fillRoundRect(x, y, w, h, arc, arc);

    // draw background rectangle's gradient
    Paint op = g2d.getPaint();
    Color dg = new Color(10,10,10,180);
    Color lg = new Color(100,100,100,125);
    GradientPaint p = new GradientPaint(0, (h * 0.20f), lg, 0, (h), dg);
    g2d.setPaint(p);
    logger.info("[anno node] filling rounded rec: x y w h " + x + " " + y + " " + w+ " " +h + " ");
    g2d.fillRoundRect(x, y, w, h, arc, arc);

    // reset paint
    g2d.setPaint(op);

    // draw border
    g2d.setStroke(new BasicStroke(BORDER_WIDTH));
    g2d.setColor(Color.BLACK);
    g2d.setPaintMode();
    g2d.drawRoundRect(x, y, w, h, arc, arc);
    // The left and right edges of the rectangle are at x and xÊ+Êwidth, respectively.
    // The top and bottom edges of the rectangle are at y and yÊ+Êheight.

    // used to draw text
    int textX = 0;
    int textY = 0;
    // used to blur shadow
    BufferedImage ret = tmp0;

    // draw author text and shadow always
    logger.info("[anno node] draw author");
    textX = 0 + PADDING_LEFT;
    textY = actualAuthorHeight + PADDING_TOP;// + paddingTop + borderWidth;

    // draw the shadow of the text
    g2d.setFont(gfxConfig.getAuthorFont());
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2d.setColor(gfxConfig.getShadowColor());
    System.out.println("shadow x and y: " + textX + " " + textY);
    System.out.println("offsets: " + SHADOW_OFFSET_X + " " + SHADOW_OFFSET_Y);
    System.out.println("desired heights, author subj: " + actualAuthorHeight + " " + actualSubjectHeight);
    g2d.drawString(author, textX + SHADOW_OFFSET_X, textY + SHADOW_OFFSET_Y);


    // blur the shadows
    ret = blur.filter(tmp0, null);
    // draw the text over the shadow
    g2d = (Graphics2D) ret.getGraphics();
    g2d.setFont(gfxConfig.getAuthorFont());
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2d.setColor(gfxConfig.getFontColor());
    System.out.println("the TEXT x and y: " + textX + " " + textY);
    g2d.drawString(author, textX, textY);
    
    // draw subject text if necessary
    if(mode == DisplayMode.MEDIUM || mode == DisplayMode.LARGE){
      logger.info("[anno node] draw subject");

      // draw subject text
      // make same left-justification, but different y
      textY += actualSubjectHeight + PADDING_LINE;
      
      g2d.setFont(gfxConfig.getSubjectFont());
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setColor(gfxConfig.getFontColor());
      g2d.drawString(subject, textX, textY);
    }
    // draw the message text if necessary
    if(mode == DisplayMode.LARGE){
      logger.info("[anno node] draw message");
      textY += actualSubjectHeight + PADDING_LINE;
      
      g2d.setFont(gfxConfig.getSubjectFont());
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setColor(gfxConfig.getFontColor());
      for(TextLayout t:chunks){
        t.draw(g2d, textX, textY);
        logger.info("[anno node] drawing string:" + t.toString());
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
   * @param authorRect rectangle bounding the author text
   * @param subjectRect rectangle bounding the subject text
   */
  private int getImageWidth(Rectangle2D authorRect, Rectangle2D subjectRect){
    int actualWidth = PADDING_LEFT + PADDING_RIGHT; // 18
    if(mode == DisplayMode.SMALL){
      // display only the author
      actualWidth += authorRect.getWidth();
    }
    // the maximal length for a line of text
    else if(authorRect.getWidth() > subjectRect.getWidth()){
      logger.info("an: author had larger width " + authorRect.getWidth() + " vs " + subjectRect.getWidth());
      actualWidth += authorRect.getWidth();
    }
    else{
      logger.info("an: subject had equal or larger width " + subjectRect.getWidth() + " " +  authorRect.getWidth());
      actualWidth += subjectRect.getWidth();
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
  private int getImageHeight(TextLayout authorLayout, TextLayout subjectLayout, ArrayList<TextLayout> chunks) {
    int ret = PADDING_BOTTOM + PADDING_TOP;
    // add subject and text to height for medium and large versions
    logger.info("[anno node] display mode here is: " + mode);
    if(mode == DisplayMode.SMALL){
      ret += (int) (authorLayout.getAscent() + authorLayout.getDescent() +
                 kernelSize + 1 + SHADOW_OFFSET_Y);
    }
    else if(mode == DisplayMode.MEDIUM || mode == DisplayMode.LARGE){
      ret += (int) (authorLayout.getAscent() + authorLayout.getDescent() +
                  subjectLayout.getAscent() + subjectLayout.getDescent() +
                  kernelSize + 1 + SHADOW_OFFSET_Y + PADDING_LINE);
    }

    // also add lines of text from chunks to height for large versions
    if(mode == DisplayMode.LARGE){
      logger.info("[anno node] large, adding chunks inside");
      for(TextLayout t:chunks){
//        logger.info("chunk: " + t.getAscent() + " " + t.getDescent());
        ret += (int)(t.getAscent() + t.getDescent());
        ret += PADDING_LINE;
      }
    }
    logger.info("[anno node] ret is finally: " + ret);
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
            gfxConfig.getSubjectFont(), frc);
    Rectangle2D textRect = textLayout.getBounds();
    // does text need to be split?
    if(textRect.getWidth() > lineWidth){

      AttributedString asText = new AttributedString(str);
      asText.addAttribute(TextAttribute.FONT, gfxConfig.getSubjectFont());
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
      logger.severe("[anno node] image is null!!!");
    }

    float w = img.getWidth();
    float h = img.getHeight();
    float height = 1f;
//    float factor = height / h;
    float factor = 0.005524862f;
    
//    Quad ret = new Quad("anno node", w * factor, h * factor);
    Quad ret = new Quad("anno node", w * fontSizeModifier, h * fontSizeModifier);
    logger.info("[anno node] width, height of quad:" + w + " " + h + "mod size is: " + fontSizeModifier);
    logger.info("[anno node] factored width, height of quad:" + w*factor + " " + h*factor + " factor is:" + factor);

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
