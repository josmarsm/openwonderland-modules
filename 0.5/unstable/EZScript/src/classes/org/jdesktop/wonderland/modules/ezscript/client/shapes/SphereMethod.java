/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.shapes;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeUtils;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */

@ReturnableScriptMethod
public class SphereMethod implements ReturnableScriptMethodSPI {

    public String getDescription() {
        return "Create a sphere object to be used in the scenegraph.\n"
                + "-- usage: var s = SPHERE();";
    }

    public String getFunctionName() {
        return "SPHERE";
    }

    public String getCategory() {
        return "geometry";
    }

    public void setArguments(Object[] args) {
    }

    public Object returns() {
        return new ShapeViewerEntity("SPHERE");
    }

    public void run() {
    }
    
}
