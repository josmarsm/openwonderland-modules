package org.jdesktop.wonderland.modules.ezscript.client.methods;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.containercell.client.ContainerComponent;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
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

    public String getDescription() {
        return "usage: AddContainer(cell)\n\n"
                +"-creates a container component for the given cell.";
    }

    public String getCategory() {
        return "architecture";
    }

}
