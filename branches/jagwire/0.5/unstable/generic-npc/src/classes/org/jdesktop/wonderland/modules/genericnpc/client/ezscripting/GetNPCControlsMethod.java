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

    private NpcCell cell;
    private NpcControls controls = null;
    private boolean fail = false;
    
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
            fail = true;
        }
    }

    public void run() {
        if(fail)
            return;
        
        controls = cell.getControls();
    }

    public String getDescription() {
        return "usage: var controls = getNpcControls(cell)\n"
                + "\t controls.playAnimation(\"Male_Wave\");\n"
                +"- return an NpcControls object from the specified NpcCell.";
    }

    public String getCategory() {
        return "NPCs";
    }

}
