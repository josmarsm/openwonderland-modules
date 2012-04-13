/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.bulletphysics.model;

import javax.vecmath.Vector3f;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class ImpulseMethod implements ReturnableScriptMethodSPI {
    
    
    private Double x;
    private Double y;
    private Double z;
    
    public String getDescription() {
        return "Create a physical object representing an Impulse.\n"
                + "-- usage: var i = Impulse(1, 0, 1);";
    }

    public String getFunctionName() {
        return "Impulse";
    }

    public String getCategory() {
        return "physics";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        x = (Double)args[0];
        y = (Double)args[1];
        z = (Double)args[2];
        
    }

    public Object returns() {
        return new Vector3f(x.floatValue(),
                            y.floatValue(),
                            z.floatValue());
    }

    public void run() {
        //do nothing
    }
    
}
