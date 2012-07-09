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
public class ShowAppInHUD implements ScriptMethodSPI {

    public String getFunctionName() {
        return "ShowAppInHUD";
    }

    public void setArguments(Object[] args) {
        new Invoker((App2DCell)args[0]).invoke();
    }

    public String getDescription() {
        return "Shows the given application in the Head's Up Display.\n"
                + "-- usage: ShowAppInHUD(cell);";
    }

    public String getCategory() {
        return "applications";
    }

    public void run() {
        //do nothing
    }
    
    private static class Invoker {
        private final App2DCell cell;
        public Invoker(App2DCell cell) {
            this.cell = cell;
        }
        
        public void invoke() {
            cell.getApp().setShowInHUD(true);
        }
    }
    
}
