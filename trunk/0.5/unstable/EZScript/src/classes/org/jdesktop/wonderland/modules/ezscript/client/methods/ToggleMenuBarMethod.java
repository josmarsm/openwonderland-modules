/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class ToggleMenuBarMethod implements ScriptMethodSPI {

    private JMenuBar menuBar;
    public String getFunctionName() {
        return "ToggleMenuBar";
    }

    public void setArguments(Object[] args) {
        menuBar = ClientContextJME.getClientMain().getFrame().getFrame().getJMenuBar();
        
    }

    public String getDescription() {
        return "Shows/Hides the menubar based upon its current condition.\n" +
                "-- usage: ToggleMenuBar();";
    }

    public String getCategory() {
        return "utilities";
    }

    public void run() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                menuBar.setVisible(!menuBar.isShowing());
            }
        });
    }

}
