/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals;

import com.jme.math.Vector3f;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.GlobalSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.Global;

/**
 *
 * @author Ryan
 */
@Global
public class PlayerGlobal implements GlobalSPI {

    public String getName() {
        return "Player";
    }

    public String getUserName() {
        return ClientContextJME.getViewManager().getPrimaryViewCell().getIdentity().getUsername();
    }

    public Vector3f getLocation() {
        ViewCell cell = ClientContextJME.getViewManager().getPrimaryViewCell();

        Vector3f position = new Vector3f();
        Vector3f lookDirection = new Vector3f();
        cell.getLocalTransform().getLookAt(position, lookDirection);
        return position;
    }

    public Vector3f getLookDirection() {
        ViewCell cell = ClientContextJME.getViewManager().getPrimaryViewCell();

        Vector3f position = new Vector3f();
        Vector3f lookDirection = new Vector3f();
        cell.getLocalTransform().getLookAt(position, lookDirection);
        return lookDirection;
    }
}
