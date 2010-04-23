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
import org.jdesktop.wonderland.modules.avatarbase.server.cell.AvatarConfigComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 * A server-side Cell component that represents the current avatar configuration
 * for NPC players. Copied from the NPC module.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@ComponentLookupClass(AvatarConfigComponentMO.class)
public class NpcPlayerConfigComponentMO extends AvatarConfigComponentMO {

    /** Constructor */
    public NpcPlayerConfigComponentMO(CellMO cell) {
        super(cell);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.eventplayer.client.npcplayer.NpcPlayerConfigComponent";
    }
}
