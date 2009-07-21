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
package org.jdesktop.wonderland.modules.cmu.server.cell;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.cmu.common.cell.CMUCellChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.cell.CMUCellClientState;
import org.jdesktop.wonderland.modules.cmu.common.cell.CMUCellServerState;
import org.jdesktop.wonderland.modules.cmu.common.cell.PlaybackDefaults;
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
    private float playbackSpeed = 0;
    private float elapsedTime = 0;
    private long timeOfLastSpeedChange;

    private static class CMUCellMessageReceiver extends AbstractComponentMessageReceiver {

        public CMUCellMessageReceiver(CMUCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            CMUCellMO cellMO = (CMUCellMO) getCell();
            CMUCellChangeMessage change = (CMUCellChangeMessage) message;
            cellMO.setPlaybackSpeed(change.getPlaybackSpeed());
            cellMO.sendCellMessage(clientID, message);
        }
    }

    /** Default constructor. */
    public CMUCellMO() {
        super();
        this.timeOfLastSpeedChange = System.currentTimeMillis();
    }

    /**
     * Returns the URI of the loaded CMU file.
     * @return The URI of the loaded CMU file.
     */
    public String getCmuURI() {
        return cmuURI;
    }

    /**
     * Sets the URI of the CMU file.
     * @param uri The URI of the CMU file.
     */
    public void setCmuURI(String uri) {
        cmuURI = uri;
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        long currentTime = System.currentTimeMillis();
        elapsedTime += this.playbackSpeed * (currentTime - this.timeOfLastSpeedChange);
        this.timeOfLastSpeedChange = currentTime;
        this.playbackSpeed = playbackSpeed;
    }

    /**
     * Get the elapsed time (in milliseconds) that this program has been running,
     * scaled by playback speed.  Each client-side program stores this information
     * anyway; the server's copy can be used for synchronization.
     * @return The elapsed time (in milliseconds) that the program has been running.
     */
    public float getElapsedTime() {
        return this.elapsedTime + ((System.currentTimeMillis() - this.timeOfLastSpeedChange) * this.getPlaybackSpeed());
    }

    public void setElapsedTime(float elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.cmu.client.cell.CMUCell";
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
        cmuClientState.setCmuURI(getCmuURI());
        cmuClientState.setElapsed(getElapsedTime());
        cmuClientState.setPlaybackSpeed(getPlaybackSpeed());

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
            channel.addMessageReceiver(CMUCellChangeMessage.class,
                    (ChannelComponentMO.ComponentMessageReceiver) new CMUCellMessageReceiver(this));
        } else {
            channel.removeMessageReceiver(CMUCellChangeMessage.class);
        }
    }
}
