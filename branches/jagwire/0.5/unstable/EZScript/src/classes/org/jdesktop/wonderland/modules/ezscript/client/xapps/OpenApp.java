/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.xapps;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.xappsconfig.client.XAppCellFactory;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class OpenApp implements ScriptMethodSPI {

    private String appID = null;
    public String getFunctionName() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "OpenApp";
    }

    public void setArguments(Object[] args) {
        appID = (String)args[0];
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "Opens an X-Application as identified by the given string.\n"
                + "-- usage: OpenApp('firefox');\n"
                + "-- NOTE: String passed to command should correspond to a"
                + " configured application as set via the web admin page.\n";
                
    }

    public String getCategory() {
        return "applications";
    }

    public void run() {
        try {
            XAppCellFactory factory = new XAppCellFactory(appID, null);
            CellUtils.createCell(factory.getDefaultCellServerState(System.getProperties()));
            
        } catch (CellCreationException ex) {
            Logger.getLogger(OpenApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
