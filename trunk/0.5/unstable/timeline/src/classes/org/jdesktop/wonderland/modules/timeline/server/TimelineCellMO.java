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
package org.jdesktop.wonderland.modules.timeline.server;

import java.util.Set;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellClientState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellServerState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 *  
 */
public class TimelineCellMO extends CellMO {

    private TimelineConfiguration config;

    public TimelineCellMO() {
        super();
    }

    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.timeline.client.TimelineCell";
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        this.setConfiguration(((TimelineCellServerState)state).getConfig());
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new TimelineCellServerState();
        }

        ((TimelineCellServerState)state).setConfig(config);

        return super.getServerState(state);
    }


    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new TimelineCellClientState();
        }

        ((TimelineCellClientState)cellClientState).setConfig(config);
        
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if(live) {
            channel.addMessageReceiver(TimelineCellChangeMessage.class, 
		(ChannelComponentMO.ComponentMessageReceiver)new TimelineCellMessageReceiver(this));
        } else {
            channel.removeMessageReceiver(TimelineCellChangeMessage.class);
        }
    }

    public void setConfiguration(TimelineConfiguration config) {
        this.config = new TimelineServerConfiguration(config, getComponent(ChannelComponentMO.class));
    }

    private static class TimelineCellMessageReceiver extends AbstractComponentMessageReceiver {

        public TimelineCellMessageReceiver(TimelineCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, 
		CellMessage message) {

            TimelineCellMO cellMO = (TimelineCellMO)getCell();

            TimelineCellChangeMessage msg = (TimelineCellChangeMessage)message;
            cellMO.setConfiguration(msg.getConfig());

            // Send updates to all other clients.
            Set<WonderlandClientID> otherClients = sender.getClients();
            otherClients.remove(clientID);
            sender.send(otherClients, msg);
        }

    }

}
