/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.bestview.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.bestview.common.BestViewClientState;
import org.jdesktop.wonderland.modules.bestview.common.BestViewServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * Server side cell component for best view
 */
public class BestViewComponentMO extends CellComponentMO {
    private static final Logger LOGGER =
            Logger.getLogger(BestViewComponentMO.class.getName());
   
    public BestViewComponentMO(CellMO cellMO) {
        super (cellMO);
    }

    /**
     * Get the class name of the client CellComponent to instantiate.
     */
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.bestview.client.BestViewComponent";
    }

    /**
     * Get the client state for this component
     */
    @Override
    public CellComponentClientState getClientState(CellComponentClientState state,
                                                   WonderlandClientID clientID,
                                                   ClientCapabilities capabilities)
    {
        // if an existing state is not passed in from a subclass, create one
        // ourselves
        if (state == null) {
            state = new BestViewClientState();
        }

        // do any configuration necessary
        //((BestViewClientState) state).setXXX();

        // pass the state we created up to the superclass to add any other
        // necessary properties
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * Get the server state for this component
     */
    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        // if an existing state is not passed in from a subclass, create one
        // ourselves
        if (state == null) {
            state = new BestViewServerState();
        }

        // do any configuration necessary
        // ((BestViewServerState) state).setXXX();

        // pass the state we created up to the superclass to add any other
        // necessary properties
        return super.getServerState(state);
    }

    /**
     * Handle when the system sets our server state, for example when
     * restoring from WFS
     */
    @Override
    public void setServerState(CellComponentServerState state) {
        // pass the state object to the superclass for further processing
        super.setServerState(state);
    }
}
