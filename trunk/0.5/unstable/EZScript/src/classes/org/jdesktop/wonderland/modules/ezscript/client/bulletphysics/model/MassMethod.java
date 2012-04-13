/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.bulletphysics.model;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class MassMethod implements ReturnableScriptMethodSPI {

    private Double _mass;
    public String getDescription() {
        return "Creates a physical object representing mass.";
    }

    public String getFunctionName() {
        return "Mass";
    }

    public String getCategory() {
        return "physics";
    }

    public void setArguments(Object[] args) {
        _mass = (Double)args[0];
    }

    public Object returns() {
        return _mass.floatValue();
    }

    public void run() {
        //do nothing
    }
    
}
