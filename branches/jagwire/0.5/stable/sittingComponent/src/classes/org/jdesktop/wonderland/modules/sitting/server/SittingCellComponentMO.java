/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.sitting.server;

import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.sitting.common.SittingCellComponentClientState;
import org.jdesktop.wonderland.modules.sitting.common.SittingCellComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.InteractionComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author Morris Ford
 */
public class SittingCellComponentMO extends CellComponentMO
    {

    private static Logger logger = Logger.getLogger(SittingCellComponentMO.class.getName());
    private float heading = 0.1f;
    private float offset = 0.1f;
    private String mouse = "Left Mouse";
    private boolean mouseEnable = false;

    @UsesCellComponentMO(InteractionComponentMO.class)
    private ManagedReference<InteractionComponentMO> interactionRef;

    public SittingCellComponentMO(CellMO cell)
        {
        super(cell);
        }

    @Override
    protected String getClientClass()
        {
        return "org.jdesktop.wonderland.modules.sitting.client.SittingCellComponent";
        }

    @Override
    protected void setLive(boolean live)
        {
        super.setLive(live);
        logger.fine("Setting SittingCellComponentMO to live = " + live);

        // make sure the cell is not collidable
        if (live) {
            interactionRef.get().setCollidable(false);
        }
        }


    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new SittingCellComponentClientState();
        }
        ((SittingCellComponentClientState)state).setMouseEnable(mouseEnable);
        ((SittingCellComponentClientState)state).setHeading(heading);
        ((SittingCellComponentClientState)state).setOffset(offset);
        ((SittingCellComponentClientState)state).setMouse(mouse);
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new SittingCellComponentServerState();
        }
        ((SittingCellComponentServerState)state).setMouseEnable(mouseEnable);
        ((SittingCellComponentServerState)state).setHeading(heading);
        ((SittingCellComponentServerState)state).setOffset(offset);
        ((SittingCellComponentServerState)state).setMouse(mouse);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        mouseEnable = ((SittingCellComponentServerState)state).getMouseEnable();
        heading = ((SittingCellComponentServerState)state).getHeading();
        offset = ((SittingCellComponentServerState)state).getOffset();
        mouse = ((SittingCellComponentServerState)state).getMouse();
    }
}
