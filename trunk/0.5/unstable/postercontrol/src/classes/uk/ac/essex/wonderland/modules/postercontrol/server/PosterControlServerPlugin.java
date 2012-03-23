/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2010, University of Essex, UK, 2010, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package uk.ac.essex.wonderland.modules.postercontrol.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;

/**
 * A ServerPlugin that registers the PosterControlConnectionHandler with the
 * CommsManager.
 * @author Bernard Horan
 */
@Plugin
public class PosterControlServerPlugin implements ServerPlugin {
    private static final Logger logger = 
            Logger.getLogger(PosterControlServerPlugin.class.getName());

    /**
     * The initialize method is called when the plugin is installed in the
     * server.  This adds the connection type to the CommsManager, making
     * it available on the server side.
     */
    public void initialize() {
        logger.info("[PosterControlServerPlugin] Registering PosterControlConnectionHandler");

        // register with the comms manager
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerClientHandler(new PosterControlConnectionHandler());
    }
}
