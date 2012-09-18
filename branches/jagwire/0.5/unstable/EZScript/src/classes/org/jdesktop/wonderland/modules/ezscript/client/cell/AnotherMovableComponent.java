/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.cell;

import java.math.BigInteger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.ezscript.common.AnotherMovableMessage;

/**
 * @author paulby
 * @author JagWire
 */
public class AnotherMovableComponent extends MovableComponent {
    public AnotherMovableComponent(Cell cell) {
        super(cell);
    }
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
//        super.super.setStatus(status, increasing);
        this.status = status;

        switch (status) {
            case DISK:
                if (msgReceiver != null && channelComp != null) {
                    channelComp.removeMessageReceiver(getMessageClass());
                    msgReceiver = null;
                }
                break;
            case ACTIVE: {
                if (increasing && msgReceiver == null) {
                    msgReceiver = new ChannelComponent.ComponentMessageReceiver() {

                        public void messageReceived(CellMessage message) {
                            // Ignore messages from this client, TODO move this up into addMessageReciever with an option to turn off the test
                            BigInteger senderID = message.getSenderID();
                            if (senderID == null) {
                                senderID = BigInteger.ZERO;
                            }
                            if (!senderID.equals(cell.getCellCache().getSession().getID())) {
                                serverMoveRequest((AnotherMovableMessage) message);
                            }
                        }
                    };
                    channelComp.addMessageReceiver(getMessageClass(), msgReceiver);
                }
            }
        }

    }



    @Override
    public Class getMessageClass() {
        return AnotherMovableMessage.class;
    }

    @Override
    protected CellMessage createMoveRequestMessage(CellTransform transform) {
        return AnotherMovableMessage.anotherNewMoveRequestMessage(cell.getCellID(),
                                                            transform);
    }

    
    protected void serverMoveRequest(AnotherMovableMessage msg) {
        CellTransform transform = msg.getCellTransform();
        applyLocalTransformChange(transform, TransformChangeListener.ChangeSource.REMOTE);
        notifyServerCellMoveListeners(msg, transform, CellMoveSource.REMOTE);
    }

    
    protected void notifyServerCellMoveListeners(AnotherMovableMessage msg,
                                                 CellTransform transform,
                                                 CellMoveSource source) {
        if(serverMoveListeners == null) {
            return;
        }

        for(CellMoveListener listener: serverMoveListeners) {
            listener.cellMoved(transform, source);
        }
    }

    @Override
    public void localMoveRequest(CellTransform transform,
                                 CellMoveModifiedListener listener) {


        localMoveRequest(transform, listener, true);
    }

    public void localMoveRequest(CellTransform transform, boolean synchronize) {
        localMoveRequest(transform, null, synchronize);
    }
    
    /**
     * We need this guy to expose the possibility of moving  and not sending
     * messages to the server.
     *
     * It's ESSENTIAL that the user of this method is aware of the dangers of
     * falling out of sync with the server.
     *
     * @param transform
     * @param listener
     * @param synchronize
     */
    public void localMoveRequest(CellTransform transform,
                                CellMoveModifiedListener listener,
                                boolean synchronize) {

        if (synchronize) {
            // make sure we are connected to the server
            if (channelComp == null
                    || channelComp.getStatus() != ClientConnection.Status.CONNECTED) {
                logger.warning("Cell channel not connected when moving cell "
                        + cell.getCellID());
                return;
            }

            // TODO throttle sends, we should only send so many times a second.
            final CellMessage req = createMoveRequestMessage(transform);
            final ResponseListener resp = createMoveResponseListener(listener);

            throttle.schedule(new Runnable() {

                public void run() {
                    //System.out.println("Sending move at " + System.currentTimeMillis());
                    channelComp.send(req, resp);
                }
            });
        }

        applyLocalTransformChange(transform, TransformChangeListener.ChangeSource.LOCAL);
    }
}
