/**
 * Open Wonderland
 *
 * Copyright (c) 2011-12, Open Wonderland Foundation, All Rights Reserved
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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.logging.Level;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.modules.phone.common.PhoneCellServerState;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellChangeMessage;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellClientState;
import org.jdesktop.wonderland.modules.webcaster.common.WebcasterCellServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellMOFactory;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * @author Christian O'Connell
 * @author Bernard Horan
 */
public class WebcasterCellMO extends CellMO
{
    private boolean isWebcasting;
    private int streamID;
    
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
            isWebcasting = false;
            WebcasterCellChangeMessage wccm = new WebcasterCellChangeMessage(isWebcasting);
            getChannel().sendAll(null, wccm);
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
        WebcasterCellServerState wcss = (WebcasterCellServerState) state;
        this.streamID = wcss.getStreamID();
        if (wcss.getPhoneCellState() != null) {
            addPhoneCell(wcss.getPhoneCellState());
        }
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

    private CellMO addPhoneCell(PhoneCellServerState setup) {
        CellTransform transform = new CellTransform(new Quaternion(), new Vector3f(0, 0, 0.75f));
        //Set the position
        // Create a position component that will set the initial origin
        PositionComponentServerState position = (PositionComponentServerState)
                setup.getComponentServerState(PositionComponentServerState.class);
        if (position == null) {
            position = new PositionComponentServerState();
            setup.addComponentServerState(position);
        }
        position.setTranslation(transform.getTranslation(null));
        position.setRotation(transform.getRotation(null));
        position.setScaling(transform.getScaling(null));
        // fetch the server-side cell class name and create the cell
        String className = setup.getServerClassName();
        CellMO cellMO = CellMOFactory.loadCellMO(className);

        // call the cell's setup method
        try {

            cellMO.setServerState(setup);
            addChild(cellMO);
        } catch (ClassCastException cce) {
            logger.log(Level.WARNING, "Error setting up new cell "
                    + cellMO.getName() + " of type "
                    + cellMO.getClass() + ", it does not implement "
                    + "BeanSetupMO.", cce);
            return null;
        } catch (MultipleParentException excp) {
            logger.log(Level.WARNING, "Error adding new cell " + cellMO.getName()
                    + " of type " + cellMO.getClass() + ", has multiple parents", excp);
        }
        return cellMO;
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
