/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Vector3f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.utils.ScenegraphUtils;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class GetCellPositionMethod implements ReturnableScriptMethodSPI {

    private Cell cell;
    private CellTransform transform;
    private static final Logger logger = Logger.getLogger(GetCellPositionMethod.class.getName());

    public String getDescription() {
        return "Returns the position (Vector3f) of the specified cell.\n"
                + "-- Usage: var vec3 = GetCellPosition(cell);";
    }

    public String getFunctionName() {
        return "GetCellPosition";
    }

    public String getCategory() {
        return "utilities";
    }

    public void setArguments(Object[] args) {
        //throw new UnsupportedOperationException("Not supported yet.");
        cell = (Cell) args[0];
    }

    public Object returns() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return transform.getTranslation(null);
    }

    public void run() {
        transform = cell.getLocalTransform();
        
        logger.warning("Got cell position: " + transform.getTranslation(null));
    }
}
