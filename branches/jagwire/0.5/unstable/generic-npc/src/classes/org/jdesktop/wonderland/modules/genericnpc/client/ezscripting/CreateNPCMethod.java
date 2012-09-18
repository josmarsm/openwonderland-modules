/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.genericnpc.client.ezscripting;

import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.AvatarConfigInfo;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.genericnpc.client.cell.NpcCellFactory;
import org.jdesktop.wonderland.modules.genericnpc.common.NpcAvatarConfigComponentServerState;
import org.jdesktop.wonderland.modules.genericnpc.common.NpcCellServerState;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class CreateNPCMethod implements ScriptMethodSPI {

    public String getFunctionName() {
        return "createNPC";
    }

    public void setArguments(Object[] os) {
        //do nothing
    }

    public void run() {
        //NpcCellFactory factory = new NpcCellFactory();
        //NpcCellServerState state = factory.getDefaultCellServerState(null);
        //if(state == null) {
            System.out.println("Factory npc failed.");
           NpcCellServerState state = new NpcCellServerState();
            
            NpcAvatarConfigComponentServerState accss = new NpcAvatarConfigComponentServerState();
            String url = "wla://avatarbaseart/assets/configurations/MaleMeso_01.xml";
            String className = "org.jdesktop.wonderland.modules.avatarbase.client.imi.ImiAvatarLoaderFactory";
            AvatarConfigInfo info = new AvatarConfigInfo(url, className);
            accss.setAvatarConfigInfo(info);
            state.addComponentServerState(accss);
            
        //}
        try {
            CellUtils.createCell(state);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getDescription() {
        return "usage: createNPC()\n\n"
                +"-create a new NPC cell.";
    }

    public String getCategory() {
        return "NPCs";
    }

}
