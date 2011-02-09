/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package uk.ac.essex.wonderland.modules.tightvnccontrol.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;

/**
 * A ServerPlugin that registers the TightVNCViewerConnectionHandler with the
 * CommsManager.
 * @author Bernard Horan
 */
@Plugin
public class TightVNCServerPlugin implements ServerPlugin {
    private static final Logger logger = 
            Logger.getLogger(TightVNCServerPlugin.class.getName());

    /**
     * The initialize method is called when the plugin is installed in the
     * server.  This adds the connection type to the CommsManager, making
     * it available on the server side.
     */
    public void initialize() {
        logger.info("[TightVNCServerPlugin] Registering TightVNCViewerConnectionHandler");

        // register with the comms manager
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerClientHandler(new TightVNCConnectionHandler());
    }
}
