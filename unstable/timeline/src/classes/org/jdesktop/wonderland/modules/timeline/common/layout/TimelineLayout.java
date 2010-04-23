/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.timeline.common.layout;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.layout.api.common.AbstractLayout;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutParticipant;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.TimelineSegment;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;

/**
 * Layout data in a spiral along the timeline, based on dividing the timeline
 * into fixed size segments, and laying data out as a grid in those segments.
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class TimelineLayout extends AbstractLayout implements Serializable {
    private static final Logger logger =
            Logger.getLogger(TimelineLayout.class.getName());
    private static final float MIN_CELL_WIDTH = 3.0f;

    private TimelineConfiguration config;
    private DatedSet configSegments;
    private TimelineLayoutStorage storage;

    public TimelineLayout(TimelineConfiguration config, TimelineLayoutStorage storage) {
        this.config = config;
        this.configSegments = config.generateSegments();
        this.storage = storage;
    }

    @Override
    public int addParticipant(LayoutParticipant lp) {
        int index = super.addParticipant(lp);

        logger.warning("Add participant: " + lp);

        // first, make sure the participant has an associated date.  Otherwise
        // just ignore it
        if (!(lp instanceof DatedLayoutParticipant)) {
            return index;
        }

        // find the segment this participant belongs in
        DatedLayoutParticipant dlp = (DatedLayoutParticipant) lp;
        TimelineSegment segment = findSegment(dlp.getDate());
        if (segment == null) {
            logger.warning("No segment for " + dlp.getDate());
            return index;
        }

        // now add the participant to the segment, and force it to relayout
        DatedSet<DatedLayoutParticipant> participants = storage.getSegmentMap().get(segment);
        if (participants == null) {
            participants = new DatedSet<DatedLayoutParticipant>();
            storage.getSegmentMap().put(segment, participants);
        }
        participants.add(dlp);

        logger.warning("Added " + dlp + " to segment " + segment);

        layoutSegment(segment, participants);

        return index;
    }

    @Override
    public void removeParticipant(LayoutParticipant lp) {
        super.removeParticipant(lp);

        // first, make sure the participant has an associated date.  Otherwise
        // just ignore it
        if (!(lp instanceof DatedLayoutParticipant)) {
            return;
        }

        // find the segment this participant belongs in
        DatedLayoutParticipant dlp = (DatedLayoutParticipant) lp;
        TimelineSegment segment = findSegment(dlp.getDate());
        if (segment == null) {
            logger.warning("No segment for " + dlp.getDate());
            return;
        }

        // now add the participant to the segment, and force it to relayout
        DatedSet<DatedLayoutParticipant> participants = storage.getSegmentMap().get(segment);
        if (participants != null) {
            participants.remove(dlp);
            layoutSegment(segment, participants);
        }
    }

    /**
     * Get the segment a particular date is assigned to
     * @param date the date to find a segment for
     * @return the segment the given date will be assigned to
     */
    protected TimelineSegment findSegment(TimelineDate date) {
        // find the segment that contains the mid-point of this date
        DatedSet segments = configSegments.containsSet(
                new TimelineDate(date.getMiddle()));

        // if no segment contains the date, return null (?)
        if (segments.isEmpty()) {
            return null;
        }

        // take the first result (due to the way segments are set up, there
        // should only be one)
        return (TimelineSegment) segments.iterator().next();
    }

    protected void layoutSegment(TimelineSegment segment,
                                 DatedSet<DatedLayoutParticipant> participants)
    {
        logger.warning("Relayout " + segment);

        // step 1. go through the list of cells, and figure out how many of
        // them have non-zero sizes (so will participate in the layout)
        int layoutCount = 0;
        for (DatedLayoutParticipant participant : participants) {
            if (participant.getSize() != null) {
                layoutCount++;
            }
        }

        logger.warning("For segment " + segment + ", " + layoutCount +
                       " participants of " + participants.size() +
                       " total.");

        // Figure out how many rows/columns we're going to have.
        // we know how many items we need to lay out, and lets have a min
        // size.

        // first, decide how many cells we can fit within our arc length.
        float arcAngle = segment.getEndAngle() - segment.getStartAngle();
        float currentSegmentArcLength = arcAngle * config.getOuterRadius();

        int maxCellsPerRow = (int) Math.floor(currentSegmentArcLength / MIN_CELL_WIDTH);

        // calculate the angle increment based on how many columns we think we can
        // fit in.
        float angleIncrement = arcAngle / maxCellsPerRow;
        int numRows = (int) Math.ceil(layoutCount / (float) maxCellsPerRow);
        float heightIncrement = config.getPitch() / numRows;

        // now iterate through each record and calculate a position
        int row = 0;
        int col = 0;

        float curAngle = segment.getStartAngle();

        // Start out at half the heightIncrement, so the first cell starts
        // a little off the ground. Then we'll add on a full height increment
        // to find the center of each other cell.
        // this really shouldn't be set manually to be this value, but it's what makes things
        // look good for this pitch, sooooooo.
        float curHeight = -1f;

        // For each cell in this segment...
        for (DatedLayoutParticipant lp : participants) {
            // New plan. Split the segment's arc into numCellsInSegment pieces.
            // For each angle, place a cell just outside the spiral at that
            // angle.

            // TODO make this properly height dependent.

            // we need a meters of height / PI relationship, which we get from the pitch
            float heightAtThisAngle = (float) ((config.getPitch() / (2 * Math.PI)) * curAngle);

            Vector3f edgePoint = new Vector3f((float) (config.getOuterRadius() * Math.sin(curAngle)),
                                              heightAtThisAngle + curHeight,
                                              ((float) (config.getOuterRadius() * Math.cos(curAngle))));
            edgePoint.y += config.getHeight() / config.getNumTurns() / 2;

            float angleBetween = (float) Math.atan2(edgePoint.x, edgePoint.z);
            float[] angles = {0.0f, angleBetween + (float) Math.PI, 0.0f};

            Quaternion q = new Quaternion(angles);
            CellTransform transform = new CellTransform(q, edgePoint);

            float scale = getScale(lp, MIN_CELL_WIDTH);
            transform.setScaling(scale);

            // now we've calculated the proper position for this cell. If
            // the position has actually changed, add a record to move the
            // cell.
            lp.setPosition(transform);

            // increment the column.
            col++;
            curAngle += angleIncrement;

            // if we've done as many cells in this row as we think we can
            // fit, move up to the next row.
            if (col == maxCellsPerRow) {
                col = 0;
                row++;
                curHeight += heightIncrement;
                curAngle = segment.getStartAngle();
            }
        }
    }

    /**
     * Calculate the scale so that the given object fits entirely inside
     * a box of the given size.
     * @param lp the layout participant to get the scale for
     * @param size the size to scale to
     * @return the scale
     */
    protected float getScale(LayoutParticipant lp, float size) {
        BoundingVolume curSize = lp.getSize();
        BoundingBox curSizeBox;
        if (curSize instanceof BoundingBox) {
            curSizeBox = (BoundingBox) curSize;
        } else if (curSize instanceof BoundingSphere) {
            float r = ((BoundingSphere) curSize).radius;
            curSizeBox = new BoundingBox(new Vector3f(0f, 0f, 0f),
                                         r, r, r);
        } else {
            throw new RuntimeException("Unexpected bounds " + curSize);
        }

        float ratio = (size / 2f) / curSizeBox.xExtent;
        ratio = Math.min(ratio, (size / 2f) / curSizeBox.yExtent);
        ratio = Math.min(ratio, (size / 2f) / curSizeBox.zExtent);

        logger.info("Computed ratio for " + curSize + " size " + size +
                    " = " + ratio);


        return (ratio < 1f) ? ratio : 1f;
    }

    /**
     * implement the actual segment map storage as a ManagedObject on the
     * server.
     */
    public interface TimelineLayoutStorage {
        public Map<TimelineSegment, DatedSet<DatedLayoutParticipant>> getSegmentMap();
    }
}
