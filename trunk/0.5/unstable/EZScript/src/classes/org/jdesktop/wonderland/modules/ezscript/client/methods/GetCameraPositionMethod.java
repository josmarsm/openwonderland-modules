/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author Ryan
 */
@ReturnableScriptMethod
public class GetCameraPositionMethod implements ReturnableScriptMethodSPI {

    private Vector3f position = null;
    public String getDescription() {
        return "Retrieve the x,y, and z coordinates of the current camera.\n"
                + "-- usage: var vec3 = GetCameraPosition();";
    }

    public String getFunctionName() {
        return "GetCameraPosition";
    }

    public String getCategory() {
        return "utilities";
    }

    public void setArguments(Object[] args) {
        //nothing
    }

    public Object returns() {
        return position;
    }

    public void run() {
        position = ClientContextJME.getViewManager().getCameraPosition(null);
    }
    
}
