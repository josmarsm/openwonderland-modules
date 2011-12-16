/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.help.WebBrowserLauncher;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author Jagwire
 */

@ScriptMethod
public class OpenExternalBrowserMethod implements ScriptMethodSPI {

    private String url = "http://www.google.com"; 
    public String getFunctionName() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "OpenExternalBrowser";
    }

    public void setArguments(Object[] args) {     
        url = (String)args[0];
    }

    public String getDescription() {
        return "Opens a given URL in an external browser.\n"
                + "-- usage: OpenExternalBrowser(\"http://www.google.com\");";
    }

    public String getCategory() {
        return "utilities";
    }

    public void run() {
        try {
            WebBrowserLauncher.openURL(url);
        } catch (Exception ex) {
            Logger.getLogger(OpenExternalBrowserMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
