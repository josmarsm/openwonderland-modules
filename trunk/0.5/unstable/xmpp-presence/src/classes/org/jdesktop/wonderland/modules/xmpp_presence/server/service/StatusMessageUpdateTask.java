/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.xmpp_presence.server.service;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Task;
import java.io.Serializable;

/**
 *
 * @author drew
 */
public class StatusMessageUpdateTask implements Task, Serializable {

    public void run()
        {
            XMPPPresenceManager manager = AppContext.getManager(XMPPPresenceManager.class);
            manager.updatePresence();
        }
}
