/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import org.jdesktop.wonderland.modules.ezscript.client.IntStateMachine;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 *
 */
@ReturnableScriptMethod
public class CreateIntStateMachine implements ReturnableScriptMethodSPI {

    public String getFunctionName() {
        return "CreateIntStateMachine";
    }

    public void setArguments(Object[] args) {
        
    }

    public Object returns() {
        return new IntStateMachine();
    }

    public void run() {
       
    }

    public String getDescription() {
        return "usage: CreateIntStateMachine()\n\n"
                +"- creates an IntStateMachine object.\n"
                +"- IntStateMachine objects are not currently used.";
    }

    public String getCategory() {
       return "utilities";
    }


}
