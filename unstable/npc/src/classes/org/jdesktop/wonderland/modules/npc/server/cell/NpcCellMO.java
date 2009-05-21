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
package org.jdesktop.wonderland.modules.npc.server.cell;

import org.jdesktop.wonderland.modules.avatarbase.server.cell.*;
import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigComponentServerState;
import org.jdesktop.wonderland.modules.npc.common.NpcCellChangeMessage;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableAvatarComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author paulby
 * @author david <dmaroto@it.uc3m.es> UC3M - "Project España Virtual"
 */
public class NpcCellMO extends CellMO {
    private Vector3f npcPosition;

    public NpcCellMO(String avatarConfigURL, CellTransform transform) {
        super(new BoundingBox(new Vector3f(0,1,0), 1,2,1), transform);
        
        AvatarConfigComponentMO avatarConfig = new AvatarConfigComponentMO(this);
        AvatarConfigComponentServerState state = new AvatarConfigComponentServerState();
        state.setAvatarConfigURL(avatarConfigURL);
        avatarConfig.setServerState(state);
        addComponent(avatarConfig);
    }

    public NpcCellMO() {
        addComponent(new ChannelComponentMO(this));
        addComponent(new MovableAvatarComponentMO(this), MovableComponentMO.class);
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
                                            ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.npc.client.cell.NpcCell";
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        return super.getClientState(cellClientState, clientID, capabilities);
    }
    
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if (live == true) {
            channel.addMessageReceiver(NpcCellChangeMessage.class,
                (ChannelComponentMO.ComponentMessageReceiver)new NpcCellMessageReceiver(this));
        }
        else {
            channel.removeMessageReceiver(NpcCellChangeMessage.class);
        }
    }

    
    private static class NpcCellMessageReceiver extends AbstractComponentMessageReceiver {
        public NpcCellMessageReceiver(NpcCellMO cellMO) {
            super(cellMO);
        }
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            NpcCellMO cellMO = (NpcCellMO)getCell();
            NpcCellChangeMessage sccm = (NpcCellChangeMessage)message;
            cellMO.npcPosition = sccm.getNpcPosition();
            cellMO.sendCellMessage(clientID, message);


        }
    }
}
