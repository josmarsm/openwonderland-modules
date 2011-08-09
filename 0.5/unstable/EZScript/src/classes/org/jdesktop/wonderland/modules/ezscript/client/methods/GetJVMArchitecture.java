/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class GetJVMArchitecture implements ReturnableScriptMethodSPI {

    String model = null;
    public String getDescription() {
        return "Returns the architecture size, either 32 or 64.";
                
    }

    public String getFunctionName() {
        return "GetJVMArchitecture";
    }

    public String getCategory() {
        return "utilities";
    }

    public void setArguments(Object[] args) {
        
    }

    public Object returns() {
        //throw new UnsupportedOperationException("Not supported yet.");
        if(model == null) {
            return "not-available";
        }
        
        return model;
    }

    public void run() {
       model = System.getProperty("sun.arch.data.model");
    }
    
}
