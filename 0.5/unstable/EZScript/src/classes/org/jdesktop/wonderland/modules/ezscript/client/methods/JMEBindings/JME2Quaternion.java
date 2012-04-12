/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods.JMEBindings;

import com.jme.math.Quaternion;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author Ryan
 */
@ReturnableScriptMethod
public class JME2Quaternion implements ReturnableScriptMethodSPI {

    public String getDescription() {
        return "Creates and returns a JME2 Quaternion object.\n"
                + "-- usage: var q = Quaternion();";
    }

    public String getFunctionName() {
        return "Quaternion";
    }

    public String getCategory() {
        return "JME2";
    }

    public void setArguments(Object[] args) {
    }

    public Object returns() {
        return new Quaternion();
    }

    public void run() {
        //do nothing
    }
    
}
