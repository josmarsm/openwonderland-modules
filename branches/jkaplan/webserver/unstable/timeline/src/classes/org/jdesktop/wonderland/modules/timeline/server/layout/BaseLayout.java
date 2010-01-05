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
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.ModelCellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.ViewComponentServerState;
import org.jdesktop.wonderland.modules.imageviewer.common.cell.ImageViewerCellServerState;
import org.jdesktop.wonderland.modules.imageviewer.server.cell.ImageViewerCellMO;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellComponentServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteTypes;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.server.cell.StickyNoteCellMO;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.TimelineSegment;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedAudio;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedImage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedModel;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedNews;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResult;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineResultListener;
import org.jdesktop.wonderland.modules.timeline.common.sticky.TimelineStickyServerState;
import org.jdesktop.wonderland.modules.timeline.server.TimelineCellMO;
import org.jdesktop.wonderland.modules.timeline.server.audio.TimelineAudioComponentMO;
import org.jdesktop.wonderland.modules.timeline.server.provider.TimelineProviderComponentMO;
import org.jdesktop.wonderland.modules.timeline.server.provider.TimelineProviderComponentMOListener;
import org.jdesktop.wonderland.modules.timeline.server.sticky.TimelineStickyCellMO;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ModelCellMO;
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

        for (TimelineResult result : results) {
            result.addResultListener(this);
        }

        processResults(results);
        doLayout();
    }

    public void resultAdded(TimelineResult result) {
        logger.info("Got new result from provider: " + result);
        result.addResultListener(this);
        processResult(result);
        doLayout();
    }

    public void resultRemoved(TimelineResult result) {
//        // This needs to be handled differently - write this later.
//        logger.info("Got a resultRemoved event that we're ignoring for now.");
    }

    public void added(DatedObject obj) {
        System.out.println("Added object: " + obj);

        layoutDatedObject(obj);
        doLayout();
    }

    public void removed(DatedObject obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void processResults(Collection<TimelineResult> results) {
        for (TimelineResult result : results) {
            processResult(result);
        }
    }

    private void processResult(TimelineResult result) {

        // Given a result, get all the DatedObject it contains
        // and process them.

        for (DatedObject obj : result.getResultSet()) {
            // now with this object, do our little dance.
            layoutDatedObject(obj);

        }
    }

    private void layoutDatedObject(DatedObject obj) {
        // make a new Cell for this object (or get an existing one if we have one)
        CellMO cell = getNewCell(obj);
        // have an exception for dated audio, which will return a null cell above.
        // this is a terrible way to structure this, but it's a hack that should
        // work okay for now.
        if (cell == null) {
            logger.warning("Type " + obj.getClass() + " not supported.");
            return;
        }

        // We don't need to layout DatedAudios.
        if(obj instanceof DatedAudio) {
            return;
        }

        System.out.println("Adding cell of type " + cell);

//        logger.info("got a new cell for this DO: " + cell);
        // now assign this cell to a segment. get the middle date for the object
        // and look that date up against all our segments.
//        logger.info("segments:" + this.cellRef.get().getSegments());
//        logger.info("date: " + obj.getDate().getMiddle());

        DatedSet segments = this.cellRef.get().getSegments().containsSet(new TimelineDate(obj.getDate().getMiddle()));

        if (segments.size() != 1) {
//            logger.warning("!!!!! Couldn't find a segment to assign this dated object to.");

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
//        logger.info("Assigning current DO to this seg: " + seg);

        // now seg is the segment to which we're going to assign this new
        // cell. well do that by:
        //  1. Adding a DatedObjectCellComponent that associates the Cell with
        //     its DatedObject.
        //  2. Add it to the Segment -> DatedObject map. We'll come back to that map
        //     after we're done creating cells to do the actual layout work.

        // 1.

        if(obj instanceof DatedAudio) {
            // do some special stuff.
            DatedAudio audioObj = (DatedAudio) obj;
//            TimelineAudioComponentMO audio = this.cellRef.getForUpdate().getComponent(TimelineAudioComponentMO.class);
            TimelineAudioComponentMO audio = this.cellRef.get().getAudio();

            logger.info("audioComponent: " + audio);
            logger.info("about to setup audio treatment with uri: " + audioObj.getAudioURI() + " on segment with date range: " + seg.getDate());
            audio.setupTreatment(seg.getDate().toString(), audioObj.getAudioURI(), Vector3f.ZERO);

//            comp.setNeedsLayout(false);
//            comp.setAddedToTimeline(true);
//            comp.setAssignedToSegment(true);
        } else {

            // Do the normal flow for dated objects that need a real
            // spatial layout.
            DatedObjectComponentMO comp = new DatedObjectComponentMO(cell);
            comp.setDatedObject(obj);

            comp.setAddedToTimeline(false);
            comp.setNeedsLayout(true);
            comp.setAssignedToSegment(true);
            cell.addComponent(comp);
        }

//        logger.info("Added relevant cell components.");

        System.out.println("Getting object for segment " + seg.toString());

        // 2.
        DatedSet currentObjects = datedObjectBySegment.get(seg);
        if (currentObjects == null) {
            System.out.println("Creating new set for segment");
            currentObjects = new DatedSet();
            datedObjectBySegment.put(seg, currentObjects);
        }

        System.out.println("Adding object size " + currentObjects.size());
        currentObjects.add(obj);
        System.out.println("objects for segment has " + currentObjects.size());
        

//        logger.info("Added DO to list of DOs attached to this segment.");
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
        for (TimelineSegment seg : datedObjectBySegment.keySet()) {

//            logger.info("Processing layout for: " + seg);

            DatedSet datedObjects = datedObjectBySegment.get(seg);

            boolean needsLayout = false;

            // fill in this set by looping through the datedObjects.
            Set<CellMO> cells = new HashSet<CellMO>();

            // In this first pass we're deciding if we need to do a full layout
            // on this segment, as well as populating a list of cells in this segment
            // to enable us to actually do that layout if we need to.

            for (DatedObject dObj : datedObjects.descendingList()) {
                // we know all of these objects will have an entry
                // in this map by now.
                if(dObj instanceof DatedAudio) {
                    continue;
                }
                CellMO cell = datedObjectToCellMap.get(dObj).get();
                DatedObjectComponentMO comp = cell.getComponent(DatedObjectComponentMO.class);

                if (dObj instanceof DatedImage) {
                    DatedImage dImg = (DatedImage) dObj;
//                    logger.info("Setting WIDTH AND HEIGHT: " + dImg.getWidth() + "-" + dImg.getHeight());

                    comp.setWidth(dImg.getWidth());
                    comp.setHeight(dImg.getHeight());

//                    logger.info("Getting the stuff we just set: " + comp.getWidth() + "-" + comp.getHeight());
                }


                // Not sure I need this second test. I think isAddedToTimeline
                // implies isNeedsLayout, but not 100% sure. For now verge
                // on the side of more false positives.
                if (comp.isNeedsLayout() || !comp.isAddedToTimeline()) {
                    needsLayout = true;
                }

                // Really should collapse these if statements at some point.
                if (!comp.isAssignedToSegment()) {
                    needsLayout = false;
                }

                cells.add(cell);
            }

//            logger.info("First pass completed. Built necessary lists. This segment needs layout: " + needsLayout + " (forcing? " + force + ")");
            // If we don't need to layout this segment, move on to the next
            // segment. 
            if (!needsLayout && !force) {
                continue;
            }

            // In this second pass, we know we need to run a layout.
            // So loop through each of the cells and set their position
            // appropriately. Add them to the TimelineCell if we need to.

            int numCellsInSegment = cells.size();
            int curCellIndex = 0;
            float curAngle = seg.getStartAngle();


            // Figure out how many rows/columns we're going to have.
            // we know how many items we need to lay out, and lets have a min
            // size.

            /**
             * Smallest we'll let a cell get. Determines how many cells we can
             * fit on a row.
             */
            float MIN_CELL_WIDTH = 3.0f;

            // first, decide how many cells we can fit within our arc length.
            float currentSegmentArcLength = (seg.getEndAngle() - seg.getStartAngle()) * config.getOuterRadius();
//            logger.info("currentArcLength: " + currentSegmentArcLength);

            int maxCellsPerRow = (int) Math.floor(currentSegmentArcLength / MIN_CELL_WIDTH);

            // calculate the angle increment based on how many columns we think we can
            // fit in. 
            float angleIncrement = (seg.getEndAngle() - seg.getStartAngle()) / maxCellsPerRow;

            int numRows = (int) Math.ceil(numCellsInSegment / (float) maxCellsPerRow);

            float heightIncrement = config.getPitch() / numRows;

            int row = 0;
            int col = 0;

            // Start out at half the heightIncrement, so the first cell starts
            // a little off the ground. Then we'll add on a full height increment
            // to find the center of each other cell.
            // this really shouldn't be set manually to be this value, but it's what makes things
            // look good for this pitch, sooooooo.
            float curHeight = -1.2f;


//            logger.warning("laying out segment. cells/row: " + maxCellsPerRow + "; numRows: " + numRows + "; angleIncrement: " + angleIncrement + "; heightIncrement: " + heightIncrement);

            // For each cell in this segment...
            for (CellMO cell : cells) {
//                logger.info("\t " + row + " - " + col);
                DatedObjectComponentMO doComp = cell.getComponent(DatedObjectComponentMO.class);
                MovableComponentMO movComp = cell.getComponent(MovableComponentMO.class);

                // New plan. Split the segment's arc into numCellsInSegment pieces.
                // For each angle, place a cell just outside the spiral at that
                // angle.

                // TODO make this properly height dependent.



                // we need a meters of height / PI relationship, which we get from the pitch

                float heightAtThisAngle = (float) ((config.getPitch() / (2 * Math.PI)) * curAngle);

                Vector3f edgePoint = new Vector3f((float) (config.getOuterRadius() * Math.sin(curAngle)), heightAtThisAngle + curHeight, ((float) (config.getOuterRadius() * Math.cos(curAngle))));
                edgePoint.y += config.getHeight() / config.getNumTurns() / 2;

                // Okay, here's the plan. First, lets just do one image per segment.
                // We're going to make a vector from 0,y,0 to the center segment,
                // normalize that vector and then scale it to outerradius.
                // that'll give us a vector to the edge.

//                Vector3f segPos = seg.getTransform().getTranslation(null);
//                segPos.y = 0;
//                segPos = segPos.normalize();
//                segPos = segPos.mult(config.getOuterRadius());
//                segPos.y = seg.getTransform().getTranslation(null).y;
//
//                // Move it up into the middle of the space in the spiral.
//                segPos.y += config.getHeight() / config.getNumTurns() / 2;

                float angleBetween = (float) Math.atan2(edgePoint.x, edgePoint.z);

                float[] angles = {0.0f, angleBetween+(float)Math.PI, 0.0f};

                Quaternion q = new Quaternion(angles);

//                logger.info("Setting position to: " + seg.getTransform() + " angle: " + angleBetween);

                CellTransform transform = new CellTransform(q, edgePoint);

                // now we need to deal with size. we know what the maximum dimension
                // for any object is.
                float maxWidth = MIN_CELL_WIDTH;
                float maxHeight = heightIncrement;

                // depending on the object, we need to handle this different.
                // either objects can tell us what size they're going to be
                // and we can set the scale with movable component, or
                // they can accept a maxWidth/maxHeight and then size themselves
                // appropriately.
                if (cell instanceof ImageViewerCellMO) {
//                    // for image cells, we can get their dimsensions and size them
//                    // appropriately.
//                    ImageViewerCellMO imageCell = (ImageViewerCellMO) cell;
//
//                    DatedObjectComponentMO dObjComp = imageCell.getComponent(DatedObjectComponentMO.class);
//
//                    // Figure out how to scale things to fit them in their
//                    // designated spot. Either scale up or down, according to
//                    // the object's size relative to the target.
//                    float scaleFactor;
//
//                    if(dObjComp.getWidth() > dObjComp.getHeight()) {
//                        // if width is the trouble dimension, figure
//                        // how much we need to scale to fit it in.
//
//                        scaleFactor = maxWidth / (float)dObjComp.getWidth();
//                    } else {
//                        scaleFactor = maxHeight / (float)dObjComp.getHeight();
//                    }
//
//                    logger.info("(" + dObjComp.getWidth() + "-" + dObjComp.getHeight() + ")" + "Setting scaling: " + scaleFactor);
//
//                    transform.setScaling(scaleFactor);
                    transform.setScaling(0.6f);
                }


//
                logger.info("Setting position: " + transform);
                movComp.moveRequest(null, transform);

                // increment the column.
                col++;
                curAngle += angleIncrement;

                // if we've done as many cells in this row as we think we can
                // fit, move up to the next row.
                if (col == maxCellsPerRow) {
                    col = 0;

                    row++;
                    curHeight += heightIncrement;
                    curAngle = seg.getStartAngle();
                }



                if (!doComp.isAddedToTimeline()) {
                    try {

                        if(cell instanceof StickyNoteCellMO) {
                            // We have to handle StickyNotes differently. AppBase
                            // cells don't work properly when children of other
                            // cells. Their movable components fail for some reason.
                            // So, convert the cell-local transform we wanted
                            // to use into a World transform, and add it directly
                            // to the world.
                            CellTransform localToWorld = this.cellRef.get().getWorldTransform(null);
                            localToWorld.invert();

                            CellTransform worldTransform = transform.mul(localToWorld);

                            PositionComponentServerState pos = new PositionComponentServerState();
                            pos.setTranslation(worldTransform.getTranslation(null));
                            pos.setRotation(worldTransform.getRotation(null));
//                            pos.setTranslation(transform.getTranslation(null));
//                            pos.setRotation(transform.getRotation(null));

                            CellServerState state = cell.getServerState(null);
                            state.addComponentServerState(pos);

                            cell.setServerState(state);

                            doComp.setNeedsLayout(false);
                            doComp.setAddedToTimeline(true);

                            WonderlandContext.getCellManager().insertCellInWorld(cell);

                        } else {
                            this.cellRef.getForUpdate().addChild(cell);
    //                        logger.info("Added cell to the timeline: " + cell);
                            doComp.setNeedsLayout(false);
                            doComp.setAddedToTimeline(true);
                        }

                    } catch (MultipleParentException ex) {
                        Logger.getLogger(BaseLayout.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

//            logger.info("++++Done with layout for segment: " + seg + "+++++++");

        }

        logger.info("-------------Done with complete layout pass----------------");
    }

    private CellMO getNewCell(DatedObject datedObj) {

        // Check and see if we've generated a cell for that object. If we have,
        // use it. (Are we guaranteed the uniqueness of DatedObjects in ResultSets?)

        if (datedObjectToCellMap.get(datedObj) != null) {
//            logger.info("=======Found Existing cell for this DO========");
            return datedObjectToCellMap.get(datedObj).get();
        }

        CellMO out = null;

        // If we don't have that cell yet, run through the list of 
        if (datedObj instanceof DatedImage) {

            DatedImage img = (DatedImage) datedObj;

            out = new ImageViewerCellMO();
            ImageViewerCellServerState state = new ImageViewerCellServerState();
            state.setImageURI(img.getImageURI());

//            logger.info("IMAGE URI: " + img.getImageURI());
            out.setServerState(state);
        } else if (datedObj instanceof DatedNews) {
            DatedNews news = (DatedNews) datedObj;

            TimelineStickyServerState state = new TimelineStickyServerState();
            state.setNoteText(news.getText());
            state.setNoteType(StickyNoteTypes.GENERIC);

            ViewComponentServerState vccs = new ViewComponentServerState(new CellTransform(new Quaternion(), Vector3f.ZERO));
            state.addComponentServerState(vccs);

            out = (CellMO) new TimelineStickyCellMO();
            out.setServerState(state);
        } else if (datedObj instanceof DatedAudio) {

            // We're not going to make cells here, instead
            // we're going to send messages to the Audio
            // subsystem about which segment this audio
            // file is associated with.
            DatedAudio audio = (DatedAudio)datedObj;


//            logger.warning("Not handling audio objects properly yet. Need to refactor some stuff for them to work.");

            out = null;


        } else if (datedObj instanceof DatedModel) {

            DatedModel model = (DatedModel) datedObj;

            ModelCellMO modelCell = new ModelCellMO();

            // This server state is pretty much empty - the important
            // model parts live in the model component state.
            ModelCellServerState state = new ModelCellServerState();

            JmeColladaCellComponentServerState modelState = new JmeColladaCellComponentServerState();
            modelState.model = model.getModelURI();
            modelState.modelTranslation = Vector3f.ZERO;
            modelState.modelRotation = new Quaternion();
            modelState.modelScale = Vector3f.UNIT_XYZ;
            modelState.modelAuthor = "timeline";
            modelState.modelGroup = "group";
            modelState.setModelLoaderClassname("org.jdesktop.wonderland.modules.kmzloader.client.KmzLoader");

            state.addComponentServerState(modelState);

            modelCell.setServerState(state);
            
            out = modelCell;
           
        } else {
            logger.warning("Attempted to make a cell from dated object (" + datedObj + ") but it was an unknown type.");
            out = null;
        }

        // Keep the map up to date.
        if (out != null) {
            // Add a movable component to this cell so it can be
            // positioned programmatically later. 
            out.addComponent(new MovableComponentMO(out));

            datedObjectToCellMap.put(datedObj, AppContext.getDataManager().createReference(out));
        }

        return out;
    }
}
