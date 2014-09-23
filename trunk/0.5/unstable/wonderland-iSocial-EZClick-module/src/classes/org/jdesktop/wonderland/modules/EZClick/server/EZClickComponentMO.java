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

package org.jdesktop.wonderland.modules.EZClick.server;

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.EZClick.common.EZClickComponentClientState;
import org.jdesktop.wonderland.modules.EZClick.common.EZClickComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A sample cell component
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class EZClickComponentMO extends CellComponentMO {

    private static Logger logger = Logger.getLogger(EZClickComponentMO.class.getName());


    
    public EZClickComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.EZClick.client.EZClickComponent";
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new EZClickComponentClientState();
        }

        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new EZClickComponentServerState();
        }

        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);

    }
}
