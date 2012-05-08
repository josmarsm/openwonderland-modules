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
public class CylinderMethod implements ReturnableScriptMethodSPI {

    public String getDescription() {
        return "Create a cylinder object to be used in the scenegraph!"
                + "-- usage: var c = CYLINDER();";
    }

    public String getFunctionName() {
        return "CYLINDER";
    }

    public String getCategory() {
        return "geometry";
    }

    public void setArguments(Object[] args) {
    }

    public Object returns() {
        return new ShapeViewerEntity("CYLINDER");
    }

    public void run() {
    }
    
}
