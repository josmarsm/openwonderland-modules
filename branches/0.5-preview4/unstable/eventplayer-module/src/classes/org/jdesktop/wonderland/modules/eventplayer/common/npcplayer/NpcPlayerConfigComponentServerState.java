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
package org.jdesktop.wonderland.modules.eventplayer.common.npcplayer;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.AvatarConfigComponentServerState;

/**
 * Server state for NPC avatar configuration component.
 * Adapted from NPC module.
 *
 * @author Jordan Slot <jslott@dev.java.net>
 * @author Bernard Horan
 */
@XmlType( namespace="eventplayer" )
@XmlRootElement(name="npc-player-config-component")
@ServerState
public class NpcPlayerConfigComponentServerState extends AvatarConfigComponentServerState {

    /** Default constructor */
    public NpcPlayerConfigComponentServerState() {
        super();
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.eventplayer.server.npcplayer.NpcPlayerConfigComponentMO";
    }
}
