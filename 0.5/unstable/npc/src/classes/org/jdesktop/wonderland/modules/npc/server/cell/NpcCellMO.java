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
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigComponentServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableAvatarComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author paulby
 */
public class NpcCellMO extends CellMO {

    public NpcCellMO(String avatarConfigURL, CellTransform transform) {
        super(new BoundingBox(new Vector3f(0,1,0), 1,2,1), transform);
        AvatarConfigComponentMO avatarConfig = new AvatarConfigComponentMO(this);
        AvatarConfigComponentServerState state = new AvatarConfigComponentServerState();
        state.setAvatarConfigURL(avatarConfigURL);
        avatarConfig.setServerState(state);
        addComponent(avatarConfig);
        addComponent(new ChannelComponentMO(this));
        addComponent(new MovableAvatarComponentMO(this), MovableComponentMO.class);
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID,
                                            ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.avatarbase.client.cell.NpcCell";
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        return super.getClientState(cellClientState, clientID, capabilities);
    }

}
