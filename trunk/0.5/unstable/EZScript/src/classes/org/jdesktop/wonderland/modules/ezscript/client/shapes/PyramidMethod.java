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
public class PyramidMethod implements ReturnableScriptMethodSPI  {

    public String getDescription() {
        return "Create a pyramid object to be used in the scenegraph.\n"
                + "-- usage: var p = PYRAMID();";
    }

    public String getFunctionName() {
        return "PYRAMID";
    }

    public String getCategory() {
        return "geometry";
    }

    public void setArguments(Object[] args) {
    }

    public Object returns() {
        return new ShapeViewerEntity("PYRAMID");
    }

    public void run() {
    }
    
}
