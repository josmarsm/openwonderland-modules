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
package org.jdesktop.wonderland.modules.eventplayer.client.npcplayer;

import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.MovableAvatarComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.eventplayer.common.npcplayer.NpcPlayerCellClientState;

/**
 * Cell that represents an NPC in the event player. Adapted from the NPC module.
 * @author paulby
 * @author david <dmaroto@it.uc3m.es> UC3M - "Project España Virtual"
 * @author Bernard Horan
 */
public class NpcPlayerCell extends AvatarCell {

    @UsesCellComponent
    private MovableAvatarComponent movableNPC;

    private WonderlandIdentity identity;

    public NpcPlayerCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);        
    }
    
    @Override
    public void setClientState(CellClientState cellClientState) {
        String userName = ((NpcPlayerCellClientState)cellClientState).getUserName();
        this.identity = new WonderlandIdentity(userName, null, null);
        super.setClientState(cellClientState);
    }

    @Override
    public WonderlandIdentity getIdentity() {
        return identity;
    }
    
}
