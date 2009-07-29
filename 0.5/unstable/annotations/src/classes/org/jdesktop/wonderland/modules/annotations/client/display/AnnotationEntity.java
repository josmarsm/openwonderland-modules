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
import com.jme.bounding.BoundingVolume;
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
import com.jme.math.FastMath;
import com.jme.math.Vector2f;
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
import org.jdesktop.wonderland.client.contextmenu.ContextMenuManager.ContextMenuListener;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;

import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.CellRefComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.modules.annotations.common.Annotation;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;


/**
 * A billboarding, in-world display for an annotation.
 * Modified from TextLabel2D.
 * @author mabonner
 */
public class AnnotationEntity extends Entity {
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

  private boolean isVisible = false;

  // root to add this node to
  Node sceneRoot;
  // node to display this annotation
  Node node;

  // the ID of the annotation this entity represents
  private final MetadataID annoID;

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

  public AnnotationEntity(Annotation a, PanelConfig pc, Node sceneRoot, Cell cell) {
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
    node = getBillboard();
    RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
    RenderComponent rComp = rm.createRenderComponent(node);
    this.addComponent(RenderComponent.class, rComp);
    attachMouseListener(new MouseListener(this));

    node.setRenderState(zbuf);
    node.setModelBound(new BoundingBox());
    node.updateModelBound();

    makeEntityPickable(this, node);

    // add a cell ref component to this entity
    // we add this in order to use the global context menu system...
    // the context menu won't show up unless the fired contextevent has a
    // cell ref component.
    this.addComponent(CellRefComponent.class, new CellRefComponent(cell));

    return;
  }



  public void setLocalTranslation(Vector3f v){
    node.setLocalTranslation(v);
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
  private BufferedImage getImage(Vector2f scaleFactors) {
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
        int actualWidth = paddingLeft + paddingRight; // 18
        // the maximal length for a line of text
        int lineWidth;
        if(authorRect.getWidth() > subjectRect.getWidth()){
          System.out.println("an: author had larger width " + authorRect.getWidth() + " vs " + subjectRect.getWidth());
          actualWidth += authorRect.getWidth();
          lineWidth = (int)authorRect.getWidth();
        }
        else{
          System.out.println("an: subject had equal or larger width " + subjectRect.getWidth() + " " +  authorRect.getWidth());
          actualWidth += subjectRect.getWidth();
          lineWidth = (int)subjectRect.getWidth();
        }


        int desiredWidth = actualWidth;

        // make subject fit into desired width
        TextLayout textLayout = new TextLayout(text, subjectFont, fontRenderContext);
        Rectangle2D textRect = textLayout.getBounds();

//        Rectangle2D subjectRect = subjectLayout.getBounds();
//        // break into chunks
//        ArrayList<String> chunks = new ArrayList<String>();
//        if(textRect.getWidth() > desiredWidth){
//          while(text.length() > 0){
//            TextLayout chunk = new TextLayout(text.charAt(0));
//            while(chunk)
//          }
//
//        }
        boolean splitText = false;
        ArrayList<TextLayout> chunks = new ArrayList<TextLayout>();
        if(textRect.getWidth() > desiredWidth){

          splitText = true;

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
            TextLayout layout = line.nextLayout(lineWidth);
            chunks.add(layout);
          }

        }
        else{
          chunks.add(textLayout);
        }



        // now we can do the heights
        // calculate the maximum height of the text including the ascents and
        // descents of the characters, both lines, padding between lines
        int actualHeight = (int) (authorLayout.getAscent() + authorLayout.getDescent() +
                subjectLayout.getAscent() + subjectLayout.getDescent() +
                kernelSize + 1 + shadowOffsetY + paddingBottom + paddingTop + paddingLine); // 23
        for(TextLayout t:chunks){
          actualHeight += (int)(t.getAscent() + t.getDescent());
          actualHeight += paddingLine;
        }

        TextLayout aLine = chunks.get(0);
        int actualAuthorHeight = (int)(authorLayout.getAscent() + authorLayout.getDescent());
        int actualSubjectHeight = (int)(subjectLayout.getAscent() + subjectLayout.getDescent());
        int actualTextLineHeight = (int)(aLine.getAscent() + aLine.getDescent());
        int desiredHeight = actualHeight;
        int desiredAuthorHeight = actualAuthorHeight;
        int desiredSubjectHeight = actualSubjectHeight;
        int desiredTextLineHeight = actualTextLineHeight;

        System.out.println("actual height/width:" + actualHeight + "/" + actualWidth);
        System.out.println("desired height/width:" + desiredHeight + "/" + desiredWidth);
        System.out.println("author:" + author);
        System.out.println("subject:" + subject);



        // create an image to render the text onto
        BufferedImage tmp0 = new BufferedImage(desiredWidth+borderWidth*2, desiredHeight+borderWidth*2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) tmp0.getGraphics();
        g2d.setFont(authorFont);

        // draw background
        int x = 0 + borderWidth;
        int y = 0 + borderWidth;

        int h = desiredHeight;
        int w = desiredWidth;

        int arc = 60;

        // debugging bg
        g2d.setColor(Color.BLUE);
//        g2d.fillRect(0, 0, w, h);

        // draw background rectangle
        g2d.setColor(Color.RED);
        System.out.println("w: " + w);
        System.out.println("w - bw2: " + (w-borderWidth*2));
        g2d.fillRoundRect(x, y, w, h, arc, arc);

        // draw background rectangle's gradient
        Paint op = g2d.getPaint();
        Color dg = new Color(65,65,65,170);
        Color lg = new Color(100,100,100,0);
        GradientPaint p = new GradientPaint(0, (h * 0.20f), lg, 0, (h), dg);
        g2d.setPaint(p);
        g2d.fillRoundRect(x, y, w, h, arc, arc);
//        g2d.fillRect(0, 0, w, h);
        // reset paint
        g2d.setPaint(op);

        // draw border
        g2d.setStroke(new BasicStroke(borderWidth));
        g2d.setColor(Color.BLACK);
        g2d.setPaintMode();
        g2d.drawRoundRect(x, y, w, h, arc, arc);
        // The left and right edges of the rectangle are at x and xÊ+Êwidth, respectively.
        // The top and bottom edges of the rectangle are at y and yÊ+Êheight.


        // center the text on the label
//        int scaledWidth = (int) (actualWidth * scalex);
        int textX = 0 + paddingLeft;
        int textY = actualAuthorHeight + paddingTop;// + paddingTop + borderWidth;

        // don't center the text, leave it left-justified
//        int textX = 0 + paddingLeft + borderWidth;// + kernelSize / 2;
//        int textY = 0 + paddingTop + borderWidth;

        // draw the shadow of the text
        g2d.setFont(authorFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(Color.gray);
        System.out.println("shadow x and y: " + textX + " " + textY);
        System.out.println("offsets: " + shadowOffsetX + " " + shadowOffsetY);
        System.out.println("desired heights, author subj: " + desiredAuthorHeight + " " + desiredSubjectHeight);
        g2d.drawString(author, textX + shadowOffsetX, textY + shadowOffsetY);


        // blur the shadows
        BufferedImage ret = blur.filter(tmp0, null);
        // draw the text over the shadow
        g2d = (Graphics2D) ret.getGraphics();
        g2d.setFont(authorFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(Color.black);
        System.out.println("the TEXT x and y: " + textX + " " + textY);
        g2d.drawString(author, textX, textY);


        // draw subject text
        // make same left-justification, but different y
        textY += actualSubjectHeight + paddingLine;

        g2d = (Graphics2D) ret.getGraphics();
        g2d.setFont(subjectFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.drawString(subject, textX, textY);

        // draw the message text
        textY += actualSubjectHeight + paddingLine;

        g2d = (Graphics2D) ret.getGraphics();
        g2d.setFont(subjectFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        for(TextLayout t:chunks){
          t.draw(g2d, textX, textY);
          System.out.println("t string:" + t.toString());
          textY += actualTextLineHeight + paddingLine;
        }

        return ret;
  }

  private Quad getQuad() {
      Vector2f scales = new Vector2f();
      BufferedImage img = getImage(scales);

      float w = img.getWidth();
      float h = img.getHeight();
      float factor = height / h;

      Quad ret = new Quad("textLabel2d", w * factor, h * factor);
      TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
      Texture tex = TextureManager.loadTexture(img, MinificationFilter.BilinearNoMipMaps, MagnificationFilter.Bilinear, true);

//        TexCoords texCo = ret.getTextureCoords(0);
//        texCo.coords = BufferUtils.createFloatBuffer(16);
//        texCo.coords.rewind();
//        for(int i=0; i < texCo.coords.limit(); i+=2){
//            float u = texCo.coords.get();
//            float v = texCo.coords.get();
//            texCo.coords.put(u*scales.x);
//            texCo.coords.put(v*scales.y);
//        }
//        ret.setTextureCoords(texCo);
//        ret.updateGeometricState(0, true);

//        tex.setScale(new Vector3f(scales.x, scales.y, 1));
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

      // set bounds to make pickable
//        ret.setModelBound(new BoundingBox());
//        ret.updateModelBound();

      this.quad = ret;
      return ret;
  }

  private BillboardNode getBillboard() {
      BillboardNode bb = new BillboardNode("bb");
      bb.attachChild(getQuad());
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

//    /**
//     * Make an entity pickable by attaching a collision component. Entity must already have
//     * a render component and a scene root node.
//     */
//    public static void entityMakePickable (Entity entity) {
//      JMECollisionSystem collisionSystem = (JMECollisionSystem) ClientContextJME.getWorldManager().
//          getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
//      RenderComponent rc = (RenderComponent) entity.getComponent(RenderComponent.class);
//      CollisionComponent cc = collisionSystem.createCollisionComponent(rc.getSceneRoot());
//      entity.addComponent(CollisionComponent.class, cc);
//    }

  /**
   * Attach a mouse listener to this entity.
   * @param ml the listener to attach
   */
  private void attachMouseListener(MouseListener ml) {
    ml.addToEntity(this);
    listeners.add(ml);
  }

//    Entity cellEntity =
//            ((App2DCellRendererJME)cell.getCellRenderer(Cell.RendererType.RENDERER_JME)).getEntity();
//
//    // Create entity and node
//        entity = new Entity("Entity for " + name);
//        viewNode = new Node("Node for " + name);
//        RenderComponent rc =
//            ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(viewNode);
//        entity.addComponent(RenderComponent.class, rc);
//        entityMakePickable(entity);
//
//        // Create input-related objects
//        gui = new Gui2DInterior(this);
//        gui.attachEventListeners(entity);



  /**
   * Converts the given 3D mouse event into a 2D event and forwards it along to the view's controlArb.
   * from view2d
   * @param window The window this view displays.
   * @param me3d The 3D mouse event to deliver.
   */
/** {@inheritDoc} */
//    public void deliverEvent(MouseEvent3D me3d) {
//        /*
//        System.err.println("********** me3d = " + me3d);
//        System.err.println("********** awt event = " + me3d.getAwtEvent());
//        PickDetails pickDetails = me3d.getPickDetails();
//        System.err.println("********** pt = " + pickDetails.getPosition());
//        */
//
//        // No special processing is needed for wheel events. Just
//        // send the 2D wheel event which is contained in the 3D event.
//        if (me3d instanceof MouseWheelEvent3D) {
//            MouseEvent me = (MouseEvent) me3d.getAwtEvent();
//            return;
//        }
//
//        // Can't convert if there is no geometry
//        if (geometryNode == null) {
//            return;
//        }
//
//        // Convert mouse event intersection point to 2D. For most events this is the intersection
//        // point based on the destination pick details calculated by the input system, but for drag
//        // events this needs to be derived from the actual hit pick details (because for drag events
//        // the destination pick details might be overridden by a grab).
//        Point point;
//        if (me3d.getID() == MouseEvent.MOUSE_DRAGGED) {
//            MouseDraggedEvent3D de3d = (MouseDraggedEvent3D) me3d;
//            point = geometryNode.calcPositionInPixelCoordinates(de3d.getHitIntersectionPointWorld(), true);
//        } else {
//            point = geometryNode.calcPositionInPixelCoordinates(me3d.getIntersectionPointWorld(), false);
//        }
//        if (point == null) {
//            // Event was outside our panel so do nothing
//            // This can happen for drag events
//            return;
//        }
//
//        // Construct a corresponding 2D event
//        MouseEvent me = (MouseEvent) me3d.getAwtEvent();
//        int id = me.getID();
//        long when = me.getWhen();
//        int modifiers = me.getModifiers();
//        int button = me.getButton();
//
//        // TODO: WORKAROUND FOR A WONDERLAND PICKER PROBLEM:
//        // See comment for pointerMoveSeen above
//        if (id == MouseEvent.MOUSE_RELEASED && pointerMoveSeen) {
//            point.x = pointerLastX;
//            point.y = pointerLastY;
//        }
//
//        me = new MouseEvent(dummyButton, id, when, modifiers, point.x, point.y,
//                0, false, button);
//
//        // Send event to the window's control arbiter
//        controlArb.deliverEvent(window, me);
//
//        // TODO: WORKAROUND FOR A WONDERLAND PICKER PROBLEM:
//        // See comment for pointerMoveSeen above
//        if (id == MouseEvent.MOUSE_MOVED || id == MouseEvent.MOUSE_DRAGGED) {
//            pointerMoveSeen = true;
//            pointerLastX = point.x;
//            pointerLastY = point.y;
//        }
//    }



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
        }
      }
    }
  }






  /**
   * Sets whether the annotation is visible (true) or invisible (false).
   *
   * @param visible True to make the affordance visible, false to not
   */
  public synchronized void setVisible(boolean visible) {
      // If we want to make the affordance visible and it already is not
      // visible, then make it visible.
      if (visible == true && isVisible == false) {
          RenderUpdater updater = new RenderUpdater() {
              public void update(Object arg0) {
                  AnnotationEntity ent = (AnnotationEntity)arg0;
                  Node rootNode = ent.getNode();
//                    logger.info("making pickable");
//                    makeEntityPickable(ent, rootNode);
                  ClientContextJME.getWorldManager().addEntity(ent);
                  ClientContextJME.getWorldManager().addToUpdateList(rootNode);
              }
          };
          WorldManager wm = ClientContextJME.getWorldManager();
          wm.addRenderUpdater(updater, this);
          isVisible = true;
          return;
      }

      // If we want to make the affordance invisible and it already is
      // visible, then make it invisible
      if (visible == false && isVisible == true) {
          RenderUpdater updater = new RenderUpdater() {
              public void update(Object arg0) {
                  AnnotationEntity ent = (AnnotationEntity)arg0;
                  ClientContextJME.getWorldManager().removeEntity(ent);
//                    logger.info("making non-pickable");
//                    ent.removeComponent(CollisionComponent.class);
              }
          };
          WorldManager wm = ClientContextJME.getWorldManager();
          wm.addRenderUpdater(updater, this);
          isVisible = false;
          return;
      }
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
      setVisible(false);
      for(MouseListener ml : listeners){
        ml.removeFromEntity(this);
        ml = null;
        listeners.clear();
      }
  }


  class CtxListener implements ContextMenuListener{

    public void contextMenuDisplayed(ContextEvent event) {
      logger.info("[ANNO COMPO] ctx menu displayed, primary entity:" + event.getPrimaryEntity() + " name is " + event.getPrimaryEntity().getName());
      for(Entity e: event.getEntityList()){
        logger.info("[ANNO COMPO] entity named " + e.getName());
      }


    }

  }


    

}
