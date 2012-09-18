/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
    
    private HUDComponent lastComponent;
    private String urlName;
    private boolean fail = false;
    private static final Logger logger = Logger.getLogger(ShowHUDImageMethod.class.getName());
    public String getDescription() {
        return "Shows an image on the Head's Up Display.\n" +
                "-- usage: ShowHUDImage(\"www.openwonderland.org/image.jpg\");\n" +
                "-- returns a HUDComponent object.";

    }

    public String getFunctionName() {
        return "ShowHUDImage";
    }

    public String getCategory() {
        return "HUD";
    }

    public void setArguments(Object[] args) {
        fail = false;
        urlName = (String)args[0];        
        //component = null;
    }

    public Object returns() {
        if(fail)
            return null;

        return lastComponent;
    }

    public void run() {
        if (fail) {
            return;
        }

        try {
            int responseCode = getResponseCode(urlName);
            logger.warning("Response code for URL: "+responseCode);
            if(getResponseCode(urlName) != 200) {
                logger.warning("Error with URL!");
                return;
            }
            
            final URL url = new URL(urlName);
            
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    HUD hud = HUDManagerFactory.getHUDManager().getHUD("main");
                    HUDComponent component = null;
                    ImageIcon i = new ImageIcon(url);

                    component = hud.createImageComponent(i);
                    component.setDecoratable(true);
                    component.setPreferredLocation(Layout.NORTH);

                    hud.addComponent(component);
                    component.setVisible(true);
                    
                    lastComponent = component;
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(ShowHUDImageMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static int getResponseCode(String urlString) throws MalformedURLException, IOException {
        URL u = new URL(urlString);
        HttpURLConnection huc = (HttpURLConnection) u.openConnection();
        huc.setRequestMethod("GET");
        huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");

        huc.connect();
        return huc.getResponseCode();
    }
}
