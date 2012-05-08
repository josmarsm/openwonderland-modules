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
 * @author Ryan
 */
@ReturnableScriptMethod
public class DomeMethod implements ReturnableScriptMethodSPI {

    public String getDescription() {
        return "Create a dome object to be used in the scenegraph.\n"
                + "-- usage: var d = DOME();";
    }

    public String getFunctionName() {
        return "DOME";
    }

    public String getCategory() {
        return "geometry";
    }

    public void setArguments(Object[] args) {
    }

    public Object returns() {
        return new ShapeViewerEntity("DOME");
    }

    public void run() {
    }
    
}
