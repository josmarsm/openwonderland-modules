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

package org.jdesktop.wonderland.modules.chatzones.common;

import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupID;

public class ChatZonesCellClientState extends CellClientState {

    private int numAvatarsInZone = 0;
    private GroupID group = null;
    
    public ChatZonesCellClientState() {
        
    }

    public int getNumAvatarsInZone() {
        return numAvatarsInZone;
    }

    public void setNumAvatarsInZone(int numAvatarsInZone) {
        this.numAvatarsInZone = numAvatarsInZone;
    }

    public GroupID getGroup() {
        return group;
    }

    public void setGroup(GroupID group) {
        this.group = group;
    }


}