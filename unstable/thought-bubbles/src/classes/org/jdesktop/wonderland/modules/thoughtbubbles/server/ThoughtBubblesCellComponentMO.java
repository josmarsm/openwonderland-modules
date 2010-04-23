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

package org.jdesktop.wonderland.modules.thoughtbubbles.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.thoughtbubbles.common.ThoughtBubblesComponentChangeMessage;
import org.jdesktop.wonderland.modules.thoughtbubbles.common.ThoughtBubblesComponentClientState;
import org.jdesktop.wonderland.modules.thoughtbubbles.common.ThoughtBubblesComponentServerState;
import org.jdesktop.wonderland.modules.thoughtbubbles.common.ThoughtRecord;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

public class ThoughtBubblesCellComponentMO extends CellComponentMO {

    private static final Logger logger = Logger.getLogger(ThoughtBubblesCellComponentMO.class.getName());


    @UsesCellComponentMO(ProximityComponentMO.class)
    private ManagedReference<ProximityComponentMO> proxRef;

    /** the channel from that cell */
    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelRef;

    private Set<ThoughtRecord> thoughts;

    public ThoughtBubblesCellComponentMO(CellMO cell) {
        super(cell);
    }

    public String getClientClass() {
        return "org.jdesktop.wonderland.modules.thoughtbubbles.client.ThoughtBubblesCellComponent";
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);

        this.thoughts = ((ThoughtBubblesComponentServerState)state).getThoughts();
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new ThoughtBubblesComponentServerState();
        }

        ((ThoughtBubblesComponentServerState)state).setThoughts(this.thoughts);
        return super.getServerState(state);
    }


    @Override
    public CellComponentClientState getClientState(CellComponentClientState CellComponentClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (CellComponentClientState == null) {
            CellComponentClientState = new ThoughtBubblesComponentClientState();
        }

        logger.warning("Getting client state!");
        ((ThoughtBubblesComponentClientState)CellComponentClientState).setThoughts(thoughts);
        
        return super.getClientState(CellComponentClientState, clientID, capabilities);
    }


    private void addThought(ThoughtRecord thought) {
        this.thoughts.add(thought);
        logger.info("Added a thought: " + thought + " (total of " + this.thoughts.size() + " thoughts now)");
    }
    
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        logger.info("Setting ThoughtBubblesCellComponentMO live: " + live);

        if(live) {
            channelRef.getForUpdate().addMessageReceiver(ThoughtBubblesComponentChangeMessage.class, (ChannelComponentMO.ComponentMessageReceiver)new ThoughtBubblesCellComponentMessageReceiver(cellRef.get(), this));
        }
        else {
            channelRef.getForUpdate().removeMessageReceiver(ThoughtBubblesComponentChangeMessage.class);
        }
    }
    
    private static class ThoughtBubblesCellComponentMessageReceiver extends AbstractComponentMessageReceiver {

        private ManagedReference<ThoughtBubblesCellComponentMO> compMORef;

        public ThoughtBubblesCellComponentMessageReceiver(CellMO cellMO, ThoughtBubblesCellComponentMO compMO) {
            super(cellMO);
            compMORef = AppContext.getDataManager().createReference(compMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            // do something.

            ThoughtBubblesComponentChangeMessage bsccm = (ThoughtBubblesComponentChangeMessage)message;

            switch(bsccm.getAction()) {
                case NEW_THOUGHT:
                    compMORef.getForUpdate().addThought(bsccm.getThought());

                    // Now send the message on to all other clients.
                    Set<WonderlandClientID> clients = sender.getClients();
                    clients.remove(clientID);

                    sender.send(clients, bsccm);
                    break;
            }
        }
    }
}