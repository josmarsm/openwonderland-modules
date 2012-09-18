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
 * Method to add new bounds to a cell. This method was written out of necessity
 * for cells whose developers may not have explicitly set cell bounds. Use of
 * this method will allow for more dynamic proximity event usage as well as a
 * great tool for debugging! 
 */


/**
 * Usage:
 *
 * NewCellBounds(cell, "SPHERE", 5);
 * ...or
 * NewCellBounds(cell, "BOX", 3, 3, 3);
 *
 * @author JagWire
 */
@ScriptMethod
public class NewCellBoundsMethod implements ScriptMethodSPI {

    private Cell cell;
    private String boundsType;
    private float radius; // if spherical
    private float xExtent; //if cubical :o)
    private float yExtent;
    private float zExtent;
    private BoundingVolume volume = null;

    public String getFunctionName() {
        return "NewCellBounds";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        boundsType = (String)args[1];

        Vector3f translation = cell.getLocalTransform().getTranslation(null);
        if(boundsType.equals("SPHERE")) {

            radius = ((Double)args[2]).floatValue();
            volume = new BoundingSphere(radius, translation);

        } else if(boundsType.equals("BOX")) {
            xExtent = ((Double)args[2]).floatValue();
            yExtent = ((Double)args[3]).floatValue();
            zExtent = ((Double)args[4]).floatValue();

            volume = new BoundingBox(translation,
                                     xExtent,
                                     yExtent,
                                     zExtent);
        } else {
            //cover all our bases
            volume = new BoundingSphere();
        }
    }

    public void run() {
        cell.setLocalBounds(volume);
    }

    public String getDescription() {
        return "usage: NewCellBounds(cell, 'Sphere', radius);\n\n"
                +"- creates a new bounding volume for a cell.\n"
                +"- GREAT utility for scripting proximity events.";
    }

    public String getCategory() {
        return "architecture";
    }

}
