/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.util.logging.Logger;
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

    private HUD hud;
    private HUDComponent component;
    private HUDMessagePanel panel;
    private String message;
    private static final Logger logger = 
                    Logger.getLogger(ShowHUDMessageMethod.class.getName());
    
    /** optional */
    private float fontSize;
    
    public String getFunctionName() {
        return "ShowHUDMessage";
    }

    public void setArguments(Object[] args) {
        if (args.length == 2) {
            message = (String) args[0];
            fontSize = ((Double) args[1]).floatValue();

            logger.warning("Casting: "+args[1]+" to: "+fontSize);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    hud = HUDManagerFactory.getHUDManager().getHUD("main");
                    panel = new HUDMessagePanel(message, fontSize);
                    component = hud.createComponent(panel);
                    panel.setComponent(component);
                    component.setDecoratable(true);
                    component.setPreferredLocation(Layout.NORTH);

                    hud.addComponent(component);
                }
            });
        } else {
            message = (String)args[0];
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    hud = HUDManagerFactory.getHUDManager().getHUD("main");
                    panel = new HUDMessagePanel(message);
                    component = hud.createComponent(panel);
                    panel.setComponent(component);
                    component.setDecoratable(true);
                    component.setPreferredLocation(Layout.NORTH);

                    hud.addComponent(component);
                }
            });
            
        }        
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
        return "usage: var c = ShowHUDMessage(message)\n"
                + "usage: var c = ShowHUDMessage(message, fontSize)\n\n"
                +"-shows a window on the HUD with the specified string.\n"
                +"-GREAT for simulating conversations onApproach with NPCs\n"
                +"-returns a hud component object.";
                
    }

    public String getCategory() {
        return "HUD";
    }
}
