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

import java.util.Map;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.AvatarConfigComponentServerState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.AvatarConfigInfo;
import org.jdesktop.wonderland.common.cell.state.AvatarCellServerState;

/**
 * Represents the server-side configuration information for the NPC player cell.
 * Has JAXB annotations so that it can be serialized to XML.
 * <p>
 * Adapted from the NPC module.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@XmlType( namespace="eventplayer" )
@XmlRootElement(name = "npc-player-cell")
@ServerState
public class NpcPlayerCellServerState extends CellServerState {
    private static final Logger logger = Logger.getLogger(NpcPlayerCellServerState.class.getName());
    private String userName;

    /** Default constructor */
    public NpcPlayerCellServerState() {
    }

    /**
     * Constructor that takes an AvaterCellServerState and creates a new instance using its internal state.
     * @param setup prototype from which this instance should be constructed
     */
    public NpcPlayerCellServerState(AvatarCellServerState setup) {
        this();
        //Get the avatar config info
        AvatarConfigComponentServerState configComponent = (AvatarConfigComponentServerState) setup.getComponentServerState(AvatarConfigComponentServerState.class);
        logger.info("config component: " + configComponent);
        AvatarConfigInfo info = configComponent.getAvatarConfigInfo();
        logger.info("config info: " + info);

        //Get the avatar position info
        PositionComponentServerState positionComponent = (PositionComponentServerState) setup.getComponentServerState(PositionComponentServerState.class);
        logger.info("position component: " + positionComponent);

        //Get the metadata from the setup state
        Map<String, String> metadata = setup.getMetaData();

        //Get the name from the setup state
        String name = setup.getUserName();

        // Attach an avatar config component. This will
        // configure the appearance of the NPC.
        NpcPlayerConfigComponentServerState accss = new NpcPlayerConfigComponentServerState();
        accss.setAvatarConfigInfo(info);
        addComponentServerState(accss);

        //Set my position
        addComponentServerState(positionComponent);

        //Set my metadata
        setMetaData(metadata);

        //Set my name
        setUserName(name);
    }

    

    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.eventplayer.server.npcplayer.NpcPlayerCellMO";
    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get the user name
     * @return the user name for the npc
     */
    public String getUserName() {
        return userName;
    }
}
