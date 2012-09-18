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

package org.jdesktop.wonderland.modules.shader.server;

import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.shader.common.ShaderComponentClientState;
import org.jdesktop.wonderland.modules.shader.common.ShaderComponentServerState;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A sample cell component
 *
 * @author Jordan Slott <jslott@dev.java.net>
 * @author JagWire
 */
public class ShaderComponentMO extends CellComponentMO {

    private static Logger logger = Logger.getLogger(ShaderComponentMO.class.getName());

    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sharedStateComponentRef;
    
    public ShaderComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.shader.client.ShaderCapability";
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
    }


    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new ShaderComponentClientState();
        }
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new ShaderComponentServerState();
        }
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
    }
}
