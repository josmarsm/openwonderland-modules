/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.StringStateMachine;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class CreateStringStateMachine implements ReturnableScriptMethodSPI {

    public String getFunctionName() {
        return "CreateStateMachine";
    }

    public void setArguments(Object[] args) {
        //nothing much to do here
    }

    public void run() {
        //do nothing here either
    }

    public Object returns() {
        return new StringStateMachine();
    }

    public String getDescription() {
        return "usage: CreateStateMachine()\n\n"
                +"-returns a StringStateMachine object\n"
                +"-StringStateMachine objects are not currently used in production";
    }

    public String getCategory() {
        return "utilities";
    }

}
