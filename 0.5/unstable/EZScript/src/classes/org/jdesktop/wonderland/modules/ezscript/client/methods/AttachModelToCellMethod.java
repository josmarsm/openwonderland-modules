/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.cell.CommonCell;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class AttachModelToCellMethod implements ScriptMethodSPI {

    private CommonCell cell;
    private String url;
    private String modelID;

    private boolean fail = false;
    private static final Logger logger = Logger.getLogger(AttachModelToCellMethod.class.getName());
    public String getFunctionName() {
        return "AttachModelToCell";
    }

    public void setArguments(Object[] args) {
        //throw new UnsupportedOperationException("Not supported yet.");
        if(!(args[0] instanceof CommonCell)) {
            fail = true;
            return;
        }

        cell = (CommonCell)args[0];
        url = (String)args[1];
        modelID = (String)args[2];
    }

    public String getDescription() {
        return "Attaches a model to a CommonCell instance based on a URL and a model name." +
                "-- usage: AttachModelToCell(cell, 'modelURL', 'MyToyTruck');";
    }

    public String getCategory() {
        return "Cells";
    }

    public void run() {
        if(fail) {
            logger.warning("Failed to attach model. Cell passed to command " +
                    "wasn't a common cell.");
            return;
        }
        
        cell.attachModel(url, modelID);
    }

}
