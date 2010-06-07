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
package org.jdesktop.wonderland.modules.scriptingNpc.server.cell;

import org.jdesktop.wonderland.common.cell.state.CellServerState;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.scriptingNpc.common.NpcCellChangeMessage;
import org.jdesktop.wonderland.modules.scriptingNpc.common.NpcCellClientState;
import org.jdesktop.wonderland.modules.scriptingNpc.common.NpcCellServerState;
import org.jdesktop.wonderland.modules.scriptingComponent.server.ScriptingComponentMO;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableAvatarComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * The server-side Cell MO for the NPC Cell.
 * 
 * @author paulby
 * @author david <dmaroto@it.uc3m.es> UC3M - "Project España Virtual"
 * @author Jordan Slott (jslott@dev.java.net)
 */
public class NpcCellMO extends CellMO {

    @UsesCellComponentMO(MovableAvatarComponentMO.class)
    private ManagedReference<MovableAvatarComponentMO> avatarMovableComp;

//    @UsesCellComponentMO(NpcAvatarConfigComponentMO.class)
//    private ManagedReference<NpcAvatarConfigComponentMO> avatarConfigComp;
    
    // The relative avatar configuration URL
    private String relativeConfigURL = null;

    /** Default constructor */
    public NpcCellMO() {
        addComponent(new ScriptingComponentMO(this), ScriptingComponentMO.class);
        System.out.println("In NPC MO constructor");
    }

    /**
     * Returns the URL of the avatar configuration, relative to the module.
     * 
     * @return The relative URL of the avatar configuration file
     */
    public String getRelativeConfigURL() {
        System.out.println("In NPC MO - getRelativeConfigURL - URL = " + relativeConfigURL);
        return relativeConfigURL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
                                            ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.scriptingNpc.client.cell.NpcCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState state) {
        System.out.println("In NPC MO getServerState");
        // Create an appropriate NPC server state if one does not exist
        if (state == null) {
            state = new NpcCellServerState();
        }
        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        System.out.println("In NPC MO setServerState - state = " + state);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState state,
            WonderlandClientID clientID, ClientCapabilities capabilities) {

        System.out.println("In NPC MO getClientState");
        // Create a new client state if one does not exist
        if (state == null) {
            state = new NpcCellClientState();
        }
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live)
        {
        super.setLive(live);

        System.out.println("In NPC MO - setLive");
        // Either add or remove the message receiver depending upon the live
        // state.
        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if (live == true)
            {
            channel.addMessageReceiver(NpcCellChangeMessage.class, new NpcCellMessageReceiver(this));
            }
        else
            {
            channel.removeMessageReceiver(NpcCellChangeMessage.class);
            }
        }

    /**
     * A class to receive NpcCellChangeMessages and relay them to all clients.
     */
    private static class NpcCellMessageReceiver extends AbstractComponentMessageReceiver
        {
        public NpcCellMessageReceiver(NpcCellMO cellMO)
            {
            super(cellMO);
            }

        /**
         * {@inheritDoc}
         */
        public void messageReceived(WonderlandClientSender sender,
                WonderlandClientID clientID, CellMessage message)
            {

            System.out.println("In NPC MO - messageReceived");
            NpcCellMO cellMO = (NpcCellMO)getCell();
            NpcCellChangeMessage sccm = (NpcCellChangeMessage)message;
            cellMO.setLocalTransform(sccm.getCellTransform());
            cellMO.sendCellMessage(clientID, message);
            }
        }
}
