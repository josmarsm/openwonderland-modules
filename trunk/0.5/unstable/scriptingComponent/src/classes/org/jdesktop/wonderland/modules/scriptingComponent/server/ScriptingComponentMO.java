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
package org.jdesktop.wonderland.modules.scriptingComponent.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentClientState;
import org.jdesktop.wonderland.modules.scriptingComponent.common.ScriptingComponentServerState;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;

/**
 *
 * @author morrisford
 */
public class ScriptingComponentMO extends CellComponentMO 
    {

    private String info = null;

     /**
     * Create a ScriptingComponent for the given cell. The cell must already
     * have a ChannelComponent otherwise this method will throw an IllegalStateException
     * @param cell
     */
    public ScriptingComponentMO(CellMO cell) 
        {
        super(cell); 
        System.out.println("ScriptingComponentMO : In constructor");
        }
    
    @Override
    protected void setLive(boolean live) 
        {
        super.setLive(live);
        System.out.println("ScriptingComponentMO : In setLive = live = " + live);
        }

    @Override
    protected String getClientClass() 
        {
        System.out.println("ScriptingComponentMO : In getClientClass");
        return "org.jdesktop.wonderland.modules.scriptingComponent.client.ScriptingComponent";
        }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) 
        {
        System.out.println("ScriptingComponentMO : In getClientState");

        if (state == null) 
            {
            state = new ScriptingComponentClientState();
            }
        ((ScriptingComponentClientState)state).setInfo(info);
        return super.getClientState(state, clientID, capabilities);
        }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) 
        {
        if (state == null) 
            {
            state = new ScriptingComponentServerState();
            }
        ((ScriptingComponentServerState)state).setInfo(info);
        System.out.println("ScriptingComponentMO : In getServerState");
        return super.getServerState(state);
        }

    @Override
    public void setServerState(CellComponentServerState state) 
        {
        super.setServerState(state);
        info = ((ScriptingComponentServerState)state).getInfo();
        System.out.println("ScriptingComponentMO - : In setServerState");
        }

    /**
     * Set the transform for the cell and notify all client cells of the move.
     * @param sessionID the id of the session that originated the move, or null
     * if the server originated it
     * @param transform
     */
/*
    public void moveRequest(WonderlandClientID clientID, CellTransform transform) {

        CellMO cell = cellRef.getForUpdate();
        ChannelComponentMO channelComponent;
        cell.setLocalTransform(transform);
        
        channelComponent = channelComponentRef.getForUpdate();

        if (cell.isLive()) {
            channelComponent.sendAll(clientID, MovableMessage.newMovedMessage(cell.getCellID(), transform));
        }
    }
*/    
    /**
     * Listener inteface for cell movement
     */
/*
    public interface CellTransformChangeListener extends ManagedObject {
        public void transformChanged(CellMO cell, CellTransform transform);
    }

    private static class ComponentMessageReceiverImpl implements ComponentMessageReceiver {

        private ManagedReference<MovableComponentMO> compRef;
        
        public ComponentMessageReceiverImpl(MovableComponentMO comp) {
            compRef = AppContext.getDataManager().createReference(comp);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            MovableMessage ent = (MovableMessage) message;
            
//            System.out.println("MovableComponentMO.messageReceived "+ent.getActionType());
            switch (ent.getActionType()) {
                case MOVE_REQUEST:
                    // TODO check permisions
                    
                    compRef.getForUpdate().moveRequest(clientID, ent);

                    // Only need to send a response if the move can not be completed as requested
                    //sender.send(session, MovableMessageResponse.newMoveModifiedMessage(ent.getMessageID(), ent.getTranslation(), ent.getRotation()));
                    break;
                case MOVED:
                    Logger.getAnonymousLogger().severe("Server should never receive MOVED messages");
                    break;
            }
        }
*/
         /**
         * Record the message -- part of the event recording mechanism.
         * Nothing more than the message is recorded in this implementation, delegate it to the recorder manager
         */
/*
        public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            RecorderManager.getDefaultManager().recordMessage(sender, clientID, message);
        }
    }
*/
}
