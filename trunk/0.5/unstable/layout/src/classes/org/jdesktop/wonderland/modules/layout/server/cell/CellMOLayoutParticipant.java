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
package org.jdesktop.wonderland.modules.layout.server.cell;

import com.jme.bounding.BoundingVolume;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.layout.api.common.Layout;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutParticipant;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;

/**
 * A wrapper for CellMO's to become layout participants.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellMOLayoutParticipant implements LayoutParticipant {

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(CellMOLayoutParticipant.class.getName());

    // A reference to the Cell MO associated with this participant proxy
    private ManagedReference<CellMO> cellMORef = null;

    /**
     * Constructor, takes the CellMO associated with this participant proxy.
     */
    public CellMOLayoutParticipant(CellMO cellMO) {
        cellMORef = AppContext.getDataManager().createReference(cellMO);

        // In order to be able to move the CellMO, we need to make sure it
        // has a movable component. We can do this here -- and assume all of
        // this takes place within a DS transaction.
        if (cellMO.getComponent(MovableComponentMO.class) == null) {
            cellMO.addComponent(new MovableComponentMO(cellMO));
        }
    }

    /**
     * {@inheritDoc}
     */
    public BoundingVolume getPreferredSize() {
        // Just returns the bounds of the CellMO, I guess
        return cellMORef.get().getLocalBounds();
    }

    /**
     * {@inheritDoc}
     */
    public void setPosition(CellTransform transform) {
        // Tell the movable component to update its position, simple as that.
        MovableComponentMO movableComponentMO =
                cellMORef.get().getComponent(MovableComponentMO.class);

        // Although the movable component should exist, we double check that
        // it is no null
        if (movableComponentMO == null) {
            LOGGER.warning("Movable component is null for layout participant " +
                    "with cell ID " + cellMORef.get().getCellID());
            return;
        }

        movableComponentMO.moveRequest(null, transform);
    }

    /**
     * {@inheritDoc}
     */
    public CellTransform getPosition() {
        // Just returns the local transform, I guess
        return cellMORef.get().getLocalTransform(null);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isResizable() {
        // For now, do not allow resizing
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void setSize(BoundingVolume bounds) {
        // Since resizing is not allowed, this does nothing.
    }

    /**
     * {@inheritDoc}
     */
    public BoundingVolume getSize() {
        // Just returns the bounds of the CellMO, I guess
        return cellMORef.get().getLocalBounds();
    }

    /**
     * {@inheritDoc}
     */
    public void added(Layout layout) {
        // Do nothing by default
    }

    /**
     * {@inheritDoc}
     */
    public void removed(Layout layout) {
        // Do nothing by default
    }
}
