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

package org.jdesktop.wonderland.modules.ezscript.server;

import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.ezscript.common.EZScriptComponentClientState;
import org.jdesktop.wonderland.modules.ezscript.common.EZScriptComponentServerState;
import org.jdesktop.wonderland.modules.ezscript.common.CellTriggerEventMessage;
import org.jdesktop.wonderland.modules.ezscript.server.cell.AnotherMovableComponentMO;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Scripting CellComponentMO based on a sample cell component by,
 *
 * @author Jordan Slott <jslott@dev.java.net>
 * @author JagWire
 */
public class EZScriptComponentMO extends CellComponentMO {

    private static Logger logger = Logger.getLogger(EZScriptComponentMO.class.getName());
    private String info = null;

    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sharedStateComponentRef;

    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelComponentRef;

    @UsesCellComponentMO(AnotherMovableComponentMO.class)
    private ManagedReference<AnotherMovableComponentMO> anotherMovableRef;
    
    //I dislike having to forcibly attach this...
    @UsesCellComponentMO(MovableComponentMO.class)
    private ManagedReference<MovableComponentMO> movableComponentRef;
    public EZScriptComponentMO(CellMO cell) {
        super(cell);
       
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.ezscript.client.EZScriptComponent";
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        logger.warning("Setting EZScriptComponentMO to live = " + live);
        if(live) {
            channelComponentRef.getForUpdate().addMessageReceiver(CellTriggerEventMessage.class,
                    (ChannelComponentMO.ComponentMessageReceiver)new FarCellEventReceiver(this.cellRef.get()));
        } else {
            channelComponentRef.getForUpdate().removeMessageReceiver(CellTriggerEventMessage.class);
        }
    }


    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new EZScriptComponentClientState();
        }
        ((EZScriptComponentClientState)state).setInfo(info);
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new EZScriptComponentServerState();
        }
        ((EZScriptComponentServerState)state).setInfo(info);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        info = ((EZScriptComponentServerState)state).getInfo();
    }

    private static class FarCellEventReceiver extends AbstractComponentMessageReceiver {

        public FarCellEventReceiver(CellMO cellMO) {
            super(cellMO);
        }

        @Override
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {            
            CellMO cellMO = getCell();
            cellMO.sendCellMessage(clientID, message);            
        }
    }
}
