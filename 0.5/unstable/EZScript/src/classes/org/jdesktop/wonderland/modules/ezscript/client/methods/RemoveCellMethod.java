/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */

@ScriptMethod
public class RemoveCellMethod implements ScriptMethodSPI {

    private Cell cell = null;
    
    public String getFunctionName() {
        return "RemoveCell";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        cell = (Cell)args[0];
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Removes a cell from a world.\n"
                + "-- usage: RemoveCell(cell);";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Cells";
    }

    public void run() {
//        throw new UnsupportedOperationException("Not supported yet.");
        CellUtils.deleteCell(cell);
    }
    
}
