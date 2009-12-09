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
package org.jdesktop.wonderland.modules.proximitytest.client;

import com.jme.bounding.BoundingVolume;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.proximitytest.common.ProximityCellClientState;
import org.jdesktop.wonderland.modules.proximitytest.common.ProximityEnterExitMessage;

/**
 * Client-side cell for proximity test
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProximityCell extends Cell implements ComponentMessageReceiver {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/proximitytest/client/resources/Bundle");

    /* The type of shape: BOX or SPHERE */
    private ProximityCellRenderer cellRenderer = null;

    @UsesCellComponent
    private ProximityComponent prox;

    @UsesCellComponent
    private ChannelComponent channel;

    private List<BoundingVolume> clientBounds;
    private List<BoundingVolume> serverBounds;

    private ClientProximityListener proxListener;

    public ProximityCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Called when the cell is initially created and any time there is a major
     * configuration change. The cell will already be attached to it's parent
     * before the initial call of this method
     * 
     * @param clientState
     */
    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);
        
        clientBounds = ((ProximityCellClientState) clientState).getClientBounds();
        serverBounds = ((ProximityCellClientState) clientState).getServerBounds();
        
        updateListeners();
        
        if (cellRenderer != null) {
            cellRenderer.updateRenderer(clientBounds, serverBounds);
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            cellRenderer = new ProximityCellRenderer(this);
            cellRenderer.updateRenderer(clientBounds, serverBounds);
            return cellRenderer;
        }
        return super.createCellRenderer(rendererType);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        switch (status) {
            case ACTIVE:
                if (increasing) {
                    updateListeners();

                    channel.addMessageReceiver(ProximityEnterExitMessage.class, this);
                } else {
                    channel.removeMessageReceiver(ProximityEnterExitMessage.class);
                }
                break;
            case DISK:
                // TODO cleanup
                break;
        }

    }

    private void updateListeners() {
        if (proxListener != null) {
            prox.removeProximityListener(proxListener);
            proxListener = null;
        }

        if (prox != null && clientBounds != null && clientBounds.size() > 0) {
            proxListener = new ClientProximityListener();
            prox.addProximityListener(proxListener,
                                      clientBounds.toArray(new BoundingVolume[0]));
        }
    }

    public void messageReceived(CellMessage message) {
        ProximityEnterExitMessage m = (ProximityEnterExitMessage) message;
        cellRenderer.setSolid(true, m.getIndex(), !m.isEnter());
    }

    private class ClientProximityListener implements ProximityListener {

        public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID,
                                  BoundingVolume proximityVolume, int proximityIndex)
        {
            String enter = entered?"Entered":"Exited";
            logger.log(Level.WARNING, "[ClientProximityListener] " + enter +
                       " cell " + cell.getCellID() + " volume " + proximityVolume);

            cellRenderer.setSolid(false, proximityIndex, !entered);
        }
    }
}
