/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.genericnpc.client.ezscripting;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;
import org.jdesktop.wonderland.modules.genericnpc.client.cell.NpcCell;
import org.jdesktop.wonderland.modules.genericnpc.client.cell.NpcControls;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class GetNPCControlsMethod implements ReturnableScriptMethodSPI {

    NpcCell cell;
    NpcControls controls = null;
    public Object returns() {
       return controls;
    }

    public String getFunctionName() {
        return "getNpcControls";
    }

    public void setArguments(Object[] os) {
        if(os[0] instanceof NpcCell) {
            cell = (NpcCell)os[0];
        } else {
            System.out.println("This cell is not an NPC!");
            System.out.println("Possible script errors may appear!");
        }
    }

    public void run() {
        controls = cell.getControls();
    }

    public String getDescription() {
        return "usage: getNpcControls(cell)\n\n"
                +"- return an NpcControls object from the specified NpcCell.";
    }

    public String getCategory() {
        return "NPCs";
    }

}
