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

package org.jdesktop.wonderland.modules.timeline.server.layout;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.modules.imageviewer.common.cell.ImageViewerCellServerState;
import org.jdesktop.wonderland.modules.imageviewer.server.cell.ImageViewerCellMO;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.TimelineSegment;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedImage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResult;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResultListener;
import org.jdesktop.wonderland.modules.timeline.server.TimelineCellMO;
import org.jdesktop.wonderland.modules.timeline.server.provider.TimelineProviderComponentMO;
import org.jdesktop.wonderland.modules.timeline.server.provider.TimelineProviderComponentMOListener;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;

/**
 *
 * @author drew
 */
public class BaseLayout implements TimelineProviderComponentMOListener, LayoutManager, TimelineResultListener, Serializable {

    private static final Logger logger =
        Logger.getLogger(BaseLayout.class.getName());

    private Map<TimelineSegment, DatedSet> datedObjectBySegment = new HashMap<TimelineSegment, DatedSet>();

    /**
     * Stores already created Cells, along with the DatedObject they were created
     * from. Used to avoid recreating Cells each time we regenerate a layout.
     */
    private Map<DatedObject, ManagedReference<CellMO>> datedObjectToCellMap = new HashMap<DatedObject, ManagedReference<CellMO>>();


    private Map<TimelineSegment, Set<DatedObject>> segmentToDatedObjects = new HashMap<TimelineSegment, Set<DatedObject>>();
    

    /**
     * The parent cell for this layout.
     */
    private ManagedReference<TimelineCellMO> cellRef;

    public BaseLayout(TimelineCellMO cellMO) {

        this.cellRef = AppContext.getDataManager().createReference(cellMO);
        
        // Set up the necessary listeners.
        TimelineProviderComponentMO providerComponent = cellMO.getComponent(TimelineProviderComponentMO.class);
        providerComponent.addComponentMOListener(this);

        // Grab an initial list of results, if they're there.
        Collection<TimelineResult> results = providerComponent.getResults();

        for(TimelineResult result : results) {
            result.addResultListener(this);
        }

        logger.info("got initial results: " + results);
        processResults(results);
        doLayout();
    }

    public void resultAdded(TimelineResult result) {
        logger.info("Got new result from provider: " + result);
//        result.addResultListener(this)
//        processResult(result);
//        doLayout();
    }

    public void resultRemoved(TimelineResult result) {
//        // This needs to be handled differently - write this later.
//        logger.info("Got a resultRemoved event that we're ignoring for now.");
    }


    public void added(DatedObject obj) {
        layoutDatedObject(obj);
        doLayout();
    }

    public void removed(DatedObject obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    private void processResults(Collection<TimelineResult> results) {
        for(TimelineResult result : results) {
            processResult(result);
        }
    }

    private void processResult(TimelineResult result) {

        // Given a result, get all the DatedObject it contains
        // and process them.

        for(DatedObject obj : result.getResultSet()) {
            // now with this object, do our little dance.
            layoutDatedObject(obj);
            
        }
    }

    private void layoutDatedObject(DatedObject obj) {
        // make a new Cell for this object (or get an existing one if we have one)
        CellMO cell = getNewCell(obj);

        logger.info("got a new cell for this DO: " + cell);
        // now assign this cell to a segment. get the middle date for the object
        // and look that date up against all our segments.
        logger.info("segments:" + this.cellRef.get().getSegments());
        logger.info("date: " + obj.getDate().getMiddle());

        DatedSet segments = this.cellRef.get().getSegments().containsSet(new TimelineDate(obj.getDate().getMiddle()));

        if(segments.size()!=1) {
            logger.warning("!!!!! Couldn't find a segment to assign this dated object to.");

            DatedObjectComponentMO comp = new DatedObjectComponentMO(cell);
            comp.setDatedObject(obj);
            comp.setAddedToTimeline(false);
            comp.setNeedsLayout(true);
            comp.setAssignedToSegment(false);
            cell.addComponent(comp);

        }
        
        // pick off the first one (which is guaranteed to be the only one, because
        // of the way we generate segments
        TimelineSegment seg = (TimelineSegment) segments.iterator().next();
        logger.info("Assigning current DO to this seg: " + seg);

        // now seg is the segment to which we're going to assign this new
        // cell. well do that by:
        //  1. Adding a DatedObjectCellComponent that associates the Cell with
        //     its DatedObject.
        //  2. Add it to the Segment -> DatedObject map. We'll come back to that map
        //     after we're done creating cells to do the actual layout work.

        // 1.
        DatedObjectComponentMO comp = new DatedObjectComponentMO(cell);
        comp.setDatedObject(obj);
        comp.setAddedToTimeline(false);
        comp.setNeedsLayout(true);
        comp.setAssignedToSegment(true);
        cell.addComponent(comp);

        logger.info("Added relevant cell components.");

        // 2.
        DatedSet currentObjects = datedObjectBySegment.get(seg);
        if(currentObjects==null)
            currentObjects = new DatedSet();

        currentObjects.add(obj);

        datedObjectBySegment.put(seg, currentObjects);
        logger.info("Added DO to list of DOs attached to this segment.");
    }

    public void doLayout() {
        doLayout(false);
    }

    @SuppressWarnings("empty-statement")
    public void doLayout(boolean force) {
        // Loop through all the dated objects by segment,
        // and check and see if their cells need either adding/layout or both.

        TimelineConfiguration config = cellRef.get().getConfiguration();

        logger.info("---------------- STARTING LAYOUT-----------");
        for(TimelineSegment seg : datedObjectBySegment.keySet()) {

            logger.info("Processing layout for: " + seg);

            DatedSet datedObjects = datedObjectBySegment.get(seg);

            boolean needsLayout = false;

            // fill in this set by looping through the datedObjects.
            Set<CellMO> cells = new HashSet<CellMO>();

            // In this first pass we're deciding if we need to do a full layout
            // on this segment, as well as populating a list of cells in this segment
            // to enable us to actually do that layout if we need to.
            
            for(DatedObject dObj : datedObjects.descendingSet()) {
                // we know all of these objects will have an entry
                // in this map by now.
                CellMO cell = datedObjectToCellMap.get(dObj).get();
                DatedObjectComponentMO comp = cell.getComponent(DatedObjectComponentMO.class);

                // Not sure I need this second test. I think isAddedToTimeline
                // implies isNeedsLayout, but not 100% sure. For now verge
                // on the side of more false positives.
                if(comp.isNeedsLayout() || !comp.isAddedToTimeline())
                    needsLayout = true;

                // Really should collapse these if statements at some point.
                if(!comp.isAssignedToSegment())
                    needsLayout = false;

                cells.add(cell);
            }

            logger.info("First pass completed. Built necessary lists. This segment needs layout: " + needsLayout + " (forcing? " + force + ")");
            // If we don't need to layout this segment, move on to the next
            // segment. 
            if(!needsLayout && !force)
                continue;

            // In this second pass, we know we need to run a layout.
            // So loop through each of the cells and set their position
            // appropriately. Add them to the TimelineCell if we need to.

            
            for(CellMO cell : cells) {
                DatedObjectComponentMO doComp = cell.getComponent(DatedObjectComponentMO.class);
                MovableComponentMO movComp = cell.getComponent(MovableComponentMO.class);

                // Put smarter layout logic here, but for now just throw the
                // images in the dead center of the segment.

                // Okay, here's the plan. First, lets just do one image per segment.
                // We're going to make a vector from 0,y,0 to the center segment,
                // normalize that vector and then scale it to outerradius.
                // that'll give us a vector to the edge.

                Vector3f segPos = seg.getTransform().getTranslation(null);
                segPos.y = 0;
                segPos = segPos.normalize();
                segPos = segPos.mult(config.getOuterRadius());
                segPos.y = seg.getTransform().getTranslation(null).y;

                // Move it up into the middle of the space in the spiral.
                segPos.y += config.getHeight() / config.getNumTurns() /2;


                float angleBetween = (float) Math.atan2(segPos.x, segPos.z);

                float[] angles = {0.0f, angleBetween, 0.0f};

                Quaternion q = new Quaternion(angles);


                logger.info("Setting position to: " + seg.getTransform() + " angle: " + angleBetween);


                movComp.moveRequest(null, new CellTransform(q, segPos));

                if(!doComp.isAddedToTimeline()) {
                    try {
                        this.cellRef.getForUpdate().addChild(cell);
                        logger.info("Added cell to the timeline: " + cell);
                        doComp.setNeedsLayout(false);
                        doComp.setAddedToTimeline(true);
                        
                    } catch (MultipleParentException ex) {
                        Logger.getLogger(BaseLayout.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            logger.info("++++Done with layout for segment: " + seg + "+++++++");

        }

        logger.info("-------------Done with complete layout pass----------------");
    }

    private CellMO getNewCell(DatedObject datedObj) {

        // Check and see if we've generated a cell for that object. If we have,
        // use it. (Are we guaranteed the uniqueness of DatedObjects in ResultSets?)

        if(datedObjectToCellMap.get(datedObj)!=null) {
            logger.info("=======Found Existing cell for this DO========");
            return datedObjectToCellMap.get(datedObj).get();
        }

        CellMO out = null;

        // If we don't have that cell yet, run through the list of 
        if(datedObj instanceof DatedImage) {

            DatedImage img = (DatedImage)datedObj;
            
            out = new ImageViewerCellMO();
            ImageViewerCellServerState state = new ImageViewerCellServerState();
            state.setImageURI(img.getImageURI());
            logger.info("IMAGE URI: " + img.getImageURI());
            out.setServerState(state);
        } else {
            logger.warning("Attempted to make a cell from dated object (" + datedObj + ") but it was an unknown type.");
            out = null;
        }

        // Keep the map up to date.
        if(out!=null) {
            // Add a movable component to this cell so it can be
            // positioned programmatically later. 
            out.addComponent(new MovableComponentMO(out));

            datedObjectToCellMap.put(datedObj, AppContext.getDataManager().createReference(out));
        }

        return out;
    }
}
