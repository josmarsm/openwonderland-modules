/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.attachto.server;

import com.wonderbuilders.modules.attachto.common.AttachToComponentClientState;
import com.wonderbuilders.modules.attachto.common.AttachToComponentServerState;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author Abhishek Upadhyay
 */
public class AttachToComponentMO extends CellComponentMO {

    private String cellName = "";
    private String nodeName = "";
    private CellMO targetCell = null;

    public AttachToComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new AttachToComponentClientState();
        }
        ((AttachToComponentClientState) state).setCellName(cellName);
        ((AttachToComponentClientState) state).setNodeName(nodeName);
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new AttachToComponentServerState();
        }
        ((AttachToComponentServerState) state).setCellName(cellName);
        ((AttachToComponentServerState) state).setNodeName(nodeName);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {
        cellName = ((AttachToComponentServerState) state).getCellName();
        nodeName = ((AttachToComponentServerState) state).getNodeName();
        super.setServerState(state);
    }

    @Override
    protected String getClientClass() {
        return "com.wonderbuilders.modules.attachto.client.AttachToComponent";
    }
    
//    @Override
//    protected void setLive(boolean live) {
//        super.setLive(live);
//        ChannelComponentMO channel = cellRef.get().getComponent(ChannelComponentMO.class);
//        if(live){
//            channel.addMessageReceiver(AttachToMessage.class, new AttachToComponentMessageReceiver(cellRef));
//        } else {
//            channel.removeMessageReceiver(AttachToMessage.class);
//        }
//    }
//    
//    private static class AttachToComponentMessageReceiver extends AbstractComponentMessageReceiver {
//        
//        private ManagedReference<CellMO> cellRef;
//        
//        public AttachToComponentMessageReceiver(ManagedReference<CellMO> cellRef){
//            super(cellRef.get());
//            this.cellRef = cellRef;
//        }
//
//        @Override
//        public synchronized void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
//            AttachToMessage msg = (AttachToMessage)message;
//            // send message back to all client receivers
//            MovableComponentMO mcMO = cellRef.getForUpdate().getComponent(MovableComponentMO.class);
//            mcMO.moveRequest(null, msg.getTransform());
//        }
//        
//    }
}
