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
package org.jdesktop.wonderland.modules.pdfpresentation.server;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.pdfpresentation.common.MovingPlatformCellChangeMessage;
import org.jdesktop.wonderland.modules.pdfpresentation.common.MovingPlatformCellClientState;
import org.jdesktop.wonderland.modules.pdfpresentation.common.MovingPlatformCellServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.MovableComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;



/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */

public class MovingPlatformCellMO extends CellMO {


    private float platformWidth;
    private float platformDepth;

    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.pdfpresentation.client.MovingPlatformCell";
    }

    @UsesCellComponentMO(MovableComponentMO.class)
    private ManagedReference<MovableComponentMO> moveRef;

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        platformWidth = ((MovingPlatformCellServerState)state).getPlatformWidth();
        platformDepth = ((MovingPlatformCellServerState)state).getPlatformDepth();

//        CellTransform transform = new CellTransform(new Quaternion(), ((MovingPlatformCellServerState)state).getTranslation());

//        Logger.getLogger();

//        this.moveRef.get().moveRequest(null, transform);
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if(state==null)
            state = new MovingPlatformCellServerState();

        ((MovingPlatformCellServerState)state).setPlatformWidth(platformWidth);
        ((MovingPlatformCellServerState)state).setPlatformDepth(platformDepth);

        return super.getServerState(state);
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null)
            cellClientState = new MovingPlatformCellClientState();

        ((MovingPlatformCellClientState)cellClientState).setPlatformWidth(platformWidth);
        ((MovingPlatformCellClientState)cellClientState).setPlatformDepth(platformDepth);

        logger.warning("Sending new PLATFORM client state, width/depth: " + platformWidth + "/" + platformDepth);

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public void setLive(boolean live) {
        if(live) {
        BoundingBox box = new BoundingBox(Vector3f.ZERO, this.platformWidth, 20.0f, this.platformDepth);
        this.setLocalBounds(box);
        }
        
        super.setLive(live);
        
        // When we go live, check to see who our parent is.
        // If our parent is a presentationcell, register
        // our existence with said cell.
        if(this.getParent() instanceof PresentationCellMO)
            ((PresentationCellMO)this.getParent()).setPlatformCellMO(this);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if(live) {
            channel.addMessageReceiver(MovingPlatformCellChangeMessage.class, (ChannelComponentMO.ComponentMessageReceiver) new MovingPlatformCellChangeMessageReceiver(this));
        } else {
            channel.removeMessageReceiver(MovingPlatformCellChangeMessage.class);
        }
    }

    public float getPlatformDepth() {
        return platformDepth;
    }

    public void setPlatformDepth(float platformDepth) {
        this.platformDepth = platformDepth;
    }

    public float getPlatformWidth() {
        return platformWidth;
    }

    public void setPlatformWidth(float platformWidth) {
        this.platformWidth = platformWidth;
    }



    private static class MovingPlatformCellChangeMessageReceiver extends AbstractComponentMessageReceiver {
        public MovingPlatformCellChangeMessageReceiver(MovingPlatformCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            MovingPlatformCellMO cellMO = (MovingPlatformCellMO)getCell();

            cellMO.setPlatformDepth(((MovingPlatformCellChangeMessage)message).getPlatformHeight());
            cellMO.setPlatformWidth(((MovingPlatformCellChangeMessage)message).getPlatformWidth());

            // We don't need to do anything more sophisicated than store these
            // values here, because clients will be updating their own platform
            // sizes based on LAYOUT messages on the PresentationCell itself.
            // This is just for keeping the state of the PlatformCellMO up to
            // date for people who come in with the platform already extant.
        }
    }
}