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
package org.jdesktop.wonderland.modules.telepointer.server.cell;

import org.jdesktop.wonderland.server.cell.*;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.telepointer.common.cell.messages.TelePointerMessage;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author paulby
 */
public class TelePointerComponentMO extends CellComponentMO {

    @UsesCellComponentMO(ChannelComponentMO.class)
    protected ManagedReference<ChannelComponentMO> channelComponentRef = null;
    
    /**
     * @param cell
     */
    public TelePointerComponentMO(CellMO cell) {
        super(cell);        
    }
    
    @Override
    public void setLive(boolean live) {
        if (live) {
            channelComponentRef.getForUpdate().addMessageReceiver(getMessageClass(), new ComponentMessageReceiverImpl(this, channelComponentRef));
        } else {
            channelComponentRef.getForUpdate().removeMessageReceiver(getMessageClass());
        }
    }

    protected Class getMessageClass() {
        return TelePointerMessage.class;
    }
    
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.telepointer.client.cell.TelePointerComponent";
    }


    private static class ComponentMessageReceiverImpl implements ComponentMessageReceiver {

        private ManagedReference<TelePointerComponentMO> compRef;
        private ManagedReference<ChannelComponentMO> channelComponentRef;
        
        public ComponentMessageReceiverImpl(TelePointerComponentMO comp, ManagedReference<ChannelComponentMO> channelComponentRef) {
            compRef = AppContext.getDataManager().createReference(comp);
            this.channelComponentRef = channelComponentRef;
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            TelePointerMessage monitorMessage = (TelePointerMessage) message;
            channelComponentRef.getForUpdate().sendAll(clientID, monitorMessage);
        }

         /**
         * Record the message -- part of the event recording mechanism.
         * Nothing more than the message is recorded in this implementation, delegate it to the recorder manager
         */
        public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            // No need to record these messages
        }
    }
}
