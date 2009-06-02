/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.mynpc.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.AvatarConfigComponentServerState;
import org.jdesktop.wonderland.modules.mynpc.common.mynpcCellServerState;

/**
 *
 * @author morrisford
 */
@CellFactory
public class mynpcCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) 
        {
        mynpcCellServerState state = new mynpcCellServerState();
        AvatarConfigComponentServerState astate = new AvatarConfigComponentServerState();
        astate.setAvatarConfigURL("assets/configurations/MaleMeso_01.xml");
        state.addComponentServerState(astate);

        return (T)state;
        }

    public String getDisplayName() {
        return "MYNPC";
    }

    public Image getPreviewImage() 
        {
        URL url = mynpcCellFactory.class.getResource("resources/NPC2.png");
        return Toolkit.getDefaultToolkit().createImage(url);
        }

}