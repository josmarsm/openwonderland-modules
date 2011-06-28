/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author Ryan
 */
@ReturnableScriptMethod
public class GetMyUsernameMethod implements ReturnableScriptMethodSPI {

    private String username;
    public String getDescription() {
        return "Get the username of the client running this command.\n" +
                "-- usage: var name = GetMyUsername();";
    }

    public String getFunctionName() {
        return "GetMyUsername";
    }

    public String getCategory() {
        return "utilities";
    }

    public void setArguments(Object[] args) {
        //do nothing
    }

    public Object returns() {
        return username;
    }

    public void run() {
        username = LoginManager.getPrimary().getUsername();
    }

}
