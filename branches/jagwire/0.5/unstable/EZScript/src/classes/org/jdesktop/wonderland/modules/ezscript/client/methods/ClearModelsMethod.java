/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.util.Map;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.cell.CommonCell;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class ClearModelsMethod implements ScriptMethodSPI {

    private CommonCell cell;
    private boolean fail = false;
    public String getFunctionName() {
        return "ClearModels";
    }

    public void setArguments(Object[] args) {
        if(!(args[0] instanceof CommonCell)) {
            fail = true;
            return;
        }
        cell = (CommonCell)args[0];
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Removes all models from a common cell.\n"
                + "-- usage: ClearModels(cell);";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Cells";
    }

    public void run() {
//        throw new UnsupportedOperationException("Not supported yet.");
        Map m = cell.getModels();
        //remove all objects from the cell map.
        for(Object o: m.keySet()) {
            m.remove(o);
            
        }
        //remove all objects from the renderer's map.
        cell.clearRendererModels();
        
        //force the renderer to update the scenegraph.
        cell.forceUpdate();
    }
    
}
