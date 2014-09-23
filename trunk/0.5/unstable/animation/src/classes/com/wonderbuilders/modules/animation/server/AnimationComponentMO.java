/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.animation.server;

import com.wonderbuilders.modules.animation.common.Animation;
import com.wonderbuilders.modules.animation.common.AnimationComponentClientState;
import com.wonderbuilders.modules.animation.common.AnimationComponentMessage;
import com.wonderbuilders.modules.animation.common.AnimationComponentServerState;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Server-side of Animation component.
 * @author Vladimir Djurovic
 */
public class AnimationComponentMO extends CellComponentMO {
    
    /**
     * Current animation state.
     */
    private Animation animation;
    
    /**
     * Creates new instance.
     * 
     * @param cell parent cell
     */
    public AnimationComponentMO(CellMO cell){
        super(cell);
    }

    @Override
    protected String getClientClass() {
        return "com.wonderbuilders.modules.animation.client.AnimationComponent";
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if(state == null){
            state = new AnimationComponentClientState();
            ((AnimationComponentClientState)state).setAnimation(animation);
        }
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if(state == null){
            state = new AnimationComponentServerState();
            ((AnimationComponentServerState)state).setAnimation(animation);
        }
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        animation = ((AnimationComponentServerState)state).getAnimation();
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        ChannelComponentMO channel = cellRef.get().getComponent(ChannelComponentMO.class);
        if(live){
            channel.addMessageReceiver(AnimationComponentMessage.class, new AnimationComponentMessageReceiver(cellRef.get()));
        } else {
            channel.removeMessageReceiver(AnimationComponentMessage.class);
        }
    }
    
    private static class AnimationComponentMessageReceiver 
                            extends AbstractComponentMessageReceiver {
        
        public AnimationComponentMessageReceiver(CellMO cellMo){
            super(cellMo);
        }

        @Override
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            AnimationComponentMessage msg = (AnimationComponentMessage)message;
            // send message back to all client receivers
            getCell().sendCellMessage(clientID, msg);
        }
    }
}
