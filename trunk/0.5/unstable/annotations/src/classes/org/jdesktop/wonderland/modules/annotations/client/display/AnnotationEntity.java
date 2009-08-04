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

import com.jme.math.Vector3f;
import com.jme.scene.Node;

import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
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
 * An Entity representing an Annotation. Creates an AnnotationNode for in-world
 * graphics.
 * @author mabonner
 */
public class AnnotationEntity extends Entity {

  /** the annotation this entity represents */
  private final Annotation anno;

  /** sets font size, given to annotation node */
  private float fontSizeModifier;




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

  private PanelConfig pc;

  DisplayMode displayMode = DisplayMode.HIDDEN;

  

  // node to display this annotation
  Node node;

  /** Whether this entity has been added to the JME manager */
  private boolean entityAdded = false;

  /** local translation of the entity */
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
   * @param fontSize initial fontSizeModifier
   */
  public AnnotationEntity(Annotation a, PanelConfig pc, Cell cell, DisplayMode mode, float fontSize) {
    super("annotation with id: " + a.getID());

    this.anno = a;
    this.pc = pc;
    this.fontSizeModifier = fontSize;

    logger.info("Annotation with id " + a.getID() + " author: " + a.getCreator() + " font size:" + fontSizeModifier);

    attachMouseListener(new MouseListener(this));


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

  /** get the ID of the annotation this entity represents */
  public MetadataID getAnnoID() {
    return anno.getID();
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
      node = new AnnotationNode(anno, displayMode, pc, fontSizeModifier);
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
   * like a new display mode or a new fontSizeModifier to take effect.
   */
  private void revalidateNode(){
    node = new AnnotationNode(anno, displayMode, pc, fontSizeModifier);
    logger.info("[ANNO ENT] reset node");
    // this is unnecessary but it can't hurt, it guarantees we are operating
    // on the nodes we think we are in the updater thread
    final Node newNode = node;

    if(localTranslation != null){
      node.setLocalTranslation(localTranslation);
      logger.info("resetting location " + localTranslation);
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
  }

}
