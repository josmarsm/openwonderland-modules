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
package org.jdesktop.wonderland.modules.eventplayer.server.npcplayer;

import org.jdesktop.wonderland.common.cell.ComponentLookupClass;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.MovableAvatarComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;

/**
 * Server side stub that corresponds to a more interesting class on the client
 * side. Adapted from the NPC module.
 * @author paulby
 * @author Bernard Horan
 */
@ComponentLookupClass(MovableComponentMO.class)
public class MovableNpcPlayerComponentMO extends MovableAvatarComponentMO {

    public MovableNpcPlayerComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.eventplayer.client.npcplayer.MovableNpcPlayerComponent";
    }
}
