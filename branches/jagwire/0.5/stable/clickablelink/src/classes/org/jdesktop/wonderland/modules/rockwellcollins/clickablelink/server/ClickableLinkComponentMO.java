/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.common.ClickableLinkComponentClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.common.ClickableLinkComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * This is the server side Component class for the ClickableLink.  This is all
 * pretty standard CellComponentMO code
 * @author Ben (shavnir)
 *
 */
public class ClickableLinkComponentMO extends CellComponentMO {

    /**
     * The URL to link to
     */
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ClickableLinkComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.rockwellcollins.clickablelink.client.ClickableLinkComponent";
    }

    @Override
    public CellComponentClientState getClientState(
            CellComponentClientState state, WonderlandClientID clientID,
            ClientCapabilities capabilities) {
        if (state == null) {
            state = new ClickableLinkComponentClientState();
        }
        ((ClickableLinkComponentClientState) state).setLinkURL(url);
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(
            CellComponentServerState state) {
        if (state == null) {
            state = new ClickableLinkComponentServerState();
        }

        ((ClickableLinkComponentServerState) state).setLinkURL(url);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        url = ((ClickableLinkComponentServerState) state).getLinkURL();
    }
}
