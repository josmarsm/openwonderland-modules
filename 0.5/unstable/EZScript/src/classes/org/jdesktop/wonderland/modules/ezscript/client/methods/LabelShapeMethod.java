/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */

@ScriptMethod
public class LabelShapeMethod implements ScriptMethodSPI {

    private String label = null;
    private ShapeViewerEntity entity = null;
    
    public String getFunctionName() {
        return "LabelShape";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        entity = (ShapeViewerEntity)args[0];
        label = (String)args[1];
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Attaches a label to a shape.\n"
                + "-- usage: LabelShape(box, \"Distance = 5\");";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "geometry";
    }

    public void run() {
//        throw new UnsupportedOperationException("Not supported yet.");
        entity.showShape();
        entity.labelShape(label);
    }
    
}
