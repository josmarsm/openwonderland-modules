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
    private float x;
    private float y;
    private float z;
    
    private Quaternion rotation;
    
    public String getFunctionName() {
        return "rotate";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        x = ((Double)args[1]).floatValue();
        y = ((Double)args[2]).floatValue();
        z = ((Double)args[3]).floatValue();
        
        x = (float)Math.toRadians(x);
        y = (float)Math.toRadians(y);
        z = (float)Math.toRadians(z);
        
        
        rotation = new Quaternion(new float[] {x, y, z});
    }

    public String getDescription() {
        return "Immediately rotates an object by the specified quaternion.\n"
                + "-- usage: rotate(cell, rotation);\n";
                
    }

    public String getCategory() {
        return "Object Movement";
    }

    public void run() {
        MovableComponent mc = cell.getComponent(MovableComponent.class);
        CellTransform transform = cell.getLocalTransform();
        transform.setRotation(rotation);
        mc.localMoveRequest(transform);
    }
    
}
