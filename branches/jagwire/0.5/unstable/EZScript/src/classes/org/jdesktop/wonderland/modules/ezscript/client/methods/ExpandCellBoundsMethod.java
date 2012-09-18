/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class ExpandCellBoundsMethod implements ScriptMethodSPI {

    int modifier = 0;
    Cell cell;
    public String getFunctionName() {
        return "ExpandCellBounds";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        modifier = ((Double)args[1]).intValue();
    }

    public void run() {
        BoundingVolume volume = cell.getLocalBounds();
        BoundingVolume bounds = null;

        //if bounds are null, create them
        if(volume == null) {
            bounds = new BoundingBox(cell.getLocalTransform().getTranslation(null),
                                    modifier,
                                    modifier,
                                    modifier);
            cell.setLocalBounds(bounds);
            return;
        }

        if(volume instanceof BoundingSphere) {
            BoundingSphere sphere = (BoundingSphere)volume;
            bounds = new BoundingSphere(sphere.radius + modifier,
                                        sphere.getCenter());
            
        } else if(volume instanceof BoundingBox) {
            BoundingBox box = (BoundingBox)volume;
            bounds = new BoundingBox(box.getCenter(),
                                     box.xExtent + modifier,
                                     box.yExtent + modifier,
                                     box.zExtent + modifier);
        } else {
            //do nothing
            return;
        }
        
        cell.setLocalBounds(bounds);
    }

    public String getDescription() {
        return "usage: ExpandCellBounds(cell, integer_modifier)\n\n"
                +"- increases/decreases the bounds of a cell by the specified modifier.";

    }

    public String getCategory() {
        return "architecture";
    }

}
