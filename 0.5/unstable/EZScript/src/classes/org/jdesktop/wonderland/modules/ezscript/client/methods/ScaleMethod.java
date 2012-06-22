
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class ScaleMethod implements ScriptMethodSPI {

    private Cell cell;
    private float x;


    public String getFunctionName() {
        return "scale";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        x = ((Double)args[1]).floatValue();

    }

    public void run() {
        System.out.println("scaling cell: "+cell.getClass());
        MovableComponent mc = cell.getComponent(MovableComponent.class);
        CellTransform scale = cell.getLocalTransform();
        scale.setScaling(x);
        mc.localMoveRequest(scale);
    }

    public String getDescription() {
        return "usage: scale(cell, scale);\n\n"
                +"-immediately resize a cell by scale.";
    }

    public String getCategory() {
        return "Object Movement";
    }

}
