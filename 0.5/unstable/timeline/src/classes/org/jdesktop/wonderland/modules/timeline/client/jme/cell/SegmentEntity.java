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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.cell.Cell;

import org.jdesktop.wonderland.client.jme.CellRefComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;
import org.jdesktop.wonderland.modules.timeline.client.TimelineClientConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDateRange;


/**
 * An Entity representing a Segment. Creates as many SegmentMesh's as necessary
 * to represent the Segment.
 * @author mabonner
 */
public class SegmentEntity extends Entity {

  private static Logger logger = Logger.getLogger(SegmentEntity.class.getName());

  // node to display this segment
  Node node;

  // err on the side of making these too small
  /**
   * how many radians are in each mesh making up this segment
   * default - pi/18 = 10 degrees, get smoother from there as necessary
   */


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
  private Node dateLabel;
//  private Vector3f initialLocation;
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
   * @param config contains almost all parameters necessary for spiral generation
   * @param radsPerMesh radians per mesh, must be a factor of radians per segment
   */
  public SegmentEntity(Cell cell, TimelineClientConfiguration config, float rotation, Vector3f target, float radsPerMesh) {
    super("timeline segment");
//    if(target != null){
//      this.initialLocation = new Vector3f(target);
//    }
    logger.fine("creating segment entity");

    if(config.getRadsPerSegment()% radsPerMesh > 0){
      logger.severe("[SEG MESH] ERROR: rads per mesh (" + radsPerMesh +
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
    trapSmallBase = (float) Math.sqrt(2*lSq -2*lSq*Math.cos(radsPerMesh));

    // law of cosines to get trapHeight of trap bases
    lSq = (float) Math.pow(config.getOuterRadius(),2);
    trapLargeBase = (float) Math.sqrt(2*lSq -2*lSq*Math.cos(radsPerMesh));

    climbPerMesh = (radsPerMesh/((float)Math.PI * 2.0f)) * config.getPitch();


    change = (trapLargeBase - trapSmallBase) / 2.0f;
    trapHeight = (float) Math.sqrt(Math.pow(trapSide,2) - Math.pow(change,2));
    logger.fine("lsq" + lSq);
    logger.fine("small base, large base, height, change: " + trapSmallBase + " " + trapLargeBase + " " + trapHeight + " " + change);
    logger.fine("Mid on large base:" + ((0.0f - change) + (0.5f * trapLargeBase)));
    logger.fine("climb, climb per mesh: " + config.getPitch() + " " + climbPerMesh);
//    logger.info("initial, target loc " + this.initialLocation + " " + target);

    // build meshes
    Vector3f oldV1, newV0;
    oldV1 = target;
    for(int i = 0; i < config.getRadsPerSegment()/radsPerMesh; i++){
      if(oldV1 == null){
        logger.fine("old vert is null at start of mesh " + i);
      }
      logger.fine("[SEG ENT] building mesh " + i);
      logger.fine("[SEG ENT] rotation (deg) and target " + Math.toDegrees(rotation) + " " + target);
      SegmentMesh mesh = buildMesh(rotation);
      // translate so proper trap edges meet... want vert 0 on new
      // to touch vert 1 on old (see diagram in segment mesh)
      newV0 = new Vector3f(mesh.getVertex(0));
      logger.fine("[SEG ENT] BEFORE COMPAREold v1 " + oldV1 + " new v0 " + newV0);
      if(oldV1 != null){
        // null on the first mesh (don't need to translate first mesh)
        // where will the rotation put the new v1?
        newV0 = new Vector3f((float)Math.cos(radsPerMesh)*newV0.getX() - (float)Math.sin(radsPerMesh)*newV0.getZ(),
                0.0f,
                (float)Math.sin(radsPerMesh)*newV0.getX() + (float)Math.cos(radsPerMesh)*newV0.getZ());
        logger.fine("[SEG ENT] adjusted newv0 " + newV0);
        logger.fine("[SEG ENT] translation " + oldV1.subtract(newV0));
        float dx = oldV1.getX() - newV0.getX();
        float dy = oldV1.getY() - newV0.getY();
        float dz = oldV1.getZ() - newV0.getZ();
        logger.fine("[SEG ENT] my dz/dx " + dz + " " + dx);
        mesh.translatePoints(new Vector3f(dx, dy, dz));
        logger.fine("v0 is now: " + mesh.getVertex(0));
        logger.fine("v1 is now: " + mesh.getVertex(1));
      }
      else{
        logger.fine("[SENG ENT] first mesh and first entity, translate so that center of cell is in center of spiral.");
        mesh.translatePoints(new Vector3f(0.0f, 0.0f, config.getInnerRadius()));
//        this.initialLocation = new Vector3f(0.0f, 0.0f, config.getInnerRadius());
        logger.fine("[SEG ENT] in don't translate: old v1 " + oldV1 + " new v0 " + newV0);
        logger.fine("[SEG ENT] in don't translate: new v1 (getting set to old v1) " + mesh.getVertex(1));
      }
      meshes.add(mesh);
      logger.fine("[SEG ENT] after it all, old v1: " + oldV1);
      // save for next mesh, if there is one
      oldV1 = mesh.getVertex(1);
      oldV1 = new Vector3f((float)Math.cos(radsPerMesh)*oldV1.getX() - (float)Math.sin(radsPerMesh)*oldV1.getZ(),
                oldV1.getY(),
                (float)Math.sin(radsPerMesh)*oldV1.getX() + (float)Math.cos(radsPerMesh)*oldV1.getZ());

      logger.fine("[SEG ENT] after set old v1 " + oldV1 + " new v0 " + newV0);
      rotation += radsPerMesh;
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

    logger.fine("[SEG ENT] building mesh");
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

  public Node getDateLabel(){
    return dateLabel;
  }

  public void setDateLabel(Node label) {
    dateLabel = label;
  }

  public void createDateLabel(TimelineClientConfiguration config, int idx, float rotation, float climbPerSegment, Vector3f loc, float radsPerMesh){

//    Vector3f dateLoc = new Vector3f(0.0f, SegmentMesh.THICKNESS + (idx * climbPerSegment), config.getInnerRadius());
//    Vector3f dateLoc = initialLocation;
//    dateLoc.add(0.0f, SegmentMesh.THICKNESS, 0.0f);
    // fetch date

    if(loc == null){
      logger.info("loc was null!");
      loc = new Vector3f(0.0f, 0.0f, config.getInnerRadius());
    }

    float x, y, z, innerX, innerZ, outerX, outerZ;

    innerX = ((float) Math.sin(config.getRadsPerSegment()*idx) * config.getInnerRadius());
    outerX = ((float) Math.sin(config.getRadsPerSegment()*idx) * config.getOuterRadius());
    y = idx * climbPerSegment + (SegmentMesh.THICKNESS * 2);
    innerZ = ((float) Math.cos(config.getRadsPerSegment()*idx) * config.getInnerRadius());
    outerZ = ((float) Math.cos(config.getRadsPerSegment()*idx) * config.getOuterRadius());
    x = innerX + (outerX - innerX) * 0.5f;
    z = innerZ + (outerZ - innerZ) * 0.5f;
    loc = new Vector3f(x,y,z);


    float pct = (float)idx / (float)config.getNumSegments();
    TimelineDateRange range = new TimelineDateRange(config.getDateRange().getMinimum(),
                                                        config.getDateRange().getMaximum(),
                                                        config.getUnits().getCalendarUnit());
    // duplication from addcolletionpanel
    DateFormat df;
    switch (range.getUnits()) {
        case Calendar.HOUR:
            df = DateFormat.getTimeInstance();
            break;
        case Calendar.DAY_OF_YEAR:
            df = new SimpleDateFormat("dd MMM");
            break;
        case Calendar.MONTH:
            df = new SimpleDateFormat("MMM");
            break;
        case Calendar.YEAR:
            df = new SimpleDateFormat("yyyy");
            break;
        default:
            df = DateFormat.getInstance();
            break;
    }

    Node l = new TextLabel2D(df.format(range.getDate(pct)));
    l.setLocalTranslation(loc);
    // build rotation, rotate about y axis (unnecessary if billboarded)
    Quaternion q = new Quaternion(0, (float)Math.sin(rotation/2), 0, (float)Math.cos(rotation/2));
    // rotate and translate mesh into appropriate position on spiral
    l.setLocalRotation(q);

    logger.info("Date range min, max, pct:" + range.getMinimum() + " "  + range.getMaximum() + " " + pct);
    logger.info("Datefetched: " + range.getDate(pct).toString());
    logger.info("location for date:" + loc);
    setDateLabel(l);
  }

}
