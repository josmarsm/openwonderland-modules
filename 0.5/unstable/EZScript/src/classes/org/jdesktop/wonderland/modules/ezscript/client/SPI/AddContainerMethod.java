/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.SPI;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.avps.common.AVPSCellServerState;
import org.jdesktop.wonderland.modules.avps.common.AVPSutil.AVPSTYPE;
import org.jdesktop.wonderland.modules.avps.common.AVPSutil.STATE;
import org.jdesktop.wonderland.modules.containercell.client.ContainerComponent;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class AddContainerMethod implements ScriptMethodSPI {

    Cell cell;
    
    public String getFunctionName() {
        return "AddContainer";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
    }

    public void run() {
        /*state = new AVPSCellServerState();
        state.setType(AVPSTYPE.SPACE);
        state.setState(STATE.TRANSPARENT);
        state.setRadius(1.0f);
        state.setLockable(true);*/

        if(cell.getComponent(ContainerComponent.class) == null) {
            new Thread(new Runnable() {
                public void run() {
                    String className = "org.jdesktop.wonderland.modules"
                                    +  ".containercell.server"
                                    +  ".ContainerComponentMO";
                    CellServerComponentMessage cscm =
                      CellServerComponentMessage.newAddMessage(cell.getCellID(),
                                                               className);

                    ResponseMessage response = cell.sendCellMessageAndWait(cscm);
                    if(response instanceof ErrorMessage) {
                        
                        System.out.println("Unable to add container compoent.");
                    }
                }
            }).start();
        }

    }

}
