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
package org.jdesktop.wonderland.modules.layout.common;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.layout.api.common.*;

/**
 * An layout that arranges its participants in an M x N grid.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class TableLayout extends AbstractLayout {

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(TableLayout.class.getName());
    
    /**
     * Default constructor
     */
    public TableLayout() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int addParticipant(LayoutParticipant participant) {
        int index = super.addParticipant(participant);

        // Do the layout, filling in the M x N grid. Note that this layout is
        // done AFTER the layout participant is notified that it has been
        // added. (Is this correct?)
        LOGGER.warning("Added layout participant " + participant);

        // See what position the new participant is at.
        if (index == -1) {
            LOGGER.warning("Participant not found in list " + participant);
            return -1;
        }

        // Figure out the row (z-axis) and column (x-axis) that this new
        // participant should go into, based upon its index in the list.
        int row = index / ((TableLayoutConfig)getConfig()).getN();
        int col = index % ((TableLayoutConfig)getConfig()).getN();

        LOGGER.warning("Layout participant " + participant + " is at row " +
                row + " column " + col);

        // Convert the row and column into a translation with respect to the
        // center of the cell. We need to take the size of the layout area and
        // divide into the number of rows and columns
        BoundingVolume bounds = getConfig().getBounds();
        float xExtent = 0, zExtent = 0;
        if (bounds instanceof BoundingSphere) {
            xExtent = zExtent = ((BoundingSphere)bounds).radius;
        }
        else {
            xExtent = ((BoundingBox)bounds).xExtent;
            zExtent = ((BoundingBox)bounds).zExtent;
        }
        float rowSize = 2 * zExtent / ((TableLayoutConfig)getConfig()).getM();
        float colSize = 2 * xExtent / ((TableLayoutConfig)getConfig()).getN();

        // Recast the desired row and col of the new participant into the x-axis
        // and z-axis coordinates, where we assume the participant at (0, 0) is
        // at the origin of the coordinate axis. We then need to shift the x, z
        // coordinate so that the center of the area coincides with the center
        // of the layout area.
        float z = (rowSize * row) - zExtent;
        float x = (colSize * col) - xExtent;

        LOGGER.warning("Putting participant at x=" + x + ", z=" + z);

        // Tell the participant to move itself, based upon its current position.
        // XXX
        // Reset the rotation to 0. Also, what do we do about the y-coordinate?
        // XXX
        CellTransform transform = participant.getPosition();
        Vector3f translation = transform.getTranslation(null);
        Vector3f newTranslation = new Vector3f(x, translation.y, z);
        transform.setTranslation(newTranslation);
        transform.setRotation(new Quaternion());
        participant.setPosition(transform);

        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeParticipant(LayoutParticipant participant) {
        super.removeParticipant(participant);

        // XXX Re-do the layout. XXX
        LOGGER.warning("Removed layout participant " + participant);

        // XXX
        // What exactly do we do here? Just not manage the participant and
        // leave it where it is? Do we fill in the hole left by the old
        // participant?
        // XXX
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParticipantAt(LayoutParticipant participant, int index) {
        super.setParticipantAt(participant, index);
    }
}
