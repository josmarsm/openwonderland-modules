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
package org.jdesktop.wonderland.modules.RoundEllie4.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.RoundEllie4.common.RoundEllie4CellClientState;
import org.jdesktop.wonderland.modules.RoundEllie4.common.RoundEllie4CellServerState;
import org.jdesktop.wonderland.modules.scriptingComponent.server.ScriptingComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
        
public class RoundEllie4CellMO extends CellMO {

    private String modelURI = null;

    public RoundEllie4CellMO()
        {
        addComponent(new ScriptingComponentMO(this), ScriptingComponentMO.class);
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientCellClassName(WonderlandClientID clientID,
            ClientCapabilities capabilities) {

         return "org.jdesktop.wonderland.modules.RoundEllie4.client.RoundEllie4Cell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState state,
            WonderlandClientID clientID, ClientCapabilities capabilities) {

        if (state == null) {
            state = new RoundEllie4CellClientState();
        }
        ((RoundEllie4CellClientState)state).setModelURI(modelURI);
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new RoundEllie4CellServerState();
        }
        ((RoundEllie4CellServerState)state).setModelURI(modelURI);
        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        this.modelURI = ((RoundEllie4CellServerState)state).getModelURI();
    }
}
