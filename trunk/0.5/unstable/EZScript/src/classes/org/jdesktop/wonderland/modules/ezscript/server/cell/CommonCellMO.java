
package org.jdesktop.wonderland.modules.ezscript.server.cell;

import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.ezscript.common.cell.CommonCellClientState;
import org.jdesktop.wonderland.modules.ezscript.common.cell.CommonCellServerState;
import org.jdesktop.wonderland.modules.ezscript.server.EZScriptComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author JagWire
 */
public class CommonCellMO extends CellMO {


    public CommonCellMO() {
        super();
    }
    @UsesCellComponentMO(EZScriptComponentMO.class)
    ManagedReference<EZScriptComponentMO> ezref;

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.ezscript.client.cell.CommonCell";
    }

    public CellClientState getClientState(CellClientState state,
                                            WonderlandClientID clientID,
                                            ClientCapabilities capabilities) {
        if(state == null) {
            state = new CommonCellClientState();        
        }

        return super.getClientState(state, clientID, capabilities);
    }

    public CellServerState getServerState(CellServerState state) {
        if(state == null) {
            state = new CommonCellServerState();
        }
        return super.getServerState(state);
    }

    public void setServerState(CellServerState state) {
        super.setServerState(state);
    }

}
