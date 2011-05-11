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

package org.jdesktop.wonderland.modules.webcaster.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellClientState;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * @author Christian O'Connell
 */
public class WebcasterCellMO extends CellMO
{
    public WebcasterCellMO(){
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities){
        return "org.jdesktop.wonderland.modules.webcaster.client.WebcasterCell";
    }

    @Override
    public void setServerState(CellServerState state){
        super.setServerState(state);
    }

    @Override
    public CellServerState getServerState(CellServerState state)
    {
        if (state == null) {
            state = new WebcasterCellServerState();
        }

        return super.getServerState(state);
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities)
    {
        if (cellClientState == null){
            cellClientState = new WebcasterCellClientState();
        }

        return super.getClientState(cellClientState, clientID, capabilities);
    }
}
