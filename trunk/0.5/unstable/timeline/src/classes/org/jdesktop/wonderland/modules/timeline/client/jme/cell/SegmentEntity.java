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


package org.jdesktop.wonderland.modules.timeline.client.jme.cell;

/**
 *
 * @author mabonner
 */


import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;

import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;

import java.util.ArrayList;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.cell.Cell;

import org.jdesktop.wonderland.client.jme.CellRefComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.timeline.client.TimelineClientConfiguration;


/**
 * An Entity representing a Segment. Creates as many SegmentMesh's as necessary
 * to represent the Segment.
 * @author mabonner
 */
public class SegmentEntity extends Entity {

  private static Logger logger = Logger.getLogger(SegmentEntity.class.getName());

  // node to display this segment
  Node node;

  // this could be configurable in the future. lower value = smoother curve
  /**
   * how many radians are in each mesh making up this segment
   * default - pi/18 = 10 degrees
   */
  public static final float RADS_PER_MESH = (float) (Math.PI / 18);

  protected static ZBufferState zbuf = null;
  static {
      RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
      zbuf = (ZBufferState)rm.createRendererState(StateType.ZBuffer);
      zbuf.setEnabled(true);
      zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
  }

  // sizing of segmentmesh trimeshes that make up this segment.
  private final float trapSide;
  private final float trapSmallBase;
  private final float trapLargeBase;
  private final float change;
  private final float trapHeight;
  private final Vector3f cellCenter;
  private ArrayList<Geometry> meshes = new ArrayList<Geometry>();
  private final float nextRotation;
  private Vector3f nextTarget;
  private final float climbPerMesh;
//  private final float nextHeight;



  /**
   * Convenience consructor, sets rotation and start height to 0.
   * @param a the annotation to represent
   * @param cell cell to reference in CellRefComponent, for context menu
   * @param degrees how far to spiral. Must be in increments of SegmentMesh.DEGREES_PER_MESH
   * @param climb height increase per 360 degrees. If you set this to 0, will not climb at all.
   * @param innerRad inner radius of spiral
   * @param outerRad outer radius of spiral
   * @param origin where to build this entity's mesh from
   * @param
   */
//  public SegmentEntity(Cell cell, int degrees, float climb, float innerRad, float outerRad){
//    this(cell, degrees, climb, innerRad, outerRad, 0.0f, null);
//  }

//  public float getNextHeight() {
//    return nextHeight;
//  }

  public float getNextRotation() {
    return nextRotation;
  }

  /**
   * @param a the annotation to represent
   * @param cell cell to reference in CellRefComponent, for context menu
   * @param degrees how far to spiral. Must be in increments of SegmentMesh.DEGREES_PER_MESH
   * @param climb height increase per 360 degrees. If you set this to 0, will not climb at all.
   * @param innerRad inner radius of spiral
   * @param outerRad outer radius of spiral
   * @param origin where to build this entity's mesh from
   * @param rotation
   * @param height
   */
  public SegmentEntity(Cell cell, TimelineClientConfiguration config, float rotation, Vector3f target) {
    super("timeline segment");
    
    logger.info("creating segment entity");

    if(config.getRadsPerSegment()% RADS_PER_MESH > 0){
      logger.severe("[SEG MESH] ERROR: rads per mesh (" + RADS_PER_MESH + 
              ") must be a factor of" + config.getRadsPerSegment());
    }

    if(cell == null){
      logger.severe("cell is null!");
    }
    cellCenter = cell.getWorldBounds().getCenter();

    // add a cell ref component to this entity
    this.addComponent(CellRefComponent.class, new CellRefComponent(cell));

    // configure segment mesh information
    trapSide = config.getOuterRadius() - config.getInnerRadius();

    // law of cosines to get trapHeight of trap bases
    float lSq = (float) Math.pow(config.getInnerRadius(),2);
    trapSmallBase = (float) Math.sqrt(2*lSq -2*lSq*Math.cos(RADS_PER_MESH));

    // law of cosines to get trapHeight of trap bases
    lSq = (float) Math.pow(config.getOuterRadius(),2);
    trapLargeBase = (float) Math.sqrt(2*lSq -2*lSq*Math.cos(RADS_PER_MESH));

    climbPerMesh = (RADS_PER_MESH/((float)Math.PI * 2.0f)) * config.getPitch();


    change = (trapLargeBase - trapSmallBase) / 2.0f;
    trapHeight = (float) Math.sqrt(Math.pow(trapSide,2) - Math.pow(change,2));
    logger.info("lsq" + lSq);
    logger.info("small base, large base, height, change: " + trapSmallBase + " " + trapLargeBase + " " + trapHeight + " " + change);
    logger.info("Mid on large base:" + ((0.0f - change) + (0.5f * trapLargeBase)));
    logger.info("climb, climb per mesh: " + config.getPitch() + " " + climbPerMesh);

    // build meshes
    Vector3f oldV1, newV0;
    oldV1 = target;
    for(int i = 0; i < config.getRadsPerSegment()/RADS_PER_MESH; i++){
      if(oldV1 == null){
        logger.info("old vert is null at start of mesh " + i);
      }
      logger.info("[SEG ENT] building mesh " + i);
      logger.info("[SEG ENT] rotation (deg) and target " + Math.toDegrees(rotation) + " " + target);
      SegmentMesh mesh = buildMesh(rotation);
      // translate so proper trap edges meet... want vert 0 on new
      // to touch vert 1 on old (see diagram in segment mesh)
      newV0 = new Vector3f(mesh.getVertex(0));
      logger.info("[SEG ENT] BEFORE COMPAREold v1 " + oldV1 + " new v0 " + newV0);
      if(oldV1 != null){
        // null on the first mesh (don't need to translate first mesh)
        // where will the rotation put the new v1?
        newV0 = new Vector3f((float)Math.cos(RADS_PER_MESH)*newV0.getX() - (float)Math.sin(RADS_PER_MESH)*newV0.getZ(),
                0.0f,
                (float)Math.sin(RADS_PER_MESH)*newV0.getX() + (float)Math.cos(RADS_PER_MESH)*newV0.getZ());
        logger.info("[SEG ENT] adjusted newv0 " + newV0);
        logger.info("[SEG ENT] translation " + oldV1.subtract(newV0));
        float dx = oldV1.getX() - newV0.getX();
        float dy = oldV1.getY() - newV0.getY();
        float dz = oldV1.getZ() - newV0.getZ();
        logger.info("[SEG ENT] my dz/dx " + dz + " " + dx);
        mesh.translatePoints(new Vector3f(dx, dy, dz));
        logger.info("v0 is now: " + mesh.getVertex(0));
        logger.info("v1 is now: " + mesh.getVertex(1));
      }
      else{
        logger.info("[SENG ENT] first mesh and first entity, translate so that center of cell is in center of spiral.");
        mesh.translatePoints(new Vector3f(0.0f, 0.0f, config.getInnerRadius()));
        logger.info("[SEG ENT] in don't translate: old v1 " + oldV1 + " new v0 " + newV0);
        logger.info("[SEG ENT] in don't translate: new v1 (getting set to old v1) " + mesh.getVertex(1));
      }
      meshes.add(mesh);
      logger.info("[SEG ENT] after it all, old v1: " + oldV1);
      // save for next mesh, if there is one
      oldV1 = mesh.getVertex(1);
      oldV1 = new Vector3f((float)Math.cos(RADS_PER_MESH)*oldV1.getX() - (float)Math.sin(RADS_PER_MESH)*oldV1.getZ(),
                oldV1.getY(),
                (float)Math.sin(RADS_PER_MESH)*oldV1.getX() + (float)Math.cos(RADS_PER_MESH)*oldV1.getZ());

      logger.info("[SEG ENT] after set old v1 " + oldV1 + " new v0 " + newV0);
      rotation += RADS_PER_MESH;
    }

    this.nextTarget = oldV1;
    this.nextRotation = rotation;
    
    return;
  }

  /**
   * Returns the node used to display this annotation
   *
   */
  public Node getNode() {
      return node;
  }

  public ArrayList<Geometry> getMeshes(){
    return meshes;
  }

  /**
   *
   * @param the rotation of the last created mesh. 0.0 for no rotation. in radians.
   * @param height the height of the bottom of the last mesh. 0.0 for no height.
   * @param adjustment how to translate vectors to match up correctly with previous mesh
   */
  private synchronized SegmentMesh buildMesh(float rotation) {

    logger.info("[SEG ENT] building mesh");
    SegmentMesh trap = new SegmentMesh("segmesh", trapSmallBase, trapLargeBase, trapHeight, change, climbPerMesh);

    // build rotation, rotate about y axis
    Quaternion q = new Quaternion(0, (float)Math.sin(rotation/2), 0, (float)Math.cos(rotation/2));
    // rotate and translate mesh into appropriate position on spiral
    trap.setLocalRotation(q);

    return trap;
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

  // TODO Deron suggested attaching entities to the cell, that should cause them
  // to be automatically removed from the world.
  // Would still need to zero out listeners if there were any
  /**
   * Clean up any references before garbage collection
   */
  public void dispose() {
      RenderUpdater updater = new RenderUpdater() {
        public void update(Object arg0) {
          SegmentEntity ent = (SegmentEntity)arg0;
          ClientContextJME.getWorldManager().removeEntity(ent);
        }
      };
  }


  public Vector3f getNextTarget() {
    return this.nextTarget;
  }

}
