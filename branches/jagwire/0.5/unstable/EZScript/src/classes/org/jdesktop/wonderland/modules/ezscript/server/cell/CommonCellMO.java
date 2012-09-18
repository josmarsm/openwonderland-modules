
package org.jdesktop.wonderland.modules.ezscript.server.cell;

import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.ezscript.common.AttachModelMessage;
import org.jdesktop.wonderland.modules.ezscript.common.CellTriggerEventMessage;
import org.jdesktop.wonderland.modules.ezscript.common.cell.CommonCellClientState;
import org.jdesktop.wonderland.modules.ezscript.common.cell.CommonCellServerState;
import org.jdesktop.wonderland.modules.ezscript.server.EZScriptComponentMO;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author JagWire
 */
public class CommonCellMO extends CellMO {

    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelComponentRef;
    
    @UsesCellComponentMO(EZScriptComponentMO.class)
    ManagedReference<EZScriptComponentMO> ezref;


    public CommonCellMO() {
        super();
    }

    @Override
    public void setLive(boolean live) {
        super.setLive(live);

        if(live) {
            channelComponentRef.getForUpdate().addMessageReceiver(AttachModelMessage.class,
                    (ChannelComponentMO.ComponentMessageReceiver)new PassThruEventReceiver(this));
        } else {
            channelComponentRef.getForUpdate().removeMessageReceiver(AttachModelMessage.class);
        }
    }


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


    private static class PassThruEventReceiver extends AbstractComponentMessageReceiver {

        public PassThruEventReceiver(CellMO cellMO) {
            super(cellMO);
        }

        @Override
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            CellMO cellMO = getCell();
            cellMO.sendCellMessage(clientID, message);
        }
    }
}
