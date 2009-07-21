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

package org.jdesktop.wonderland.modules.pdfspreader.server;

import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellChangeMessage;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellChangeMessage.LayoutType;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellClientState;
import org.jdesktop.wonderland.modules.pdfspreader.common.PDFSpreaderCellServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

public class PDFSpreaderCellMO extends CellMO {

    private static final Logger logger = Logger.getLogger(PDFSpreaderCellMO.class.getName());

    private String pdfURI;

    private float spacing = 4.0f;
    private float scale = 1.0f;
    private LayoutType layout = LayoutType.LINEAR;

    public PDFSpreaderCellMO () {
        super();

        // Need to do this before the Cell goes live.
        // this.setLocalBounds(new BoundingCapsule(new Vector3f(), new LineSegment(new Vector3f(0, 0, -10), new Vector3f(0, 0, 10)), 1));
    }

    @Override
    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.pdfspreader.client.PDFSpreaderCell";
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        this.pdfURI = ((PDFSpreaderCellServerState)state).getSourceURI();
        this.scale = ((PDFSpreaderCellServerState)state).getScale();
        this.spacing = ((PDFSpreaderCellServerState)state).getSpacing();
        this.layout = ((PDFSpreaderCellServerState)state).getLayout();
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new PDFSpreaderCellServerState();
        }

        ((PDFSpreaderCellServerState)state).setSourceURI(pdfURI);
        ((PDFSpreaderCellServerState)state).setScale(scale);
        ((PDFSpreaderCellServerState)state).setSpacing(spacing);
        ((PDFSpreaderCellServerState)state).setLayout(layout);
        
        return super.getServerState(state);
    }


    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new PDFSpreaderCellClientState();

        }

        logger.info("client state requested, sending spacing: " + spacing + "; scale: " + scale + "; layout: " + layout);

        ((PDFSpreaderCellClientState)cellClientState).setPdfURI(pdfURI);
        ((PDFSpreaderCellClientState)cellClientState).setSpacing(spacing);
        ((PDFSpreaderCellClientState)cellClientState).setScale(scale);
        ((PDFSpreaderCellClientState)cellClientState).setLayout(layout);

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    public void setLayout(LayoutType layout) {
        logger.warning("Setting layout to: " + layout);
        this.layout = layout;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        logger.info("Setting PDFSpreaderCellMO live: " + live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if(live) {
            channel.addMessageReceiver(PDFSpreaderCellChangeMessage.class, (ChannelComponentMO.ComponentMessageReceiver)new PDFSpreaderCellMessageReceiver(this));
        }
        else {
            channel.removeMessageReceiver(PDFSpreaderCellChangeMessage.class);
        }
    }
    
    private static class PDFSpreaderCellMessageReceiver extends AbstractComponentMessageReceiver {
        public PDFSpreaderCellMessageReceiver(PDFSpreaderCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            // do something.
            PDFSpreaderCellMO cellMO = (PDFSpreaderCellMO)getCell();

            PDFSpreaderCellChangeMessage msg = (PDFSpreaderCellChangeMessage)message;

            logger.info("Received PDFSpreader message from client: " + msg.getLayout() + "; " + msg.getScale() + "; " + msg.getSpacing());
            // when we get a message, change our internal state and send it back to everyone else.
            cellMO.setSpacing(msg.getSpacing());
            cellMO.setScale(msg.getScale());
            cellMO.setLayout(msg.getLayout());

            // Remove the user who sent it from the list of people to send to.
            Set<WonderlandClientID> clients = sender.getClients();
            clients.remove(clientID);

            sender.send(clients, message);
        }
    }

    /**
     * This event is fired by the ProximityListener when an avatar enters this
     * cell.
     *
     * @param wcid The WonderlandClientID of the avatar that entered the cell.
     */
    public void userEnteredCell(WonderlandClientID wcid) {

    }

    /**
     * This event is fired by the ProximityListener when an avatar leaves this
     * cell.
     *
     * @param wcid The WonderlandClientID of the avatar that entered the cell.
     */
    public void userLeftCell(WonderlandClientID wcid) {

    }
}