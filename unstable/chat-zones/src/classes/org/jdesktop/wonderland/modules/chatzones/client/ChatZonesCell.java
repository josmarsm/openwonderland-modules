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
import javax.swing.JFrame;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.chatzones.client.jme.cell.ChatZonesCellRenderer;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellChangeMessage;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellChangeMessage.ChatZoneAction;
import org.jdesktop.wonderland.modules.chatzones.common.ChatZonesCellClientState;

public class ChatZonesCell extends Cell {

    private ChatZonesCellRenderer renderer = null;

    private MouseEventListener listener = null;

    private ChatZoneLabelDialog labelDialog = null;

    private static final Logger logger =
            Logger.getLogger(ChatZonesCell.class.getName());

    private int numAvatarsInZone = 0;

    // It's a single space because an empty string causes the label node
    // to barf. TODO fix the label node so it can more gracefully handle
    // empty strings as labels. 
    private String label = " ";

    public ChatZonesCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
  
    }

    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);

        this.numAvatarsInZone = ((ChatZonesCellClientState)state).getNumAvatarsInZone();
        this.label = ((ChatZonesCellClientState)state).getGroup().getLabel();
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        ChannelComponent channel = getComponent(ChannelComponent.class);


        if(status==CellStatus.ACTIVE && increasing) {

            labelDialog = new ChatZoneLabelDialog(this);

            listener = new MouseEventListener(labelDialog);
            listener.addToEntity(renderer.getEntity());


            channel.addMessageReceiver(ChatZonesCellChangeMessage.class, new ChatZonesCellMessageReceiver());
        } else if (status==CellStatus.DISK && !increasing) {
            listener.removeFromEntity(renderer.getEntity());
            listener = null;
            
        } else if (status==CellStatus.RENDERING&& !increasing) {
            // As we're falling down the status chain, try removing the listener
            // earlier. It seems to be gone by the time we get to DISK.
            channel.removeMessageReceiver(ChatZonesCellChangeMessage.class);
        }

    }

    public void setLabel(String newLabel) {
        logger.warning("Setting group label to: " + newLabel);

        this.label = newLabel;
        // Now we send a message to the server with the changed name, and wait
        // for its response before we update anything locally.
        // (no particular resaon not to update, but useful for debugging)
        ChatZonesCellChangeMessage msg = new ChatZonesCellChangeMessage(ChatZoneAction.LABEL);
        msg.setLabel(newLabel);

        this.sendCellMessage(msg);
    }

    public String getLabel() {
        return this.label;
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

    public int getNumAvatarsInZone() {
        return this.numAvatarsInZone;
    }
 
    class ChatZonesCellMessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            ChatZonesCellChangeMessage bsccm = (ChatZonesCellChangeMessage)message;

            switch(bsccm.getAction()) {
                case JOINED:
                    numAvatarsInZone++;

                    logger.warning(bsccm.getName() + " joined the zone.");
                    if(bsccm.getNumAvatarInZone()!=numAvatarsInZone) {
                        logger.warning("avatar count is out of sync with server, syncing: " +bsccm.getNumAvatarInZone() + " -> " + numAvatarsInZone);
                        numAvatarsInZone = bsccm.getNumAvatarInZone();
                    }
                    ClientContextJME.getWorldManager().addRenderUpdater(new SizeRenderUpdater(), null);
                    break;
                case LEFT:
                    numAvatarsInZone--;

                    if(numAvatarsInZone<=0) {
                        logger.warning("Received a LEFT message that would make the total avatars in zone less than zero. Clamping to zero.");
                        numAvatarsInZone = 0;
                    }

                    logger.warning(bsccm.getName() + " left the zone.");
                    if(bsccm.getNumAvatarInZone()!=numAvatarsInZone) {
                        logger.warning("avatar count is out of sync with server, syncing: " +bsccm.getNumAvatarInZone() + " -> " + numAvatarsInZone);
                        numAvatarsInZone = bsccm.getNumAvatarInZone();
                    }
                    ClientContextJME.getWorldManager().addRenderUpdater(new SizeRenderUpdater(), null);
                    break;
                case LABEL:
                    logger.warning("Changed cell name to: " + bsccm.getLabel());
                    label = bsccm.getLabel();
                    renderer.updateLabel();
                    break;
                default:
                    logger.warning("Received unknown message type in client: " + bsccm.getAction());
                    break;
            }

            logger.warning("Received message! Current avatarsInZone: " + numAvatarsInZone + " current label: " + label);
        }
    }

    class MouseEventListener extends EventClassListener {

        private JFrame labelDialog;

        public MouseEventListener (JFrame d) {
            super();

            labelDialog = d;
            setSwingSafe(true);
        }

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseButtonEvent3D.class };
        }

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;

            // Filter out right mouse clicks.
            if(mbe.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
                logger.info("Got click! " + event);
                labelDialog.setVisible(true);
            }
        }

    }

    public class SizeRenderUpdater implements RenderUpdater {

        public void update(Object arg0) {
            logger.warning("In SIZE RENDER UPDATER");
            renderer.updateSize(numAvatarsInZone);
        }

    }
}