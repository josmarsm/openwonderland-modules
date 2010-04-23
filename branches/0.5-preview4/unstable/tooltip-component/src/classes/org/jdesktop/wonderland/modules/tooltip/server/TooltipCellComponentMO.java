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
package org.jdesktop.wonderland.modules.tooltip.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.tooltip.common.TooltipCellComponentClientState;
import org.jdesktop.wonderland.modules.tooltip.common.TooltipCellComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * The server-side Tooltip Cell Component.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class TooltipCellComponentMO extends CellComponentMO {

    // The tooltip text
    private String text = null;

    // The timeout (in milliseconds) to hide the tooltip even if the mouse has
    // not moved. If -1 then no timeout.
    private int timeout = -1;
    
    /**
     * Constructor, takes the CellMO associated with the Cell Component.
     *
     * @param cell The CellMO associated with this component
     */
    public TooltipCellComponentMO(CellMO cell) {
        super(cell);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.tooltip.client" +
               ".TooltipCellComponent";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellComponentClientState getClientState(
            CellComponentClientState state, WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        if (state == null) {
            state = new TooltipCellComponentClientState();
        }
        ((TooltipCellComponentClientState) state).setText(text);
        ((TooltipCellComponentClientState) state).setTimeout(timeout);
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellComponentServerState getServerState(
            CellComponentServerState state) {
        
        if (state == null) {
            state = new TooltipCellComponentServerState();
        }
        ((TooltipCellComponentServerState) state).setText(text);
        ((TooltipCellComponentServerState) state).setTimeout(timeout);
        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        text = ((TooltipCellComponentServerState) state).getText();
        timeout = ((TooltipCellComponentServerState) state).getTimeout();
    }
}
