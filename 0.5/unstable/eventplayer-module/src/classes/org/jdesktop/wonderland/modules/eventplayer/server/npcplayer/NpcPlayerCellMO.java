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
package org.jdesktop.wonderland.modules.eventplayer.server.npcplayer;

import org.jdesktop.wonderland.common.cell.state.CellServerState;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.eventplayer.common.npcplayer.NpcPlayerCellClientState;
import org.jdesktop.wonderland.modules.eventplayer.common.npcplayer.NpcPlayerCellServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.MovableAvatarComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * The server-side Cell MO for the NPC Player Cell.<br>
 * Adapted from the NPC module.
 * 
 * @author paulby
 * @author david <dmaroto@it.uc3m.es> UC3M - "Project España Virtual"
 * @author Jordan Slott (jslott@dev.java.net)
 * @author Bernard Horan
 */
public class NpcPlayerCellMO extends CellMO {

    @UsesCellComponentMO(MovableAvatarComponentMO.class)
    private ManagedReference<MovableAvatarComponentMO> npcMovableComp;

    private String userName = null;

    /** Default constructor */
    public NpcPlayerCellMO() {
    }

  

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
                                            ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.eventplayer.client.npcplayer.NpcPlayerCell";
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        // Create an appropriate NPC server state if one does not exist
        if (state == null) {
            state = new NpcPlayerCellServerState();
        }
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellServerState state) {
        userName = ((NpcPlayerCellServerState)state).getUserName();
        super.setServerState(state);
    }


    @Override
    public CellClientState getClientState(CellClientState state,
            WonderlandClientID clientID, ClientCapabilities capabilities) {

        // Create a new client state if one does not exist
        if (state == null) {
            state = new NpcPlayerCellClientState(userName);
        }
        return super.getClientState(state, clientID, capabilities);
    }

    

    
}
