/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
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
public class CreateCommonCellMethod implements ReturnableScriptMethodSPI {

    private CellID cellID;
    private String name;
    public String getDescription() {
        return "Creates a cell with no renderable geometry. Used for purposes of" +
                "grouping cells together and creating multiple transformable composites.\n" +
                "-- usage: CreateCommonCell();\n" +
                "-- usage: CreateCommonCell(aCellID);\n" +
                "-- usage: CreateCommonCell('mycell');\n" +
                "-- usage: CreateCommonCell(aCellID,'mycell);\n" +                
                "-- automatically adds the EZScript capability to the cell.";
    }

    public String getFunctionName() {
        return "CreateCommonCell";
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
     * @return null
     */
    public Object returns() {
        return null;
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
                CellUtils.createCell(state);
            } else {
                CellUtils.createCell(state, cellID);
            }
            
            
        } catch (CellCreationException ex) {
            Logger.getLogger(CreateCommonCellMethod.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
