/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.SPI;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
//@ScriptMethod
public class WaitMethod extends ProcessorBasedMethod {

    int seconds = 0;
    int fps = 30;
    int fpsCounter = 0;

    public String getFunctionName() {
        return "Wait";
    }

    @Override
    public void setArguments(Object[] args) {
        super.setArguments(args);
       seconds = ((Double)args[1]).intValue();
    }

    public String getDescription() {
        return "usage: Wait(10);\n\n"
              +"- blocks the executing thread for the specified amount of seconds";
    }


    @Override
    public void _compute() {
        fpsCounter += 1;
        //throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public void _commit() {
        //do nothing
    }

    @Override
    public boolean finished() {
        if(fpsCounter >= fps*seconds)
            return true;

        return false;
    }

    public Object returns() {
        return "";
    }

    public String getCategory() {
        return "animation";
    }
}
