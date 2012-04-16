
package org.jdesktop.wonderland.modules.ezscript.client;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.registry.spi.CellComponentFactorySPI;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.annotation.ContextMenuFactory;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentResponseMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.ezscript.common.EZScriptComponentServerState;

/**
 *
 * @author JagWire
 */
@ContextMenuFactory
public class EZScriptContextMenu implements ContextMenuFactorySPI {

    private ContextMenuItem[] items;
    private MenuListener listener;
    private Logger logger = Logger.getLogger(EZScriptContextMenu.class.getName());
    public EZScriptContextMenu() {
        listener = new MenuListener();
        ContextMenuItem[] is = { new SimpleContextMenuItem("Add EZScript...", listener) };
        items = is;
    }

    public ContextMenuItem[] getContextMenuItems() {

        return items;
    }

    public void addEZScriptToCell(Cell cell) {
        EZScriptComponentFactory factory = new EZScriptComponentFactory();
        EZScriptComponentServerState state = factory.getDefaultCellComponentServerState();

        CellID cellID = cell.getCellID();
        CellServerComponentMessage message =
                CellServerComponentMessage.newAddMessage(cellID, state);
        ResponseMessage response = cell.sendCellMessageAndWait(message);
        if(response == null) {
            //we got some problems
            logger.warning("Received null reply from cell: "+cell.getCellID());
            logger.warning("Unable to add EZScriptComponent.");
            return;
        }

         if(response instanceof ErrorMessage) {
            //something else went wrong.
             logger.warning("Received error message when trying to add EZScriptComponent.\n"
                     +"Error: "+((ErrorMessage)response).getErrorMessage()
                     +"\nCause: "+((ErrorMessage)response).getErrorCause());
        } else {

            while(cell.getComponent(EZScriptComponent.class) == null) {
//                try {
//                    //spin wheels
////                    wait(500);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(EZScriptContextMenu.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
            
            EZScriptComponent c = cell.getComponent(EZScriptComponent.class);
            c.showEditorWindow();
            
        }


    }

    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        if(hasEZScript(event.getPrimaryCell())) {
            
            return new ContextMenuItem[] { };
        }

        return items;
    }

    private boolean hasEZScript(Cell cell) {
        if(cell.getComponent(EZScriptComponent.class) == null)
            return false;

        return true;
    }

//FOR REFERENCE 

//        private void addComponent(CellComponentFactorySPI spi) {
//        // Fetch the default server state for the factory, and cell id. Make
//        // sure we make it dynamically added
//        CellComponentServerState state = spi.getDefaultCellComponentServerState();
//        CellID cellID = selectedCell.getCellID();
//
//        // Send a ADD component message on the cell channel. Wait for a
//        // response. If OK, then update the GUI with the new component.
//        // Otherwise, display an error dialog box.
//        CellServerComponentMessage message =
//                CellServerComponentMessage.newAddMessage(cellID, state);
//        ResponseMessage response = selectedCell.sendCellMessageAndWait(message);
//        if (response == null) {
//            // log and error and post a dialog box
//            LOGGER.warning("Received a null reply from cell with id " +
//                    selectedCell.getCellID() + " with name " +
//                    selectedCell.getName() + " adding component.");
//            return;
//        }
//
//        if (response instanceof CellServerComponentResponseMessage) {
//            // If successful, add the component to the GUI by refreshing the
//            // Cell that is selected.
//            setSelectedCell(selectedCell);
//        }
//        else if (response instanceof ErrorMessage) {
//            // Log an error. Eventually we should display a dialog
//            LOGGER.log(Level.WARNING, "Unable to add component to the server: " +
//                    ((ErrorMessage) response).getErrorMessage(),
//                    ((ErrorMessage) response).getErrorCause());
//        }
//    }

    class MenuListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            if(event.getContextMenuItem().getLabel().equals("Add EZScript...")) {
                logger.warning("Attempting to add EZScriptComponent...");
                addEZScriptToCell(event.getCell());
                
            }
        }
        
    }

    

}
