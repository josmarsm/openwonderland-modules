/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods.JMEBindings;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class Vector3F implements ReturnableScriptMethodSPI {

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Creates a JME2 Vector3f object.\n"
                + "-- usage: var vec = Vector3f();";
    }

    public String getFunctionName() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Vector3f";
    }

    public String getCategory() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "JME2";
    }

    public void setArguments(Object[] args) {
        //do nothing
    }

    public Object returns() {
        return new Vector3f();
    }

    public void run() {
        //do nothing
    }
    
}
