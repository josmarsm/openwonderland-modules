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
package org.jdesktop.wonderland.modules.scriptingNpc.client.cell;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.avatarbase.client.imi.ImiAvatarLoaderFactory;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.AvatarConfigInfo;
import org.jdesktop.wonderland.modules.scriptingNpc.common.NpcAvatarConfigComponentServerState;
import org.jdesktop.wonderland.modules.scriptingNpc.common.NpcCellServerState;

/**
 * The cell factory for the sample cell.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@CellFactory
public class NpcCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        System.out.println("NPC in getDefaultCellServerState");
        NpcCellServerState state = new NpcCellServerState();

        System.out.println("NPC After state = ");
        // Attach an avatar config component to the server state. This will help
        // configure the appearance of the NPC.
        NpcAvatarConfigComponentServerState accss = new NpcAvatarConfigComponentServerState();
        System.out.println("NPC After accs = " + accss);
        String url = "wla://avatarbaseart/assets/configurations/MaleMeso_01.xml";
        System.out.println("NPC After url = " + url);
        String className = ImiAvatarLoaderFactory.class.getName();
        System.out.println("NPC After className = " + className);
        AvatarConfigInfo info = new AvatarConfigInfo(url, className);
        System.out.println("NPC info = " + info);
        accss.setAvatarConfigInfo(info);
        System.out.println("NPC After setAvatarConfigInfo");
        state.addComponentServerState(accss);
        System.out.println("NPC After addComponentServerState");
        return (T)state;
    }

    public String getDisplayName() {
        return "NPC Cell";
    }

    public Image getPreviewImage() {
        URL url = NpcCellFactory.class.getResource("resources/NPC2.png");
        return Toolkit.getDefaultToolkit().createImage(url);
    }
}
