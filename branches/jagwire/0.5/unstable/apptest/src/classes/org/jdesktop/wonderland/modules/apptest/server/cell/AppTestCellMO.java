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
package org.jdesktop.wonderland.modules.apptest.server.cell;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.appbase.server.cell.App2DCellMO;
import org.jdesktop.wonderland.modules.apptest.common.cell.AppTestCellClientState;
import org.jdesktop.wonderland.modules.apptest.common.cell.AppTestCellServerState;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A server cell associated with the app test.
 *
 * @author nsimpson,deronj
 */
@ExperimentalAPI
public class AppTestCellMO extends App2DCellMO {

    /** The preferred width (from the WFS file) */
    private int preferredWidth;
    /** The preferred height (from the WFS file) */
    private int preferredHeight;

    /** Default constructor, used when the cell is created via WFS */
    public AppTestCellMO() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.apptest.client.cell.AppTestCell";
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new AppTestCellClientState(pixelScale);
        }
        ((AppTestCellClientState) cellClientState).setPreferredWidth(preferredWidth);
        ((AppTestCellClientState) cellClientState).setPreferredHeight(preferredHeight);
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        AppTestCellServerState serverState = (AppTestCellServerState) state;
        preferredWidth = serverState.getPreferredWidth();
        preferredHeight = serverState.getPreferredHeight();
        pixelScale = new Vector2f(serverState.getPixelScaleX(), serverState.getPixelScaleY());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState stateToFill) {
        if (stateToFill == null) {
            return null;
        }

        super.getServerState(stateToFill);

        AppTestCellServerState state = (AppTestCellServerState) stateToFill;
        state.setPreferredWidth(preferredWidth);
        state.setPreferredHeight(preferredHeight);

        return stateToFill;
    }
}
