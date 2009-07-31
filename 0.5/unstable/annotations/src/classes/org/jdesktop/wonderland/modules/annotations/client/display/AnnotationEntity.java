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

import com.jme.bounding.BoundingBox;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;

import com.jme.image.Texture;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.BillboardNode;
import com.jme.scene.Node;
import com.jme.scene.Spatial.LightCombineMode;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.BlendState.TestFunction;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;

import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.CellRefComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.modules.annotations.common.Annotation;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;


/**
 * A billboarding, in-world display for an annotation.
 * Modified from TextLabel2D.
 * @author mabonner
 */
public class AnnotationEntity extends Entity {




  /** Set how this annotation will display itself in world
   * When adding a new type, create a toString method that returns
   * a pretty display string for the global menu. Also create a rawValue
   * method for getting the default string version of the enum.
   */
  public enum DisplayMode {
    HIDDEN{
      @Override
      public String toString(){ return "Hidden";}
      public String rawValue(){ return "HIDDEN";}
    },
    SMALL{
      @Override
      public String toString(){ return "Stub";}
      public String rawValue(){ return "SMALL";}
    },
    MEDIUM{
      @Override
      public String toString(){ return "Partial";}
      public String rawValue(){ return "MEDIUM";}
    },
    LARGE {
      @Override
      public String toString(){ return "Full";}
      public String rawValue(){ return "LARGE";}
    };
    public abstract String rawValue();
    public static DisplayMode stringToRawValue(String in){
      for(DisplayMode dm:DisplayMode.values()){
        if(dm.toString().equals(in)){
          return dm;
        }
      }
      return null;
    }
  }

  private static Logger logger = Logger.getLogger(AnnotationEntity.class.getName());

  private String author;
  private String subject;
  private String text;
  private float blurIntensity = 0.1f;
  private int kernelSize = 5;
  private ConvolveOp blur;

  private float authorFontResolution = 40f;
  private float subjectFontResolution = 40f;
  private int shadowOffsetX = 2;
  private int shadowOffsetY = 2;

  // padding between text and edges
  private int paddingLeft = 30;
  private int paddingRight = 30;
  private int paddingTop = 5;
  private int paddingBottom = 5;

  // padding between Author and Title
  private int paddingLine = 5;

  /** width of border */
  private int borderWidth = 6;

  private Font font = null;
  private Font authorFont;
  private Font subjectFont;
  private float height = 1f;
  private FontRenderContext fontRenderContext = null;
  private Quad quad;

  private PanelConfig pc;

  DisplayMode displayMode = DisplayMode.HIDDEN;

  private int MIN_WIDTH = 475;

  // node to display this annotation
  Node node;

  // the ID of the annotation this entity represents
  private final MetadataID annoID;

  /** Whether this entity has been added to the JME manager */
  private boolean entityAdded = false;

  /** local translation of the entity */
  private Vector3f localTranslation = null;

  public MetadataID getAnnoID() {
    return annoID;
  }

  // mouse listeners on this annotation
  Set<MouseListener> listeners = new HashSet<MouseListener>();

  protected static ZBufferState zbuf = null;
  static {
      RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
      zbuf = (ZBufferState)rm.createRendererState(StateType.ZBuffer);
      zbuf.setEnabled(true);
      zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
  }



  public PanelConfig getPanelConfig() {
    return pc;
  }

  public void setPanelConfig(PanelConfig pc) {
    this.pc = pc;
  }

  /**
   * Constructor
   * @param a the annotation to represent
   * @param pc initial font/color settings
   * @param cell cell to reference in CellRefComponent, for context menu
   * @param mode initial DisplayMode
   */
  public AnnotationEntity(Annotation a, PanelConfig pc, Cell cell, DisplayMode mode) {
    super("annotation with id: " + a.getID());

    this.author = a.getCreator();
    this.subject = a.getSubject();
    this.text = a.getText();
    this.pc = pc;
    this.font = pc.getFont();
    this.annoID = a.getID();

    logger.info("Annotation with id " + a.getID() + " author: " + author);
    updateKernel();
    if (font == null) {
      logger.info("AN: font is null!");
        font = Font.decode("Sans PLAIN 40");
    }
    setFont(font);
    if (font == null) {
      logger.info("AN: font is STILL null!");
    }

    // Create node and render component to attach
//    node = getBillboard();
//    RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
//    RenderComponent rComp = rm.createRenderComponent(node);
//    this.addComponent(RenderComponent.class, rComp);
    attachMouseListener(new MouseListener(this));

//    node.setRenderState(zbuf);
//    node.setModelBound(new BoundingBox());
//    node.updateModelBound();
//
//    makeEntityPickable(this, node);

    // add a cell ref component to this entity
    // we add this in order to use the global context menu system...
    // the context menu won't show up unless the fired contextevent has a
    // cell ref component.
    this.addComponent(CellRefComponent.class, new CellRefComponent(cell));

    // set this using setDisplayMode - this will result in the entity
    // getting added to the world if necessary
    logger.info("display mode is: " + displayMode);
    displayMode = DisplayMode.HIDDEN;
    logger.info("now mode is: " + displayMode);
    setDisplayMode(mode);

    return;
  }



  public void setLocalTranslation(Vector3f v){
    logger.info("[anno ent] setting local trans to " + v);
    localTranslation = v;
    if(node != null){
      node.setLocalTranslation(v);
    }
  }

  public void setFont(Font font) {
      authorFont = new Font(font.getName(),Font.BOLD,font.getSize());
      BufferedImage tmp0 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
      authorFont = authorFont.deriveFont(authorFontResolution);
      int intendedSubjSize = (int)(font.getSize() * 0.8f);
      Font f = new Font(font.getName(),Font.ITALIC,(int)(font.getSize() * 0.8f));
      subjectFont = f.deriveFont(subjectFontResolution);
      fontRenderContext = g2d.getFontRenderContext();
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
      this.authorFontResolution = fontResolution;
  }

  private void updateKernel() {
      float[] kernel = new float[kernelSize * kernelSize];
      Arrays.fill(kernel, blurIntensity);
      blur = new ConvolveOp(new Kernel(kernelSize, kernelSize, kernel));
  }

  /**
   * Generate an image of the label
   *
   * @param scaleFactors is set to the factors needed to adjust texture coords
   * to the next power-of-two-sized resulting image
   */
  private BufferedImage getImage() {
    // calculate the size of the label text rendered with the specified font
    System.out.println("getting image");

    TextLayout authorLayout = new TextLayout(author, authorFont, fontRenderContext);
    Rectangle2D authorRect = authorLayout.getBounds();

    // and for subject line
    if(subject == null || subject.length() == 0){
      subject = " ";
    }
    TextLayout subjectLayout = new TextLayout(subject, subjectFont, fontRenderContext);
    Rectangle2D subjectRect = subjectLayout.getBounds();

    // calculate the width of the label with shadow and blur
    // width depends on which is longer, subject or author name
    int totalWidth = getImageWidth(authorRect, subjectRect);
    // prepare and split up text if displaying in large mode
    ArrayList<TextLayout> chunks = null;
    if(displayMode == DisplayMode.LARGE){
      if(totalWidth < MIN_WIDTH){
        totalWidth = MIN_WIDTH;
      }
      chunks = new ArrayList<TextLayout>();
      // make text fit into desired width
      int singleLineWidth = getLineWidth(authorRect, subjectRect);
      splitText(chunks, singleLineWidth);
    }

    // now we can do the heights
    // calculate the maximum height of the text including the ascents and
    // descents of the characters, both lines, padding between lines
    int totalHeight = getImageHeight(authorLayout, subjectLayout, chunks);

    int actualAuthorHeight = 0;
    int actualTextLineHeight = 0;
    int actualSubjectHeight = 0;
    // medium - get heights of author and subject
    if(displayMode == DisplayMode.MEDIUM){
      actualAuthorHeight= (int)(authorLayout.getAscent() + authorLayout.getDescent());
      actualSubjectHeight = (int)(subjectLayout.getAscent() + subjectLayout.getDescent());
    }
    // large - get heights of author, subject and text
    else if(displayMode == DisplayMode.LARGE){
      actualAuthorHeight= (int)(authorLayout.getAscent() + authorLayout.getDescent());
      actualSubjectHeight = (int)(subjectLayout.getAscent() + subjectLayout.getDescent());
      TextLayout aLine = chunks.get(0);
      actualTextLineHeight = (int)(aLine.getAscent() + aLine.getDescent());
    }

    logger.info("[ANNO ENT] actual height/width:" + totalHeight + "/" + totalWidth);
    logger.info("[ANNO ENT] desired height/width:" + totalHeight + "/" + totalWidth);
    logger.info("[ANNO ENT] author:" + author);
    logger.info("[ANNO ENT] subject:" + subject);
    logger.info("[ANNO ENT] author font:" + authorFont);
    logger.info("[ANNO ENT] subject font:" + subjectFont);



    // create an image to render the text onto
    BufferedImage tmp0 = new BufferedImage(totalWidth+borderWidth*2, totalHeight+borderWidth*2, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
    g2d.setFont(authorFont);

    // draw background
    int x = 0 + borderWidth;
    int y = 0 + borderWidth;

    int h = totalHeight;
    int w = totalWidth;

    int arc = 60;

    // draw background rectangle
    g2d.setColor(pc.getBackgroundColor());
    logger.info("[anno ent] w: " + w);
    logger.info("[anno ent] w - bw2: " + (w-borderWidth*2));
    g2d.fillRoundRect(x, y, w, h, arc, arc);

    // draw background rectangle's gradient
    Paint op = g2d.getPaint();
    Color dg = new Color(60,60,60,175);
    Color lg = new Color(100,100,100,0);
    GradientPaint p = new GradientPaint(0, (h * 0.20f), lg, 0, (h), dg);
    g2d.setPaint(p);
    g2d.fillRoundRect(x, y, w, h, arc, arc);

    // reset paint
    g2d.setPaint(op);

    // draw border
    g2d.setStroke(new BasicStroke(borderWidth));
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
    // draw author and subject text if necessary
    if(displayMode == DisplayMode.MEDIUM || displayMode == DisplayMode.LARGE){
      logger.info("[ANNO ENT] draw subject and author");
      textX = 0 + paddingLeft;
      textY = actualAuthorHeight + paddingTop;// + paddingTop + borderWidth;

      // draw the shadow of the text
      g2d.setFont(authorFont);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setColor(pc.getShadowColor());
      System.out.println("shadow x and y: " + textX + " " + textY);
      System.out.println("offsets: " + shadowOffsetX + " " + shadowOffsetY);
      System.out.println("desired heights, author subj: " + actualAuthorHeight + " " + actualSubjectHeight);
      g2d.drawString(author, textX + shadowOffsetX, textY + shadowOffsetY);


      // blur the shadows
      ret = blur.filter(tmp0, null);
      // draw the text over the shadow
      g2d = (Graphics2D) ret.getGraphics();
      g2d.setFont(authorFont);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setColor(pc.getFontColor());
      System.out.println("the TEXT x and y: " + textX + " " + textY);
      g2d.drawString(author, textX, textY);


      // draw subject text
      // make same left-justification, but different y
      textY += actualSubjectHeight + paddingLine;

      g2d = (Graphics2D) ret.getGraphics();
      g2d.setFont(subjectFont);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setColor(pc.getFontColor());
      g2d.drawString(subject, textX, textY);
    }
    // draw the message text if necessary
    if(displayMode == DisplayMode.LARGE){
      logger.info("[ANNO ENT] draw message");
      textY += actualSubjectHeight + paddingLine;

      g2d = (Graphics2D) ret.getGraphics();
      g2d.setFont(subjectFont);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setColor(pc.getFontColor());
      for(TextLayout t:chunks){
        t.draw(g2d, textX, textY);
        logger.info("[anno ent] drawing string:" + t.toString());
        textY += actualTextLineHeight + paddingLine;
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
    int actualWidth = paddingLeft + paddingRight; // 18
    if(displayMode == DisplayMode.SMALL){
      return actualWidth;
    }

    // the maximal length for a line of text
    if(authorRect.getWidth() > subjectRect.getWidth()){
      System.out.println("an: author had larger width " + authorRect.getWidth() + " vs " + subjectRect.getWidth());
      actualWidth += authorRect.getWidth();
    }
    else{
      System.out.println("an: subject had equal or larger width " + subjectRect.getWidth() + " " +  authorRect.getWidth());
      actualWidth += subjectRect.getWidth();
    }
    return actualWidth;
  }

  /**
   * Compares author and subject to find the maximal line width
   *
   * Used by getImage.
   * @return the actual width the image should have
   * @param authorRect rectangle bounding the author text
   * @param subjectRect rectangle bounding the subject text
   */
  private int getLineWidth(Rectangle2D authorRect, Rectangle2D subjectRect){
    int width;
    if(authorRect.getWidth() > subjectRect.getWidth()){
      logger.info("[ANNO ENT] an: author had larger width " + authorRect.getWidth() + " vs " + subjectRect.getWidth());
      width = (int) authorRect.getWidth();
    }
    else{
      logger.info("[ANNO ENT] an: subject had equal or larger width " + subjectRect.getWidth() + " " +  authorRect.getWidth());
      width = (int) subjectRect.getWidth();
    }
    return width;
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
    int ret = paddingBottom + paddingTop;
    // add subject and text to height for medium and large versions
//    (int) (authorLayout.getAscent() + authorLayout.getDescent() +
//                subjectLayout.getAscent() + subjectLayout.getDescent() +
//                kernelSize + 1 + shadowOffsetY + paddingBottom + paddingTop + paddingLine); // 23
    logger.info("[anno ent] display mode here is: " + displayMode);
    if(displayMode == DisplayMode.SMALL){
      // TODO
    }
    else if(displayMode == DisplayMode.MEDIUM || displayMode == DisplayMode.LARGE){
      logger.info("[anno ent] medium/large, adding author and subj");
      logger.info("[anno ent] good #s inside:" + authorLayout.getAscent() + " " + authorLayout.getDescent() + " "
              + subjectLayout.getAscent() + " " + subjectLayout.getDescent() + " " + kernelSize + " " + shadowOffsetY + " " + paddingLine);
      logger.info("[anno ent] author asc is: " + authorLayout.getAscent());
      ret += (int) (authorLayout.getAscent() + authorLayout.getDescent() +
                  subjectLayout.getAscent() + subjectLayout.getDescent() +
                  kernelSize + 1 + shadowOffsetY + paddingLine);
      logger.info("ret after auth/subj is now: " + ret);
    }
    // also add lines of text from chunks to height for large versions
    if(displayMode == DisplayMode.LARGE){
      logger.info("[anno ent] large, adding chunks inside");
      for(TextLayout t:chunks){
        logger.info("chunk: " + t.getAscent() + " " + t.getDescent());
        ret += (int)(t.getAscent() + t.getDescent());
        ret += paddingLine;
      }
    }
    logger.info("[anno ent] ret is finally: " + ret);
    return ret;
  }

  /**
   * Calculates width of text, splits onto multiple lines of maximum length
   * lineWidth if necessary. Stores line(s) in the chunks ArrayList.
   * @param chunks ArrayList to store line(s) in
   * @param lineWidth maximum length of each line
   */
  private void splitText(ArrayList<TextLayout> chunks, int lineWidth){
    TextLayout textLayout = new TextLayout(text, subjectFont, fontRenderContext);
    Rectangle2D textRect = textLayout.getBounds();
    // does text need to be split?
    if(textRect.getWidth() > lineWidth){

      AttributedString asText = new AttributedString(text);
      asText.addAttribute(TextAttribute.FONT, subjectFont);
      AttributedCharacterIterator asItr = asText.getIterator();

      int start = asItr.getBeginIndex();
      int end = asItr.getEndIndex();

      LineBreakMeasurer line = new LineBreakMeasurer(asItr, fontRenderContext);
//          LineBreakMeasurer line = new LineBreakMeasurer(asItr, new FontRenderContext(null, false, false));
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
      logger.severe("[anno ent] image is null!!!");
    }

    float w = img.getWidth();
    float h = img.getHeight();
    float factor = height / h;

    Quad ret = new Quad("textLabel2d", w * factor, h * factor);
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

    this.quad = ret;
    return ret;
  }

  /**
   * The billboarding node that displays this annotation in-world
   * @return
   */
  private Node getBillboard() {
    if(displayMode == DisplayMode.HIDDEN){
      logger.info("bb: GENERATING BLANK NODE");
      return new Node();
    }
    BillboardNode bb = new BillboardNode("bb");
    bb.attachChild(getQuad());
    // set bounds to make pickable
    bb.setModelBound(new BoundingBox());
    bb.updateModelBound();
    return bb;
  }



  /**
   * Returns the node used to display this annotation
   *
   */
  public Node getNode() {
      return node;
  }













  MouseListener mouseListener;

  /**
   * Attach a mouse listener to this entity.
   * @param ml the listener to attach
   */
  private void attachMouseListener(MouseListener ml) {
    ml.addToEntity(this);
    listeners.add(ml);
  }

  /** A basic listener for 3D mouse events */
  protected class MouseListener extends EventClassListener {
    private Entity ent;

    public MouseListener(Entity e){
      ent = e;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{MouseEvent3D.class};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitEvent(final Event event) {
//      logger.info("AN: commit event!");
      MouseEvent3D me3d = (MouseEvent3D) event;
      MouseEvent me = (MouseEvent) me3d.getAwtEvent();
      if(me.getID() == MouseEvent.MOUSE_CLICKED){
        if(me.getButton() == MouseEvent.BUTTON3){
          logger.info("AN: right click!");
          // display the context menu
          // build an AnnotationContextEvent, fire it for the
          // context menu system to pick up
          InputManager inputManager = InputManager.inputManager();
          ArrayList<Entity> l = new ArrayList<Entity>();
          l.add(ent);
          inputManager.postEvent(new AnnotationContextEvent(l, me));


        }
        else if(me.getButton() == MouseEvent.BUTTON1
                && me.isShiftDown()){
          logger.info("AN: shift-left click!");
          // cycle to next display mode
          cycleDisplayMode();
        }
      }
    }

    /**
     * Cycles the node's display mode. Cycles between visible modes only (e.g.
     * LARGE cycles to SMALL, not HIDDEN). Cycling a HIDDEN node has no effect.
     * and back to HIDDEN.
     */
    private void cycleDisplayMode() {
      switch(displayMode){
        case HIDDEN:
          // do nothing
          break;
        case SMALL:
          setDisplayMode(DisplayMode.MEDIUM);
          break;
        case MEDIUM:
          setDisplayMode(DisplayMode.LARGE);
          break;
        case LARGE:
          setDisplayMode(DisplayMode.SMALL);
          break;
      }
    }
  }






  /**
   * Sets how the annotation is displayed. To hide the annotation, set this to
   * DisplayMode.HIDDEN
   *
   * @param newDisplayMode what style to display the node as
   */
  public synchronized void setDisplayMode(DisplayMode newDisplayMode) {

    logger.info("[ANNO ENT] setting display mode: " + newDisplayMode);
    logger.info("[ANNO ENT] current display mode: " + displayMode);
    // if node is currently HIDDEN and the new mode is not HIDDEN (is visible),
//     make the node visible in the world
    if (newDisplayMode != DisplayMode.HIDDEN) {
      logger.info("[ANNO ENT] will display");
      displayMode = newDisplayMode;
      logger.info("[ANNO ENT] display mode is now:" + displayMode);

      // refreshes the node to the new DisplayMode's version
      node = getBillboard();
      // this is unnecessary but it can't hurt, it guarantees we are operating
      // on the nodes we think we are in the updater thread
      final Node newNode = node;

      RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
      RenderComponent rComp = rm.createRenderComponent(node);
      this.removeComponent(RenderComponent.class);
      this.addComponent(RenderComponent.class, rComp);

      node.setRenderState(zbuf);
      node.setModelBound(new BoundingBox());
      node.updateModelBound();

      makeEntityPickable(this, node);

      RenderUpdater updater = new RenderUpdater() {
        public void update(Object arg0) {
          AnnotationEntity ent = (AnnotationEntity)arg0;
          ClientContextJME.getWorldManager().removeEntity(ent);
          entityAdded = false;
          if(localTranslation != null){
            node.setLocalTranslation(localTranslation);
            logger.info("resetting location " + localTranslation);
          }
          else{
            logger.info("location was null");
          }
//          Node rootNode = ent.getNode();
          logger.info("[ANNO ENT] adding entity");
          ClientContextJME.getWorldManager().addEntity(ent);
          entityAdded = true;
          ClientContextJME.getWorldManager().addToUpdateList(newNode);
        }
      };



      WorldManager wm = ClientContextJME.getWorldManager();
      wm.addRenderUpdater(updater, this);

      return;
    }
    // If we want to make the affordance invisible and it already is
    // visible, then make it invisible
    if (newDisplayMode == displayMode.HIDDEN && displayMode != displayMode.HIDDEN) {
      logger.info("[ANNO ENT] will be hidden");
      RenderUpdater updater = new RenderUpdater() {
        public void update(Object arg0) {
          AnnotationEntity ent = (AnnotationEntity)arg0;
          ClientContextJME.getWorldManager().removeEntity(ent);
          entityAdded = false;
//                    logger.info("making non-pickable");
//                    ent.removeComponent(CollisionComponent.class);
        }
      };
      WorldManager wm = ClientContextJME.getWorldManager();
      wm.addRenderUpdater(updater, this);
      displayMode = displayMode.HIDDEN;
      return;
    }
//    logger.info("[ANNO ENT] did nothing!");
  }

  /**
   * Check the current display mode of this annotation entity
   */
  public DisplayMode getDisplayMode(){
    return displayMode;
  }

  /**
   * Make this entity pickable by adding a collision component to it.
   */
  protected void makeEntityPickable(Entity entity, Node node) {
      JMECollisionSystem collisionSystem = (JMECollisionSystem)
              ClientContextJME.getWorldManager().getCollisionManager().
              loadCollisionSystem(JMECollisionSystem.class);

      CollisionComponent cc = collisionSystem.createCollisionComponent(node);
      entity.addComponent(CollisionComponent.class, cc);
  }

  /**
   * Clean up listeners etc so this annotation can be properly garbage
   * collected.
   */
  public void dispose() {
      setDisplayMode(DisplayMode.HIDDEN);
      for(MouseListener ml : listeners){
        ml.removeFromEntity(this);
        ml = null;
        listeners.clear();
      }
  }


//  class CtxListener implements ContextMenuListener{
//
//    public void contextMenuDisplayed(ContextEvent event) {
//      logger.info("[ANNO COMPO] ctx menu displayed, primary entity:" + event.getPrimaryEntity() + " name is " + event.getPrimaryEntity().getName());
//      for(Entity e: event.getEntityList()){
//        logger.info("[ANNO COMPO] entity named " + e.getName());
//      }
//    }
//  }




}
