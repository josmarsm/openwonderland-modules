/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.SPI;

import org.jdesktop.wonderland.modules.ezscript.client.StateMachine;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class CreateStateMachine implements ReturnableScriptMethodSPI {

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
        return new StateMachine();
    }

}
