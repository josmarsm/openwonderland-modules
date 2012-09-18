/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.util.Collection;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.security.client.SecurityComponentFactory;
import org.jdesktop.wonderland.modules.security.client.SecurityQueryComponent;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class ApplyBasicSecurityMethod implements ScriptMethodSPI {

    public String getFunctionName() {
        return "ApplyBasicSecurity";
    }

    public void setArguments(Object[] args) {
        //do nothing
    }

    public String getDescription() {
       return "usage: ApplyBasicSecurity();\n\n"
               +"- applies a security component to all cells in the current cell cache.\n"
               +"- only applies to currently loaded cells. i.e. if you have objects\n"
               +"  far away from each other, you will need to execute the command\n"
               +"  again near those far away objects.";
    }

    public String getCategory() {
        return "utilities";
    }

    public void run() {

        //Cell cell;
        ServerSessionManager manager = LoginManager.getPrimary();
        WonderlandSession session = manager.getPrimarySession();
        Collection<Cell> cs = ClientContextJME.getCellCache(session).getRootCells();

        
        for(Cell c : cs) {
            if(c instanceof AvatarCell) {
                continue;
            }
            if(c.getComponent(SecurityQueryComponent.class) == null) {
                final Cell cell = c;
                System.out.println("Processing cellID: "+ cell.getCellID());
                new Thread(new Runnable() {
                    public void run() {
//                        String className = "org.jdesktop.wonderland.modules"
//                                        +  ".security.server"
//                                        +  ".SecurityComponentMO";

                        SecurityComponentFactory f = new SecurityComponentFactory();
                        CellComponentServerState s = f.getDefaultCellComponentServerState();
                        CellServerComponentMessage csm =
                            CellServerComponentMessage.newAddMessage(cell.getCellID(), s);
                        ResponseMessage response = cell.sendCellMessageAndWait(csm);
                        if(response instanceof ErrorMessage) {
                            System.out.println("Unable to add security component.");
                        }
                    }
                }).start();
            }
        }

//        if(cell.getComponent(ContainerComponent.class) == null) {
//            new Thread(new Runnable() {
//                public void run() {
//                    String className = "org.jdesktop.wonderland.modules"
//                                    +  ".containercell.server"
//                                    +  ".ContainerComponentMO";
//                    CellServerComponentMessage cscm =
//                      CellServerComponentMessage.newAddMessage(cell.getCellID(),
//                                                               className);
//
//                    ResponseMessage response = cell.sendCellMessageAndWait(cscm);
//                    if(response instanceof ErrorMessage) {
//
//                        System.out.println("Unable to add container compoent.");
//                    }
//                }
//            }).start();
//        }

    }

}
