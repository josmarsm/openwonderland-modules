/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author jagwire
 */
@ScriptMethod
public class CloseHUDWindowMethod implements ScriptMethodSPI {

    private HUDComponent component;
    private boolean fail = false;
    public String getFunctionName() {
        return "CloseHUDWindow";
    }

    public void setArguments(Object[] args) {
        if(!(args[0] instanceof HUDComponent)) {
            fail = true;
            return;
        }
        component = (HUDComponent)args[0];
    }

    public String getDescription() {
        return "Closes a HUD window. Usage: CloseHUDWindow(window);\n"
                + "--window parameter MUST be variable returned from a HUD method.\n"
                + "";
    }

    public String getCategory() {
        return "HUD";
    }

    public void run() {
        if(fail)
            return;
        
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() {
                HUD main = HUDManagerFactory.getHUDManager().getHUD("main");
                component.setVisible(false);
                //main.removeComponent(component);
            }
        });
    }
    
}
