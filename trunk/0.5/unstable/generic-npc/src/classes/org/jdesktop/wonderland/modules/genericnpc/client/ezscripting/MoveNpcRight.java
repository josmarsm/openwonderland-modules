/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.genericnpc.client.ezscripting;

import imi.character.avatar.AvatarContext.TriggerNames;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.genericnpc.client.cell.NpcCell;
import org.jdesktop.wonderland.modules.genericnpc.client.cell.NpcControls;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class MoveNpcRight implements ScriptMethodSPI {
    private NpcCell cell;
    private NpcControls controls;
    private boolean fail = true;
    private static final Logger logger = Logger.getLogger(MoveNpcForward.class.getName());
    public String getFunctionName() {
        return "MoveNpcRight";
    }

    public void setArguments(Object[] os) {
        if(!(os[0] instanceof NpcCell)) {
            //cell = (NpcCell)os[0];
            return;
        }

        cell = (NpcCell)os[0];
        controls = cell.getControls();
        fail = false;
    }

    public String getDescription() {
        return "Move an npc to the right one unit.\n" +
                "usage: MoveNpcRight(cell);";
    }

    public String getCategory() {
        return "NPCs";
    }

    public void run() {
        if(fail) {
            return;
        }
        controls.triggerActionStart(TriggerNames.Move_Right);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(MoveNpcRight.class.getName()).log(Level.SEVERE, null, ex);
        }
        controls.triggerActionStop(TriggerNames.Move_Right);
    }
}
