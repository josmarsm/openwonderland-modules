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

package org.jdesktop.wonderland.modules.thoughtbubbles.client;

import com.jme.bounding.BoundingVolume;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.thoughtbubbles.common.ThoughtBubblesComponentChangeMessage;

public class ThoughtBubblesCellComponent extends CellComponent implements ProximityListener {

    private MouseEventListener listener = null;

    private static final Logger logger =
            Logger.getLogger(ThoughtBubblesCellComponent.class.getName());

    @UsesCellComponent
    private ChannelComponent channel;

    @UsesCellComponent
    private ProximityComponent prox;



    // It's a single space because an empty string causes the label node
    // to barf. TODO fix the label node so it can more gracefully handle
    // empty strings as labels. 
    private String label = " ";

    public ThoughtBubblesCellComponent(Cell cell) {
        super(cell);
    }

    @Override
    public void setClientState(CellComponentClientState state) {
        super.setClientState(state);
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        // get the activation bounds from the cell we are part of
        BoundingVolume[] bounds = new BoundingVolume[] {
            this.cell.getLocalBounds()
        };

        if(status==CellStatus.ACTIVE && increasing) {

//            listener = new MouseEventListener(labelDialog);
//            listener.addToEntity(renderer.getEntity());

            channel.addMessageReceiver(ThoughtBubblesComponentChangeMessage.class, new ThoughtBubblesCellMessageReceiver());
            prox.addProximityListener(this, bounds);
        } else if (status==CellStatus.DISK && !increasing) {
            
//            listener.removeFromEntity(renderer.getEntity());
//            listener = null;
            
        } else if (status==CellStatus.RENDERING&& !increasing) {
            // As we're falling down the status chain, try removing the listener
            // earlier. It seems to be gone by the time we get to DISK.
            channel.removeMessageReceiver(ThoughtBubblesComponentChangeMessage.class);
            prox.removeProximityListener(this);
        }

    }

    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {
        logger.warning("VIEW ENTERING CELL");
    }
 
    class ThoughtBubblesCellMessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            ThoughtBubblesComponentChangeMessage msg = (ThoughtBubblesComponentChangeMessage)message;

            switch(msg.getAction()) {
                default:
                    logger.warning("Received unknown message type in client: " + msg.getAction());
                    break;
            }
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
            }
        }

    }
}