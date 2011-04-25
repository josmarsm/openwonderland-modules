package org.jdesktop.wonderland.modules.webcaster.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellClientState;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

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
