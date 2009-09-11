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

package org.jdesktop.wonderland.modules.timeline.client.jme.cell;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.timeline.client.TimelineClientConfiguration;

/**
 *
 *
 */
public class TimelineCellRenderer extends BasicRenderer {

  // 10 degrees
  public static final float baseRadsPerMesh = (float) (Math.PI / 18);

private static Logger logger = Logger.getLogger(TimelineCellRenderer.class.getName());
    private ArrayList<SegmentEntity> segments = new ArrayList<SegmentEntity>();

    public TimelineCellRenderer(Cell cell) {
        super(cell);
    }

    @Override
    protected Node createSceneGraph(Entity entity) {
      Node root = new Node();

      logger.fine("[TIME REND] creating scene graph, have " + segments.size() + " segments");

      return root;
    }

    private void update() {
      logger.fine("[TIME REND] update");
      if(sceneRoot == null){
        sceneRoot = new Node();
      }
       ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
           public void update(Object arg0) {
               synchronized(sceneRoot) {
                 sceneRoot.detachAllChildren();
                 int segCount = 0; int meshCount = 0;
                 for(SegmentEntity s: segments){
                   logger.fine("[TIME REND] attaching segment " + segCount++);
                   for(Geometry mesh:s.getMeshes()){
                     logger.fine("[TIME REND] attaching mesh " + meshCount++);
                     sceneRoot.attachChild(mesh);
                   }
                   meshCount = 0;
                   sceneRoot.attachChild(s.getDateLabel());
                 }
                 sceneRoot.setModelBound(new BoundingSphere());
                 sceneRoot.updateModelBound();
                 ClientContextJME.getWorldManager().addToUpdateList(rootNode);
               }
           }
       }, sceneRoot);
     }

  // TODO matt
  // segments getting generated automatically now
  // make connection between TimelineSegment and this function tomorrow
  // probably replacing these temporary functions
  public void buildSegments(TimelineClientConfiguration config) {
    segments.clear();
    float rotation = 0.0f;
    Vector3f nextTarget = null;
    logger.info("Build segments; num segments in config is:" + config.getNumSegments() + " start/end/units date in config are: " +
            config.getDateRange().getMinimum() + " " + config.getDateRange().getMaximum() + " " + config.getUnits());
    // ceiling = increase the precision as necessary, don't decrease it
    float radsPerMesh = (config.getRadsPerSegment()/((float)Math.ceil(config.getRadsPerSegment() / baseRadsPerMesh)));
    float climbPerSegment = (config.getRadsPerSegment()/((float)Math.PI * 2.0f)) * config.getPitch();
    logger.fine("[TIME CELL] climb per segment: " + climbPerSegment);

//    float radsPerMesh = (config.getRadsPerSegment()/((float)Math.ceil(config.getRadsPerSegment() % baseRadsPerMesh)));
    logger.fine("[TIME REND] rads/seg: " + config.getRadsPerSegment() + " base rads: " + baseRadsPerMesh + " mod: " + config.getRadsPerSegment() % baseRadsPerMesh
            + " ceil: " + ((float)Math.ceil(config.getRadsPerSegment() % baseRadsPerMesh)) + " rads/mesh: " + radsPerMesh);
    for(int i = 0; i < config.getNumSegments(); i++){
      logger.fine("[TIME CELL] building seg entity " + i);
      // cell, distance in degrees, climb, inner, outer
      SegmentEntity ent = new SegmentEntity(cell, config, rotation, nextTarget, radsPerMesh);
      segments.add(ent);
      // create date label before updating rotation
//      Node label = createDateLabel(config, i, rotation, climbPerSegment);
      ent.createDateLabel(config, i, rotation, climbPerSegment, nextTarget, radsPerMesh);

      // prepare for next iteration
      rotation = ent.getNextRotation();
      nextTarget = ent.getNextTarget();
      logger.fine("[TIME CELL] AFTER building seg entity, target is " + nextTarget);
    }
    update();
  }

}
