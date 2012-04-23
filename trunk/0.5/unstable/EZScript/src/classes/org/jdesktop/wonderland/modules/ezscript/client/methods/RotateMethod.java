/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Quaternion;
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
public class RotateMethod implements ScriptMethodSPI {

    private Cell cell;
    private Quaternion rotation;
    
    public String getFunctionName() {
        return "rotate";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        rotation = (Quaternion)args[1];
    }

    public String getDescription() {
        return "Immediately rotates an object by the specified quaternion.\n"
                + "-- usage: rotate(cell, rotation);\n";
                
    }

    public String getCategory() {
        return "transformation";
    }

    public void run() {
        MovableComponent mc = cell.getComponent(MovableComponent.class);
        CellTransform transform = cell.getLocalTransform();
        transform.setRotation(rotation);
        mc.localMoveRequest(transform);
    }
    
}
