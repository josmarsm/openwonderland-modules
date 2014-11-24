/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.attachto.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.wonderbuilders.modules.attachto.common.AttachToComponentClientState;
import com.wonderbuilders.modules.attachto.common.AttachToMessage;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.utils.ScenegraphUtils;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * Base class for Attach-To component.
 *
 * @author Abhishek
 */
public class AttachToComponent extends CellComponent implements CellStatusChangeListener {

    private String cellName = "";
    private String nodeName = "";
    private ScheduledExecutorService ses = null;
    private Cell targetCell = null;

    @UsesCellComponent
    protected ChannelComponent channelComp;
    
    public AttachToComponent(Cell cell) {
        super(cell);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {

        if (status == CellStatus.VISIBLE && increasing) {

            // add message receiver for synchronizing state
            ChannelComponent channel = cell.getComponent(ChannelComponent.class);
            channel.addMessageReceiver(AttachToMessage.class, new AttachToMessageReceiver());

            this.targetCell = null;
            if(ses!=null) {
                ses.shutdown();
                ses = null;
            }
            final Object obj = this;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    MovableComponent mc = cell.getComponent(MovableComponent.class);
                    while(mc==null) {
                        /**
                         * hack to get movable component
                         */
                        ClientContextJME.getWorldManager().getRenderManager().setRunning(false);
                        ClientContextJME.getWorldManager().getRenderManager().setRunning(true);
                        
                        mc = cell.getComponent(MovableComponent.class);
                    }
                    Cell targetCell = getTargetCell(cellName, cell.getCellCache().getRootCells());
                    if (targetCell != null) {
                        if (targetCell.getStatus() != CellStatus.VISIBLE) {
                            ClientContextJME.getCellManager()
                                    .addCellStatusChangeListener((CellStatusChangeListener) obj);
                        } else {
                            System.out.println("attaching from status..."+mc);
                            attachTransformChangeListener(getTargetNode(targetCell, nodeName));
                        }
                    } else {
                        ClientContextJME.getCellManager()
                                .addCellStatusChangeListener((CellStatusChangeListener) obj);
                    }
                }
            }).start();
            
        }

        if (status == CellStatus.DISK && !increasing) {
            if (ses != null) {
                ses.shutdown();
                ses = null;
            }
            ChannelComponent channel = cell.getComponent(ChannelComponent.class);
            channel.removeMessageReceiver(AttachToMessage.class);
        }

        super.setStatus(status, increasing);
    }

    private class AttachToMessageReceiver implements ChannelComponent.ComponentMessageReceiver {

        @Override
        public void messageReceived(CellMessage message) {
            AttachToMessage msg = (AttachToMessage) message;
            if (!msg.getSenderID().equals(cell.getCellCache().getSession().getID())) {

            }
        }
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        cellName = ((AttachToComponentClientState) clientState).getCellName();
        nodeName = ((AttachToComponentClientState) clientState).getNodeName();
    }

    public void attachTransformChangeListener(final Node node) {
        if (node != null) {
            if (ses != null) {
                ses.shutdown();
                ses = null;
            }
            ses = Executors.newSingleThreadScheduledExecutor();
            ses.scheduleAtFixedRate(new Runnable() {
                private Vector3f prevTrans = null;

                private void moveBand() {
                    MovableComponent mc = cell.getComponent(MovableComponent.class);
                    CellTransform trans = cell.getLocalTransform();
                    Vector3f pos = node.getWorldTranslation();
                    pos.setY((float) (pos.getY() - 0.01));
                    trans.setTranslation(pos);
                    
                    if(cell.getParent()!=null) {
                        CellTransform parentTrans = cell.getParent().getWorldTransform();
                        CellTransform newTrans = ScenegraphUtils.computeChildTransform(parentTrans
                                , trans);
                        trans.setTranslation(newTrans.getTranslation(prevTrans));
                    }
                    if (mc != null) {
                        //cell.sendCellMessage(new AttachToMessage(trans));
                        mc.localMoveRequest(trans);
                        prevTrans = node.getWorldTranslation();
                    }
                }

                @Override
                public void run() {
                    try {
                        if (prevTrans == null) {
                            prevTrans = node.getWorldTranslation();
                            moveBand();
                        }
                        if (!prevTrans.equals(node.getWorldTranslation())) {
                            moveBand();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 1, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void cellStatusChanged(Cell cell, CellStatus status) {
        if (status == CellStatus.VISIBLE) {
            if (cell.getName().equals(cellName)) {
                if (this.cell.getComponent(MovableComponent.class) == null) {
                    this.cell.addComponent(new MovableComponent(this.cell));
                }
                System.out.println("attaching from cell-status...");
                attachTransformChangeListener(getTargetNode(cell, nodeName));
            }
        }
    }

    private Node getTargetNode(Cell cell, String name) {
        CellRendererJME rend = (CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        RenderComponent rc = rend.getEntity().getComponent(RenderComponent.class);
        return (Node) rc.getSceneRoot().getChild(name);
    }

    private Cell getTargetCell(String name, Collection<Cell> cells) {
        for (Cell cell : cells) {
            if (cell.getName().equals(name)) {
                targetCell = cell;
                return targetCell;
            } else {
                getTargetCell(name, cell.getChildren());
            }
        }
        return targetCell;
    }

}
