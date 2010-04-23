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
package org.jdesktop.wonderland.modules.imager.server;

import java.util.Map;
import org.jdesktop.wonderland.modules.imager.common.ImagerCellServerState;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.imager.common.ImagerCellClientState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * The server-side cell for the simple image viewer
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */

@ExperimentalAPI
public class ImagerCellMO extends CellMO {

    /** The uri of the image */
    private String imageURI;
    
    /** Default constructor, used when the cell is created via WFS */
    public ImagerCellMO() {
        super();
    }

    /**
     * Returns the URI of the image displayed.
     *
     * @return The URI of the image
     */
    public String getImageURI() {
        return imageURI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.imager.client.ImagerCell";
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState clientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (clientState == null) {
            clientState = new ImagerCellClientState();
        }
        ((ImagerCellClientState)clientState).setImageURI(imageURI);
        return super.getClientState(clientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState serverState) {
        super.setServerState(serverState);

        ImagerCellServerState setup = (ImagerCellServerState)serverState;
        imageURI = setup.getImageURI();
    }

    @Override
    public CellServerState getServerState(CellServerState serverState) {
        if (serverState == null) {
            serverState = new ImagerCellServerState();
        }
        ((ImagerCellServerState)serverState).setImageURI(imageURI);
        return super.getServerState(serverState);
    }
}
