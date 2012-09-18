/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2012, University of Essex, UK, 2012, All Rights Reserved.
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
package uk.ac.essex.wonderland.modules.countdowntimer.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;
import com.sun.sgs.app.AppContext;


/**
 * A ServerPlugin that registers the CountdownCellConnectionHandler with the
 * CommsManager.
 * @author Bernard Horan
 */
@Plugin
public class CountdownCellServerPlugin implements ServerPlugin {
    private static final Logger logger = 
            Logger.getLogger(CountdownCellServerPlugin.class.getName());
    public static final String COUNTDOWNCELL_CONNECTION_HANDLER_BINDING = "COUNTDOWNCELL_CONNECTION_HANDLER";



    /**
     * The initialize method is called when the plugin is installed in the
     * server.  This adds the connection type to the CommsManager, making
     * it available on the server side.
     */
    public void initialize() {
        logger.info("[CountdownCellServerPlugin] Registering CountdownCellConnectionHandler");
        //Create a new countdowncell connection handler
        CountdownCellConnectionHandler countdownCellConnectionHandler = new CountdownCellConnectionHandler();
        AppContext.getDataManager().setBinding(COUNTDOWNCELL_CONNECTION_HANDLER_BINDING, countdownCellConnectionHandler);

        // register it with the comms manager
        CommsManager cm = WonderlandContext.getCommsManager();
        cm.registerClientHandler(countdownCellConnectionHandler);
    }  
}
