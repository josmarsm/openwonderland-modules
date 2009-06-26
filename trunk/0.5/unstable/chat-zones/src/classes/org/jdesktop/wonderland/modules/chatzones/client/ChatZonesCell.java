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

package org.jdesktop.wonderland.modules.chatzones.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.chatzones.client.jme.cell.ChatZonesCellRenderer;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellChangeMessage;

public class ChatZonesCell extends Cell {

    private ChatZonesCellRenderer renderer = null;

    private MouseEventListener listener = null;

    private static final Logger logger =
            Logger.getLogger(ChatZonesCell.class.getName());

    public ChatZonesCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        ChannelComponent channel = getComponent(ChannelComponent.class);

        switch(status) {
            case DISK:
                if(listener != null) {
                    listener.removeFromEntity(renderer.getEntity());
                    listener = null;

                    logger.info("Cell getting set to DISK.");
                }
                channel.removeMessageReceiver(ChatZonesCellChangeMessage.class);
                break;
            case ACTIVE:
                if(listener==null) {
                    listener =  new MouseEventListener();
                    listener.addToEntity(renderer.getEntity());

                    logger.info("Making cell active and added a new listener: " + listener);
                }
                channel.addMessageReceiver(ChatZonesCellChangeMessage.class, new ChatZonesCellMessageReceiver());
                break;

            default:
                break;
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new ChatZonesCellRenderer(this);
            return this.renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    class ChatZonesCellMessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            ChatZonesCellChangeMessage bsccm = (ChatZonesCellChangeMessage)message;

            // Check and see if it's for us? Might need to throw it out.
        }
    }

    class MouseEventListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseButtonEvent3D.class };
        }

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;

            logger.info("Got click! " + event);
        }

    }
}