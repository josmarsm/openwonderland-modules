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

import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.layout.common.LayoutParticipantCellComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 * A server-side Cell Component that stores the unique ID for the layout
 * participant. This is attached to the child CellMO so that Cells can be
 * identified if they are persisted to XML in WFS.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class LayoutParticipantCellComponentMO extends CellComponentMO {

    // The order in which this participant was added to the layout
    private int position = -1;

    /**
     * Constructor.
     * @param cell The CellMO to which this component is attached
     */
    public LayoutParticipantCellComponentMO(CellMO cell) {
        super(cell);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientClass() {
        // There is no client-side part of this component
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellComponentServerState getServerState(
            CellComponentServerState state) {

        // If the server state is null, then create one
        if (state == null) {
            state = new LayoutParticipantCellComponentServerState();
        }
        ((LayoutParticipantCellComponentServerState)state).setPosition(position);

        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);

        position = ((LayoutParticipantCellComponentServerState)state).
                getPosition();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
