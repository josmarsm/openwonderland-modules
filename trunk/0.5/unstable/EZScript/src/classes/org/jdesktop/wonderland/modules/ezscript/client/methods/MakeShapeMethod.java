/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.scene.Node;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeUtils;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author Jagwire
 */
@ReturnableScriptMethod
public class MakeShapeMethod implements ReturnableScriptMethodSPI {

    private String shapeType = "";
//    private Node node = null;
    private ShapeViewerEntity entity = null;
    public String getDescription() {
        return "Create a shape primitive to be used in a Scenegraph.\n"
                + "-- usage:var box =  MakeShape('BOX'); ";
    }

    public String getFunctionName() {
        return "MakeShape";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "geometry";
    }

    public void setArguments(Object[] args) {
        //initialize variables first
        shapeType = "";
        shapeType = (String)args[0];
    }

    public Object returns() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return new ShapeViewerEntity(shapeType);
    }

    public void run() {
        //do nothing
    }
    
}
