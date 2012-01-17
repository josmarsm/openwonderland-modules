
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class ShowShapeMethod implements ScriptMethodSPI {

    private ShapeViewerEntity entity = null;
    private Vector3f position = null;
    public String getFunctionName() {
        return "ShowShape";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        entity = (ShapeViewerEntity)args[0];
        position = (Vector3f)args[1];
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Display contents of a ShapeViewer made from MakeShape() method.\n"
                + "-- usage: ShowShape(box); ";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "geometry";
    }

    public void run() {
//        throw new UnsupportedOperationException("Not supported yet.");
        entity.showShape();
        entity.updateTransform(position, new Quaternion());
    }
    
}
