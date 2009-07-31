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
package org.jdesktop.wonderland.modules.cmu.server;

import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.cmu.common.PlaybackSpeedChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellClientState;
import org.jdesktop.wonderland.modules.cmu.common.CMUCellServerState;
import org.jdesktop.wonderland.modules.cmu.common.ConnectionChangeMessage;
import org.jdesktop.wonderland.modules.cmu.player.PlaybackDefaults;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author kevin
 */
public class CMUCellMO extends CellMO {

    private String cmuURI;
    private String server;
    private int port;
    private boolean socketInitialized = false;  // False until a CMU instance informs us with valid socket information.
    private final Serializable socketLock = new String();

    //TODO: expand this for different message types.
    private static class CMUCellMessageReceiver extends AbstractComponentMessageReceiver {

        public CMUCellMessageReceiver(CMUCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            CMUCellMO cellMO = (CMUCellMO) getCell();
            PlaybackSpeedChangeMessage change = (PlaybackSpeedChangeMessage) message;
            cellMO.setPlaybackSpeed(change.getPlaybackSpeed());
            cellMO.sendCellMessage(clientID, message);
        }
    }

    /** Default constructor. */
    public CMUCellMO() {
        super();
    }

    /**
     * Get the URI of the loaded CMU file.
     * @return The URI of the loaded CMU file
     */
    public String getCmuURI() {
        return cmuURI;
    }

    /**
     * Sets the URI of the CMU file.
     * @param uri The URI of the CMU file
     */
    public void setCmuURI(String uri) {
        cmuURI = uri;
    }

    /**
     * Current rate of playback.
     * @return Current playback speed
     */
    public float getPlaybackSpeed() {
        //TODO: do this
        return 0.0f;
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        //TODO: do this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.cmu.client.CMUCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState clientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (clientState == null) {
            clientState = new CMUCellClientState();
        }

        CMUCellClientState cmuClientState = ((CMUCellClientState) clientState);
        cmuClientState.setPlaybackSpeed(getPlaybackSpeed());
        synchronized (socketLock) {
            if (this.socketInitialized) {
                cmuClientState.setServerAndPort(getServer(), getPort());
            }
        }
        
        return super.getClientState(clientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState serverState) {
        super.setServerState(serverState);

        CMUCellServerState setup = (CMUCellServerState) serverState;
        setCmuURI(setup.getCmuURI());
        ProgramConnectionHandlerMO.createProgram(this.getCellID(), this.getCmuURI());
        this.setPlaybackSpeed(PlaybackDefaults.DEFAULT_START_SPEED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState serverState) {
        if (serverState == null) {
            serverState = new CMUCellServerState();
        }
        ((CMUCellServerState) serverState).setCmuURI(getCmuURI());
        return super.getServerState(serverState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if (live == true) {
            channel.addMessageReceiver(PlaybackSpeedChangeMessage.class,
                    (ChannelComponentMO.ComponentMessageReceiver) new CMUCellMessageReceiver(this));
        } else {
            channel.removeMessageReceiver(PlaybackSpeedChangeMessage.class);
        }
    }

    public int getPort() {
        synchronized (socketLock) {
            return port;
        }
    }

    public String getServer() {
        synchronized (socketLock) {
            return server;
        }
    }

    public void setServerAndPort(String server, int port) {
        synchronized (socketLock) {
            this.socketInitialized = true;
            this.server = server;
            this.port = port;
            sendServerInformation();
        }
    }

    private void sendServerInformation() {
        synchronized(socketLock) {
            this.sendCellMessage(null, new ConnectionChangeMessage(this.getCellID(), getServer(), getPort()));
        }
    }
}
