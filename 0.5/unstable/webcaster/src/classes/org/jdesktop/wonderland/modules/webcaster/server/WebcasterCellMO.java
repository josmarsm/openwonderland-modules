/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.webcaster.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellChangeMessage;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellClientState;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * @author Christian O'Connell
 * @author Bernard Horan
 */
public class WebcasterCellMO extends CellMO
{
    private transient boolean isWebcasting;
    private String streamID;

    public WebcasterCellMO(){
        super();
        isWebcasting = false;
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        if (live) {
            ChannelComponentMO channel = getChannel();
            if (channel == null) {
                throw new IllegalStateException("Cell does not have a ChannelComponent");
            }
            //Add the message receiver to the channel
            channel.addMessageReceiver(WebcasterCellChangeMessage.class,
                    (ChannelComponentMO.ComponentMessageReceiver) new WebcasterCellMOMessageReceiver(this));
        } else {
            getChannel().removeMessageReceiver(WebcasterCellChangeMessage.class);
        }
    }

    private ChannelComponentMO getChannel() {
        return getComponent(ChannelComponentMO.class);
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities){
        return "org.jdesktop.wonderland.modules.webcaster.client.WebcasterCell";
    }

    @Override
    public void setServerState(CellServerState state){
        super.setServerState(state);
        this.streamID = ((WebcasterCellServerState)state).getStreamID();
    }

    @Override
    public CellServerState getServerState(CellServerState state)
    {
        if (state == null) {
            state = new WebcasterCellServerState();
        }
        
        return super.getServerState(state);
    }

    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities)
    {
        if (cellClientState == null){
            cellClientState = new WebcasterCellClientState(isWebcasting, streamID);
        }

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    private void setWebcasting(boolean isWebcasting) {
        logger.warning("isWebcasting: " + isWebcasting);
        this.isWebcasting = isWebcasting;
    }

    private static class WebcasterCellMOMessageReceiver extends AbstractComponentMessageReceiver {
        public WebcasterCellMOMessageReceiver(WebcasterCellMO cellMO) {
            super(cellMO);
        }
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            WebcasterCellMO cellMO = (WebcasterCellMO)getCell();
            WebcasterCellChangeMessage wccm = (WebcasterCellChangeMessage)message;
            cellMO.setWebcasting(wccm.isWebcasting());
            cellMO.getChannel().sendAll(clientID, wccm);
        }
    }
}
