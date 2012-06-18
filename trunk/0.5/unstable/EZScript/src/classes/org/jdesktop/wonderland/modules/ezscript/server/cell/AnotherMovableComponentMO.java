package org.jdesktop.wonderland.modules.ezscript.server.cell;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.ezscript.common.AnotherMovableMessage;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;

//ON HOLD FOR NOW.
/**
 * @author paulby
 * @author JagWire
 */
//@ComponentLookupClass(MovableComponentMO.class)
public class AnotherMovableComponentMO extends CellComponentMO {
    
    @UsesCellComponentMO(ChannelComponentMO.class)
    protected ManagedReference<ChannelComponentMO> channelComponentRef = null;
    private static final Logger logger = Logger.getLogger(AnotherMovableComponentMO.class.getName());
    
    public AnotherMovableComponentMO(CellMO cell) {
        super(cell);
    }
    
    @Override
    public void setLive(boolean live) {
        super.setLive(live);
        
        logger.warning("SET LIVE in AnotherMovableComponentMO!");
        
        if (live) {
            channelComponentRef.getForUpdate().addMessageReceiver(getMessageClass(),
                    new ComponentMessageReceiverImpl(this));
        } else {
            channelComponentRef.getForUpdate().removeMessageReceiver(getMessageClass());
        }
    }
    
    @Override
    protected String getClientClass() {
        logger.warning("GetClientClass in AnotherMovableComponentMO!");
        return "org.jdesktop.wonderland.modules.ezscript.client.cell.AnotherMovableComponent";
    }
    
    protected Class getMessageClass() {
        logger.warning("GetMessageClass in AnotherMovableComponentMO!");
        return AnotherMovableMessage.class;
    }
    
    void moveRequest(WonderlandClientID clientID, AnotherMovableMessage msg) {
        moveRequest(clientID, msg.getCellTransform());
    }
    
    public void moveRequest(WonderlandClientID clientID, CellTransform transform) {
        CellMO cell = cellRef.getForUpdate();
        ChannelComponentMO channelComponent;
        //cell.setLocalTransform(transform);

        if (cell.isLive()) {
            channelComponent = channelComponentRef.getForUpdate();
            channelComponent.sendAll(clientID, AnotherMovableMessage.anotherNewMoveRequestMessage(cellID, transform));
            
        }
    }
    
    private static class ComponentMessageReceiverImpl implements ComponentMessageReceiver {
        
        private ManagedReference<AnotherMovableComponentMO> compRef;
        
        public ComponentMessageReceiverImpl(AnotherMovableComponentMO comp) {
            compRef = AppContext.getDataManager().createReference(comp);
        }
        
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            AnotherMovableMessage ent = (AnotherMovableMessage) message;

//            System.out.println("MovableComponentMO.messageReceived "+ent.getActionType());
            switch (ent.getActionType()) {
                case MOVE_REQUEST:
                    // TODO check permisions
                    Logger.getAnonymousLogger().warning("RECEIVED MOVE REQEUST!");
                    compRef.getForUpdate().moveRequest(clientID, ent);

                    // Only need to send a response if the move can not be completed as requested
                    //sender.send(session, MovableMessageResponse.newMoveModifiedMessage(ent.getMessageID(), ent.getTranslation(), ent.getRotation()));
                    break;
                case MOVED:
                    Logger.getAnonymousLogger().severe("Server should never receive MOVED messages");
                    break;
            }
        }

        /**
         * Record the message -- part of the event recording mechanism. Nothing
         * more than the message is recorded in this implementation, delegate it
         * to the recorder manager
         */
        public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            RecorderManager.getDefaultManager().recordMessage(sender, clientID, message);
        }
    }
}
