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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlElement;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupID;

@XmlRootElement(name="chat-zone-cell")
@ServerState
public class ChatZonesCellServerState extends CellServerState {

    @XmlElement(name="group")
    private GroupID group = null;

    @XmlElement(name="numAvatarsInZone")
    private int numAvatarsInZone = 0;

    @XmlElement(name="transform")
    private CellTransform transform;

    @XmlElement(name="parentID")
    private String parentID;

    public ChatZonesCellServerState() {
    }

    @XmlTransient public GroupID getChatGroup() { return this.group; }
    public void setChatGroup(GroupID group) { this.group = group; }

    @XmlTransient public int getNumAvatarsInZone() { return this.numAvatarsInZone; }
    public void setNumAvatarsInZone(int numAvatarInZone) { this.numAvatarsInZone = numAvatarsInZone; }

    @XmlTransient public CellTransform getInitialTransform() { return this.transform; }
    public void setInitialCellTransform(CellTransform transform) { this.transform = transform; }

    @XmlTransient public String getParentID() { return this.parentID; }
    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.chatzones.server.ChatZonesCellMO";
    }
}