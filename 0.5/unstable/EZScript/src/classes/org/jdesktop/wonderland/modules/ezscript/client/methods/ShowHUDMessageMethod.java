/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class ShowHUDMessageMethod implements ReturnableScriptMethodSPI {

    HUD hud;
    HUDComponent component;
    HUDMessagePanel panel;
    String message;
    public String getFunctionName() {
        return "ShowHUDMessage";
    }

    public void setArguments(Object[] args) {
        message = (String)args[0];
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                hud = HUDManagerFactory.getHUDManager().getHUD("main");
                panel = new HUDMessagePanel(message);
                component = hud.createComponent(panel);
                panel.setComponent(component);
                component.setDecoratable(true);
                component.setPreferredLocation(Layout.CENTER);
                
                hud.addComponent(component);
            }
        });

    }

    public void run() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                component.setVisible(true);
            }
        });
    }

    public Object returns() {
        return component;
    }

    public String getDescription() {
        return "usage: var c = ShowHUDMessage(string message)\n\n"
                +"-shows a window on the HUD with the specified string.\n"
                +"-GREAT for simulating conversations onApproach with NPCs\n"
                +"-returns a hud component object.";
                
    }

    public String getCategory() {
        return "HUD";
    }
}
