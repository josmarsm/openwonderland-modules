/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.ezscript.client.EZScriptComponentFactory;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.cell.CommonCellFactory;
import org.jdesktop.wonderland.modules.ezscript.common.cell.CommonCellServerState;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class CreateEmptyCellMethod implements ReturnableScriptMethodSPI {

    private CellID cellID;
    private String name;
    
    private CellID resultingCellID;
    private static final Logger logger = Logger.getLogger(CreateEmptyCellMethod.class.getName());
    public String getDescription() {
        return "Creates a cell with no renderable geometry. Used for purposes of" +
                "grouping cells together and creating multiple transformable composites.\n" +
                "-- usage: Cell();\n" +
                "-- usage: Cell(aCellID);\n" +
                "-- usage: Cell(\"mycell\");\n" +
                "-- usage: Cell(aCellID,\"mycell\");\n" +                
                "-- automatically adds the EZScript capability to the cell.";
    }

    public String getFunctionName() {
        return "Cell";
    }

    public String getCategory() {
        return "utilities";
    }

    public void setArguments(Object[] args) {
        if(args.length == 0)
            return;
        
        if(args.length >= 1) {
            
            if(!(args[0] instanceof String)) 
                cellID = (CellID)args[0];
            else
                name = (String)args[0];
        
        }
        
        if(args.length > 1)
            name = (String)args[1];
    }

    /**
     * This should return the resulting CellID of the cell that gets created.
     * Unsure if the core changes have been incorporated yet.
     * 
     * @return The resulting cell object
     */
    public Object returns() {
        if (resultingCellID == null) {
            logger.warning("RESULTING CELLID IS NULL, RETURNING NULL!");
            return null;
        }

        //get wonderland session
        WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
    
        //use session to get cell cache
        CellCache cellCache = ClientContextJME.getCellCache(session);
        
        //use cell cache to get cell from resultingCellID
        return cellCache.getCell(resultingCellID);
        
    }

    public void run() {
        CommonCellFactory factory = new CommonCellFactory();
        CommonCellServerState state = factory.getDefaultCellServerState(null);
        
        EZScriptComponentFactory ezFactory = new EZScriptComponentFactory();
        if(name != null)
            state.setName(name);
        
        state.addComponentServerState(ezFactory.getDefaultCellComponentServerState());
                
        try {
            if(cellID == null) {
               resultingCellID = CellUtils.createCell(state);
            } else {
                resultingCellID = CellUtils.createCell(state, cellID);
            }
            
            
        } catch (CellCreationException ex) {
            Logger.getLogger(CreateEmptyCellMethod.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
