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
package org.jdesktop.wonderland.modules.poster.server;

import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.modules.poster.common.PosterCellServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A "generic" server-side Cell that works with a developer-defined client-side
 * Cell. This Cell is meant to be the most simple implementation of a Cell, so
 * that developers do not need to develop any server-side facilities or the
 * communication mechanism (client and server state) necessary for the Cell
 * infrastructure.
 * <p>
 * This class adds a "shared state" component which can be used by the client-
 * side Cell code to synchronize the state of the Cell across clients.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class PosterCellMO extends CellMO {

    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sharedStateCompRef;

    /** Default constructor */
    public PosterCellMO() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        //eventPlayerLogger.fine("Getting client cell class name");
        return "org.jdesktop.wonderland.modules.poster.client.PosterCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new PosterCellServerState();
        }
        return super.getServerState(state);
    }
}
