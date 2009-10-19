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

import com.jme.math.Vector3f;
import com.jme.scene.Node;

import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;

import java.awt.Point;
import java.awt.event.MouseEvent;
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
import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.modules.annotations.client.AnnotationComponent;
import org.jdesktop.wonderland.modules.annotations.common.Annotation;
import org.jdesktop.wonderland.modules.metadata.common.MetadataID;


/**
 * An Entity representing an Annotation. Creates an AnnotationNode for in-world
 * graphics.
 * @author mabonner
 */
public class AnnotationEntity extends Entity {

  /** the annotation this entity represents */
  private Annotation anno;

  /** sets font size, given to annotation node */
  private float fontSizeModifier;

  /** set whether annotation is currently movable */
  private boolean moving = false;






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
      public String toString(){ return "Mini";}
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

  private AnnotationSettings annoSettings;

  DisplayMode displayMode = DisplayMode.HIDDEN;

  

  // node to display this annotation
  Node node;

  /** Whether this entity has been added to the JME manager */
  private boolean entityAdded = false;

  /** local translation of the entity during a drag, committed to annotation itself when drag completed */
  private Vector3f localTranslation = null;

  // mouse listeners on this annotation
  Set<MouseListener> listeners = new HashSet<MouseListener>();

  protected static ZBufferState zbuf = null;
  static {
      RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
      zbuf = (ZBufferState)rm.createRendererState(StateType.ZBuffer);
      zbuf.setEnabled(true);
      zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
  }



  public AnnotationSettings getAnnotationSettings() {
    return annoSettings;
  }

  /**
   * set new graphics for this annotation
   * causes node to revalidat/repaint itself
   * @param pc
   */
  public void setAnnotationSettings(AnnotationSettings as) {
    this.annoSettings = as;
    revalidateNode();
  }

  /**
   * Constructor
   * @param a the annotation to represent
   * @param cell cell to reference in CellRefComponent, for context menu
   * @param mode initial DisplayMode
   * @param fontSize initial fontSizeModifier
   */
  public AnnotationEntity(Annotation a, Cell cell, DisplayMode mode, float fontSize) {
    super("annotation with id: " + a.getID());

    this.anno = a;
    // create own copy, so the entity can be adjusted individually
    this.annoSettings = new AnnotationSettings(a.getSettings());
    this.fontSizeModifier = fontSize;

    logger.info("creating anno ent with id " + a.getID() + " author: " + a.getCreator() + " font size:" + fontSizeModifier + " location " + anno.getTranslation());

    attachMouseListener(new MouseListener(this));


    // add a cell ref component to this entity
    // we add this in order to use the global context menu system...
    // the context menu won't show up unless the fired contextevent has a
    // cell ref component.
    this.addComponent(CellRefComponent.class, new CellRefComponent(cell));

    // setDisplayMode - this will result in the entity
    // getting added to the world if necessary, also builds the correct
    // node (graphical representation of annotation/entity) 
    displayMode = DisplayMode.HIDDEN;
    setDisplayMode(mode);

    return;
  }



//  public void setLocalTranslation(Vector3f v){
//    logger.info("[anno ent] setting local trans to " + v);
//    localTranslation = v;
//    if(node != null){
//      node.setLocalTranslation(v);
//    }
//  }

  /** get the ID of the annotation this entity represents */
  public MetadataID getAnnoID() {
    return anno.getID();
  }

  public Annotation getAnnotation() {
    return anno;
  }

  /**
   * Returns the node used to display this annotation
   *
   */
  public Node getNode() {
      return node;
  }

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
    // dragging variables
    // The intersection point on the entity over which the button was pressed, in world coordinates.
    Vector3f dragStartWorld;

    // The screen coordinates of the button press event.
    Point dragStartScreen;
    Vector3f translationOnPress = null;
    // store true start location here, in order to cancel movement and to
    // allow feedback by moving annotation immediately
    Vector3f startLocation;

    // set true if drag begins
    boolean dragging = false;

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
      // collect events
      MouseEvent3D me3d = (MouseEvent3D) event;
      MouseEvent me = (MouseEvent) me3d.getAwtEvent();
      logger.info("got mouse event!");

      // click events - display context menu, cycle display mode
      if(me.getID() == MouseEvent.MOUSE_CLICKED){
        if(me.getButton() == MouseEvent.BUTTON3){
          logger.info("AN: right click!");
          // display the context menu
          // build an AnnotationContextEvent, fire it for the
          // context menu system to pick up
          InputManager inputManager = InputManager.inputManager();
          ArrayList<Entity> entities = new ArrayList<Entity>();
          entities.add(ent);
          inputManager.postEvent(new AnnotationContextEvent(entities, me));


        }
        else if(me.getButton() == MouseEvent.BUTTON1
                && me.isShiftDown()){
          logger.info("AN: shift-left click!");
          // cycle to next display mode
          cycleDisplayMode();
        }
      }
      // drag start events
      // this could be a drag start if the mouse button 1 was pressed, moving is
      // enabled
      else if(me.getID() == MouseEvent.MOUSE_PRESSED && moving &&
              me.getButton() == MouseEvent.BUTTON1){
        logger.info("began drag event");
        dragging = true;
        dragStartScreen = new Point(me.getX(), me.getY());
        dragStartWorld = me3d.getIntersectionPointWorld();
        startLocation = node.getLocalTranslation();
//        translationOnPress = transform.getTranslation(null);
      }

      // drag events
      else if(me.getID() == MouseEvent.MOUSE_DRAGGED && moving){
        logger.info("dragged, and moving");
        MouseDraggedEvent3D dragEvent = (MouseDraggedEvent3D) event;
        logger.info("ds world, dss:" + dragStartWorld + " " + dragStartScreen);
        Vector3f dragVector = dragEvent.getDragVectorWorld(dragStartWorld, dragStartScreen,
                new Vector3f());
        // Now add the drag vector the node translation and move the cell.
        logger.info("node local trans is:" + node.getLocalTranslation());
        logger.info("node world trans is:" + node.getWorldTranslation());
        logger.info("node drag vector is:" + dragVector);
//        Vector3f newTranslation = translationOnPress.add(dragVector);
        node.setLocalTranslation(startLocation.add(dragVector));
        localTranslation = startLocation.add(dragVector);

        makeEntityPickable(AnnotationEntity.this, node);
      }
      else if(me.getID() == MouseEvent.MOUSE_RELEASED && moving && dragging){
        logger.info("completed drag event");
        dragging = false;
        anno.setTranslation(localTranslation);
        logger.info("node local trans is:" + node.getLocalTranslation());
        logger.info("node world trans is:" + node.getWorldTranslation());
        logger.info("final location stored in anno is:" + localTranslation);
        revalidateNode();
        // commit the change to anno compo
        
        CellRefComponent cRef = AnnotationEntity.this.getComponent(CellRefComponent.class);
        AnnotationComponent annoCompo = cRef.getCell().getComponent(AnnotationComponent.class);
        annoCompo.modifyAnnotationInMetadata(anno);
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
   * Set the annotation this entity represents. Use this to update the current
   * annotation by passing the current annotation.
   *
   * Causes node to revalidate/repaint itself
   * @param a
   */
  public void setAnnotation(Annotation a){
    logger.info("set annotation, current location is " + anno.getTranslation());
    this.anno = a;
    logger.info("set annotation, new location is " + anno.getTranslation());

    // causes node to be revalidated
    setAnnotationSettings(a.getSettings());
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
    logger.info("[ANNO ENT] current location is: " + anno.getTranslation());
    // if node is currently HIDDEN and the new mode is not HIDDEN (is visible),
//     make the node visible in the world
    if (newDisplayMode != DisplayMode.HIDDEN) {
      logger.info("[ANNO ENT] will display");
      displayMode = newDisplayMode;
      logger.info("[ANNO ENT] display mode is now:" + displayMode);

      // refreshes the node to the new DisplayMode's version
      node = new AnnotationNode(anno, displayMode, annoSettings, fontSizeModifier);
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
          if(anno.getTranslation() != null){
            node.setLocalTranslation(anno.getTranslation());
            logger.info("resetting location " + anno.getTranslation());
          }
          else{
            logger.info("annotation's location was null");
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
   * Adjust annoation's font size
   *
   * @param newDisplayMode what style to display the node as
   */
  public synchronized void setFontSizeModifier(float newMod) {

    logger.info("[ANNO ENT] setting new font size mod: " + newMod);
    logger.info("[ANNO ENT] current display mode: " + fontSizeModifier);
    fontSizeModifier = newMod;
    revalidateNode();
  }

  /**
   * helper function, refreshes this Entity's Node. This will cause any changes
   * like a new display mode or a new fontSizeModifier to take effect. Also used
   * to re-calculate bounds, e.g. after node is moved.
   */
  private void revalidateNode(){
    node = new AnnotationNode(anno, displayMode, annoSettings, fontSizeModifier);
    logger.info("[ANNO ENT] revalidate node");
    // this is unnecessary but it can't hurt, it guarantees we are operating
    // on the nodes we think we are in the updater thread
    final Node newNode = node;

    if(anno.getTranslation() != null){
      node.setLocalTranslation(anno.getTranslation());
      logger.info("resetting location " + anno.getTranslation());
    }
    else{
      logger.info("location was null");
    }

    RenderUpdater updater = new RenderUpdater() {
      public void update(Object arg0) {
        AnnotationEntity ent = (AnnotationEntity)arg0;

        logger.info("[ANNO ENT] get render manager");
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        logger.info("[ANNO ENT] got manager");
        RenderComponent rComp = rm.createRenderComponent(newNode);
        logger.info("[ANNO ENT] made render compo");
        ent.removeComponent(RenderComponent.class);
        logger.info("[ANNO ENT] removed render compo");
        ent.addComponent(RenderComponent.class, rComp);
        logger.info("[ANNO ENT] swapped render compo");

        node.setRenderState(zbuf);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();

        makeEntityPickable(AnnotationEntity.this, newNode);

//        ClientContextJME.getWorldManager().removeEntity(ent);
//        entityAdded = false;

        logger.info("[ANNO ENT] adding entity");
//        ClientContextJME.getWorldManager().addEntity(ent);
        entityAdded = true;
        ClientContextJME.getWorldManager().addToUpdateList(newNode);
      }
    };



    WorldManager wm = ClientContextJME.getWorldManager();
    logger.info("[ANNO ENT] add render updater");
    wm.addRenderUpdater(updater, this);
    logger.info("[ANNO ENT] finished");
  }

  /**
   * Check the current display mode of this annotation entity
   */
  public DisplayMode getDisplayMode(){
    return displayMode;
  }

  /**
   * enable/disable relocating annoation via dragging
   */
  public void toggleMoving(){
    moving = !moving;
  }

  /**
   * Make this entity pickable by adding a collision component to it.
   */
  protected void makeEntityPickable(Entity entity, Node node) {
      JMECollisionSystem collisionSystem = (JMECollisionSystem)
              ClientContextJME.getWorldManager().getCollisionManager().
              loadCollisionSystem(JMECollisionSystem.class);
      entity.removeComponent(CollisionComponent.class);
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
      RenderUpdater updater = new RenderUpdater() {
        public void update(Object arg0) {
          AnnotationEntity ent = (AnnotationEntity)arg0;
          ClientContextJME.getWorldManager().removeEntity(ent);
        }
      };
  }

}
