/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.shapes;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class BoxMethod implements ReturnableScriptMethodSPI {

    public String getDescription() {
        return "Create a box to be used in the scenegraph.\n"
                + "-- usage var b = BOX();";
    }

    public String getFunctionName() {
        return "BOX";
    }

    public String getCategory() {
        return "geometry";
    }

    public void setArguments(Object[] args) {
        //do nothing
    }

    public Object returns() {
        return new ShapeViewerEntity("BOX");
    }

    public void run() {
        //return do nothing
    }
    
}
