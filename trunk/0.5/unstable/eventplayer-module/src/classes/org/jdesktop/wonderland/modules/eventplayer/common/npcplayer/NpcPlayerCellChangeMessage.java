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


package org.jdesktop.wonderland.modules.eventplayer.common.npcplayer;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;


/**
 * A message to indicate a change in position of the NPC player Cell.
 * Adapted from the NPC module.
 *
 * @author david <dmaroto@it.uc3m.es>
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
public class NpcPlayerCellChangeMessage extends CellMessage {
    
    private CellTransform transform;

    public NpcPlayerCellChangeMessage(CellID cellID, CellTransform transform) {
        super(cellID);
        this.transform = transform;
    }

    /**
     * Get the cell transform
     * @return the cell transform
     */
    public CellTransform getCellTransform(){
        return transform;
    }
}
