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

import org.jdesktop.wonderland.modules.timeline.common.layout.DatedObjectComponentServerState;
import org.jdesktop.wonderland.modules.timeline.common.layout.DatedObjectComponentClientState;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author drew
 */
public class DatedObjectComponentMO extends CellComponentMO {

    private DatedObject datedObject;

    private boolean addedToTimeline = false;
    private boolean needsLayout = true;

    public DatedObjectComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.timeline.client.layout.DatedObjectCellComponent";
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state,
                                                   WonderlandClientID clientID,
                                                   ClientCapabilities capabilities)
    {
        if (state == null) {
            state = new DatedObjectComponentClientState();
        }
        
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new DatedObjectComponentServerState();
        }

        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);


    }


    public DatedObject getDatedObject() {
        return datedObject;
    }

    public void setDatedObject(DatedObject datedObject) {
        this.datedObject = datedObject;
    }

    void setAddedToTimeline(boolean b) {
        this.addedToTimeline = b;
    }

    void setNeedsLayout(boolean b) {
        this.needsLayout = b;
    }

    public boolean isAddedToTimeline() {
        return addedToTimeline;
    }

    public boolean isNeedsLayout() {
        return needsLayout;
    }
}
