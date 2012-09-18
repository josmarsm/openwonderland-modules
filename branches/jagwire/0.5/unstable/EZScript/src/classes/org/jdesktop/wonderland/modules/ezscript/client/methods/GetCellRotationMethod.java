/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class GetCellRotationMethod implements ReturnableScriptMethodSPI {

    private Cell cell;
    private CellTransform transform;
    
    public String getDescription() {
        return "Returns the rotation (Quaternion) of the specified cell.\n"
                + "-- Usage: var rot = GetCellRotation(cell);";
    }

    public String getFunctionName() {
        return "GetCellRotation";
    }

    public String getCategory() {
        return "utilities";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
    }

    public Object returns() {
        return transform.getRotation(null);
    }

    public void run() {
        transform = cell.getLocalTransform();
    }
    
}
