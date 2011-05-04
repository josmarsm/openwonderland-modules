/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.headless;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author ryan
 */
@ScriptMethod
public class CreateHeadlessConnectionMethod implements ScriptMethodSPI {

    private String username;
    private String serverURL;

    public String getFunctionName() {
       return "CreateHeadlessConnection";
    }

    public void setArguments(Object[] args) {
        username = (String)args[0];
        serverURL = (String)args[1];
    }

    public String getDescription() {
       return "Creates an arbitrary headless client connection to the specified server."
               +"usage: CreateHeadlessConnection('jagwire', 'www.google.com:8080');";

    }

    public String getCategory() {
        return "utilities";
    }

    public void run() {

        new Thread(new Runnable() {
            public void run() {
                Properties ps = new Properties();
                ps.put("serverURL", serverURL);
                Client3DSim client = new Client3DSim();
                try {
                    client.initialize(username, ps, new EZReplySender());
                } catch (ProcessingException ex) {
                    Logger.getLogger(CreateHeadlessConnectionMethod.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }).start();



    }

    class EZReplySender implements ReplySender {
        public void sendReply(TestReply request) {
            //meh
        }
    }

}