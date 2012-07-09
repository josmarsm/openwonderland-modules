/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.xapps;

import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class TakeControlOfApp implements ScriptMethodSPI {

    public String getFunctionName() {
        return "TakeControlOfApp";
    }

    public void setArguments(Object[] args) {
        new Invoker((App2DCell)args[0]).invoke();
    }

    public String getDescription() {
        return "Takes control of a given application.\n"
                + "-- usage: TakeControlOfApp(cell);";
    }

    public String getCategory() {
        return "applications";
    }

    public void run() {
    }
    
    private static class Invoker {
        private final App2DCell cell;
        public Invoker(App2DCell cell) {
            this.cell = cell;
        }
        
        public void invoke() {
            cell.getApp().getControlArb().takeControl();
        }
    }
    
}
