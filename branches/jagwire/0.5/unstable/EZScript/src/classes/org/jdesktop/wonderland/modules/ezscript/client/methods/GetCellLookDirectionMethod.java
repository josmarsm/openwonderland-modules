/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class GetCellLookDirectionMethod implements ScriptMethodSPI {

    private Cell cell = null;
    public String getFunctionName() {
        return "GetCellLookDirection";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
    }

    public String getDescription() {
        return "Prints the look direction of the given cell";
    }

    public String getCategory() {
        return "utilities";
    }

    public void run() {
        Vector3f look = new Vector3f();
        cell.getWorldTransform().getLookAt(new Vector3f(), look);
        System.out.println("Cell: " + cell.getName() + " -> look: " +look);
    }

}
