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

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.apptest.common.cell.AppTestCellComponentClientState;
import org.jdesktop.wonderland.modules.apptest.common.cell.AppTestCellComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A component which is attached to the server cells which app test launches.
 * This causes the AppTestCellComponent to be attached to the client cells
 * of these cells and passes app-test-specific information to these client components.
 *
 * NOTE: what is unique about this component is that it is not attached
 * to the app test cell. Instead, it is attached to the cells that the 
 * app test *launches*.
 */
public class AppTestCellComponentMO extends CellComponentMO {

    private static Logger logger = Logger.getLogger(AppTestCellComponentMO.class.getName());

    private CellID appTestCellID;
    private String displayName;

    public AppTestCellComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.apptest.client.cell.AppTestCellComponent";
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new AppTestCellComponentClientState();
        }
        ((AppTestCellComponentClientState)state).setAppTestCellID(appTestCellID);
        ((AppTestCellComponentClientState)state).setDisplayName(displayName);
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new AppTestCellComponentServerState();
        }
        ((AppTestCellComponentServerState)state).setAppTestCellID(appTestCellID);
        ((AppTestCellComponentServerState)state).setDisplayName(displayName);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        appTestCellID = ((AppTestCellComponentServerState)state).getAppTestCellID();
        displayName = ((AppTestCellComponentServerState)state).getDisplayName();
    }
}
