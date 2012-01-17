
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author Jagwire
 */
@ScriptMethod
public class HideShapeMethod implements ScriptMethodSPI {

    private ShapeViewerEntity entity = null;
    public String getFunctionName() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "HideShape";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        entity = (ShapeViewerEntity)args[0];
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Hide a shape created from MakeShape() command.\n"
                + "-- usage: HideShape(box);";
                
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "geometry";
    }

    public void run() {
//        throw new UnsupportedOperationException("Not supported yet.");
        entity.setVisible(false);
    }
    
}
