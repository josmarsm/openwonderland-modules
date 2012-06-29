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
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class ShowHUDMessageMethod implements ScriptMethodSPI {

    private static final Logger logger =
            Logger.getLogger(ShowHUDMessageMethod.class.getName());


    public String getFunctionName() {
        return "ShowHUDMessage";
    }

    public void setArguments(Object[] args) {
        if(args.length == 2) {
            new HUDWindowInvoker(((String)args[0]),
                                 ((Double)args[2]).floatValue())
                                                                .invoke();
        } else {
            new HUDWindowInvoker(((String)args[0])).invoke();
        }
    }

    public void run() {

    }

    public String getDescription() {
        return "usage: var c = ShowHUDMessage(message)\n"
                + "usage: var c = ShowHUDMessage(message, fontSize)\n\n"
                + "-shows a window on the HUD with the specified string.\n"
                + "-GREAT for simulating conversations onApproach with NPCs\n"
                + "-returns a hud component object.";

    }

    public String getCategory() {
        return "HUD";
    }

    private static HUD hud() {
        return HUDManagerFactory.getHUDManager().getHUD("main");
    }

    private static class HUDWindowInvoker {

        private final String message;
        private final float fontSize;

        public HUDWindowInvoker(String message) {
            this.message = message;
            fontSize = 0;
        }

        public HUDWindowInvoker(String message, float fontSize) {
            this.message = message;
            this.fontSize = fontSize;
        }

        public void invoke() {

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    HUD hud = hud();
                    HUDMessagePanel panel = createPanel(message, fontSize);
                    HUDComponent hc = hud.createComponent(panel);
                    panel.setComponent(hc);
                    hc.setDecoratable(true);
                    hc.setPreferredLocation(Layout.NORTH);

                    hud.addComponent(hc);

                    hc.setVisible(true);
                }
            });

        }

        private HUDMessagePanel createPanel(String message, float fontSize) {
            if (fontSize == 0) {
                return new HUDMessagePanel(message);

            } else {
                return new HUDMessagePanel(message, fontSize);
            }
        }
    }
}
