/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.xmpp_presence.server;

import com.sun.sgs.app.AppContext;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.xmpp_presence.server.service.XMPPPresenceManager;
import org.jdesktop.wonderland.server.ServerPlugin;

/**
 *
 * @author drew
 */
@Plugin
public class XMPPPresenceServerPlugin implements ServerPlugin{

    private static final Logger logger =
            Logger.getLogger(XMPPPresenceServerPlugin.class.getName());


    public void initialize() {
        logger.warning("XMPP Presence Server Plugin initializing.");

        XMPPPresenceManager manager = AppContext.getManager(XMPPPresenceManager.class);
        manager.startPresenceUpdating();
    }

}