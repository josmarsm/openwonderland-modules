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
package org.jdesktop.wonderland.modules.shape.server;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.shape.common.ShapeCellChangeMessage;
import org.jdesktop.wonderland.modules.shape.common.ShapeCellClientState;
import org.jdesktop.wonderland.modules.shape.common.ShapeCellServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
        
public class ShapeCellMO extends CellMO {

    private String shapeType = null;
    private String textureURI = null;

    public ShapeCellMO() {
    }
        
    @Override
    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
         return "org.jdesktop.wonderland.modules.shape.client.ShapeCell";
    }

    @Override
    public CellClientState getClientState(CellClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new ShapeCellClientState();
        }
        ((ShapeCellClientState)state).setShapeType(shapeType);
        ((ShapeCellClientState)state).setTextureURI(textureURI);
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new ShapeCellServerState();
        }
        ((ShapeCellServerState)state).setShapeType(shapeType);
        ((ShapeCellServerState)state).setTextureURI(textureURI);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        this.shapeType = ((ShapeCellServerState)state).getShapeType();
        this.textureURI = ((ShapeCellServerState)state).getTextureURI();
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if (live == true) {
            channel.addMessageReceiver(ShapeCellChangeMessage.class,
                (ChannelComponentMO.ComponentMessageReceiver)new ShapeCellMessageReceiver(this));
        }
        else {
            channel.removeMessageReceiver(ShapeCellChangeMessage.class);
        }
    }

    private static class ShapeCellMessageReceiver extends AbstractComponentMessageReceiver {
        public ShapeCellMessageReceiver(ShapeCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            ShapeCellChangeMessage sccm = (ShapeCellChangeMessage) message;
            ShapeCellMO cellMO = (ShapeCellMO)getCell();
            cellMO.shapeType = sccm.getShapeType();
            cellMO.sendCellMessage(clientID, message);
        }
    }
}
