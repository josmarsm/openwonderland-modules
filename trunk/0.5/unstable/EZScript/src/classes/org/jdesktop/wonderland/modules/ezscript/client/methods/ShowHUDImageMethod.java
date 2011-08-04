/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
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
public class ShowHUDImageMethod implements ReturnableScriptMethodSPI {

    private HUDComponent component;
    private String urlName;
    private boolean fail = false;
    public String getDescription() {
        return "Shows an image on the Head's Up Display.\n" +
                "--usage: ShowHUDImageMethod('www.openwonderland.org/image.jpg');\n" +
                "-- returns a HUDComponent object.";

    }

    public String getFunctionName() {
        return "ShowHUDImage";
    }

    public String getCategory() {
        return "HUD";
    }

    public void setArguments(Object[] args) {
        //throw new UnsupportedOperationException("Not supported yet.");
        urlName = (String)args[0];
    }

    public Object returns() {
        if(fail)
            return null;

        


        return component;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void run() {
        if (fail) {
            return;
        }

        try {
            final URL url =  new URL(urlName);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                        HUD hud = HUDManagerFactory.getHUDManager().getHUD("main");
                        component = hud.createImageComponent(new ImageIcon(url));                        
                        component.setDecoratable(true);
                        component.setPreferredLocation(Layout.CENTER);

                        hud.addComponent(component);
                        component.setVisible(true);
                }                        
            });
      //  } catch (MalformedURLException ex) {
        } catch(Exception ex) {
            Logger.getLogger(ShowHUDImageMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
