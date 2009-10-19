/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.connectionsample.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;

/**
 * A ServerPlugin that registers the ColorChangeConnectionHandler with the
 * CommsManager.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@Plugin
public class ConnectionSampleServerPlugin implements ServerPlugin {
    private static final Logger logger = 
            Logger.getLogger(ConnectionSampleServerPlugin.class.getName());

    /**
     * The initialize method is called when the plugin is installed in the
     * server.  This adds the connection type to the CommsManager, making
     * it available on the server side.
     */
    public void initialize() {
        logger.info("[ConnectionSampleServerPlugin] Registering ColorChangeConnectionHandler");

        // register with the comms manager
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerClientHandler(new ColorChangeConnectionHandler());
    }
}
