/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class WaitMethod implements ScriptMethodSPI {

    int seconds = 0;
    int fps = 30;
    int fpsCounter = 0;
    private Semaphore lock = new Semaphore(0);

    public String getFunctionName() {
        return "Wait";
    }

    @Override
    public void setArguments(Object[] args) {
        //super.setArguments(args);
       seconds = ((Double)args[0]).intValue();
    }

    public String getDescription() {
        return "usage: Wait(10);\n\n"
              +"- blocks the executing thread for the specified amount of seconds";
    }


    public String getCategory() {
        return "utilities";
    }

    public void run() {
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() {
               Timer timer = new Timer(seconds * 1000, new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        lock.release();
                    }
       
                });
                timer.setRepeats(false);
                
                timer.start();
            }
        });

        try {
            lock.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(WaitMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
}
