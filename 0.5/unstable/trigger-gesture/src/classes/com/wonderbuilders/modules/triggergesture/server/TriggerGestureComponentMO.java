/**
 * Copyright (c) 2013, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.triggergesture.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentClientState;
import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState;
import com.wonderbuilders.modules.triggergesture.common.TriggerGestureComponentServerState.Trigger;
import com.wonderbuilders.modules.triggergesture.common.TriggerGestureMessage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
/**
 * The server-side trigger gesture component.
 * 
 * @author Abhishek Upadhyay.
 */
public class TriggerGestureComponentMO extends CellComponentMO {

    private static final Logger logger = Logger.getLogger(TriggerGestureComponentMO.class.getName());
    private Trigger trigger = Trigger.LEFT_CLICK;
    private String gesture = "Answer Cell";
    private String contextMenuName = "Trigger Gesture";
    private int radius = 3;
    @UsesCellComponentMO(ChannelComponentMO.class)
    protected ManagedReference<ChannelComponentMO> channelComponentRef = null;
    private ManagedReference<CellMO> cellRef = null;
    
    public TriggerGestureComponentMO(CellMO cell) {
        super(cell);
        this.cellRef = AppContext.getDataManager().createReference(cell);
    }
    
    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID,
            ClientCapabilities capabilities) {

        if (state == null) {
            state = new TriggerGestureComponentClientState();
            ((TriggerGestureComponentClientState)state).setTrigger(trigger);
            ((TriggerGestureComponentClientState)state).setGesture(gesture);
            ((TriggerGestureComponentClientState)state).setContextMenuName(contextMenuName);
            ((TriggerGestureComponentClientState)state).setRadius(radius);
        }
   
        return super.getClientState(state, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        
        if (state == null) {
            state = new TriggerGestureComponentServerState();
            ((TriggerGestureComponentServerState)state).setTrigger(trigger);
            ((TriggerGestureComponentServerState)state).setGesture(gesture);
            ((TriggerGestureComponentServerState)state).setContextMenuName(contextMenuName);
            ((TriggerGestureComponentServerState)state).setRadius(radius);
        }
        
        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellComponentServerState state) {
        super.setServerState(state);
        trigger = ((TriggerGestureComponentServerState)state).getTrigger();
        gesture = ((TriggerGestureComponentServerState)state).getGesture();
        contextMenuName = ((TriggerGestureComponentServerState)state).getContextMenuName();
        radius = ((TriggerGestureComponentServerState)state).getRadius();
    }
    
    @Override
    protected String getClientClass() {
        return "com.wonderbuilders.modules.triggergesture.client.TriggerGestureComponent";
    }
    
    @Override
    public void setLive(boolean live) {
        super.setLive(live);
        // Otherwise, either add or remove the message receiver to listen for
        // avatar configuration events
        ChannelComponentMO channel = channelComponentRef.getForUpdate();
        if (live) {
            AvatarNameTagMessageReceiver recv = new AvatarNameTagMessageReceiver(cellRef);
            channel.addMessageReceiver(TriggerGestureMessage.class, recv);
        } else {
            channel.removeMessageReceiver(TriggerGestureMessage.class);
        }
    }
    
    /**
     * Handles messages for the avatar name tag.
     */
    private static class AvatarNameTagMessageReceiver implements ChannelComponentMO.ComponentMessageReceiver {

        private ManagedReference<CellMO> cellRef = null;
        
        public AvatarNameTagMessageReceiver(ManagedReference<CellMO> cellRef) {
            this.cellRef = cellRef;
        }
        
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID
                , CellMessage message) {
            //set cellComp server state.
            logger.log(Level.WARNING, "message received from client...{0}", clientID);
            TriggerGestureComponentMO cellCompMO = cellRef.getForUpdate().getComponent(TriggerGestureComponentMO.class);
            TriggerGestureComponentServerState tgcss = new TriggerGestureComponentServerState();
            tgcss.setContextMenuName(((TriggerGestureMessage)message).getContextMenuName());
            tgcss.setRadius(((TriggerGestureMessage)message).getRadius());
            tgcss.setGesture(((TriggerGestureMessage)message).getGesture());
            tgcss.setTrigger(((TriggerGestureMessage)message).getTrigger());
            cellCompMO.setServerState(tgcss);
            logger.warning("sending message to all clients...");
            cellRef.get().sendCellMessage(clientID, message);
        }

        public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID
                , CellMessage message) {
            //empty
        }
        
    }
    
}
