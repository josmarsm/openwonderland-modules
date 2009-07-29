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
package org.jdesktop.wonderland.modules.cmu.client.cell;

import org.jdesktop.wonderland.modules.cmu.common.cell.VisualMessage;
import org.jdesktop.wonderland.modules.cmu.common.cell.TransformationMessage;
import java.io.IOException;
import java.net.UnknownHostException;
import com.jme.scene.Node;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.cmu.client.cell.jme.cellrenderer.CMUCellRenderer;
import org.jdesktop.wonderland.modules.cmu.client.cell.jme.cellrenderer.TransformableParent;
import org.jdesktop.wonderland.modules.cmu.client.cell.jme.cellrenderer.VisualNode;
import org.jdesktop.wonderland.modules.cmu.common.cell.CMUCellChangeMessage;
import org.jdesktop.wonderland.modules.cmu.common.cell.CMUCellClientState;
import wonderland.MirrorWindow;

/**
 * Cell to display and interact with a CMU scene.
 * @author kevin
 */
public class CMUCell extends Cell {

    private CMUCellRenderer renderer;
    private MouseEventListener listener;
    private boolean messageReceiverAdded = false;
    private String cmuURI = null;
    private final TransformableParent sceneRoot = new TransformableParent();
    private final MirrorWindow mirrorWindow = new MirrorWindow();

    private class MouseEventListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }
/*
            CMUCellChangeMessage msg = new CMUCellChangeMessage(getCellID(), getPlaybackSpeed());
            sendCellMessage(msg);
 */
        }

        @Override
        public void computeEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }
/*
            if (program.isPlaying()) {
                program.pause();
            } else {
                program.play();
            }
 * */
        }
    }

    private class CMUCellMessageReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            CMUCellChangeMessage change = (CMUCellChangeMessage) message;
            if (!change.getSenderID().equals(getCellCache().getSession().getID())) {
                //program.setPlaybackSpeed(change.getPlaybackSpeed());
            }
        }
    }

    private class VisualChangeReceiver extends Thread {
        @Override
        public void run() {
            try {
                ObjectInputStream fromServer;
                Socket connection = new Socket("127.0.0.1", 5557);
                fromServer = new ObjectInputStream(connection.getInputStream());
                while (true) {
                    Object received = fromServer.readObject();
                    System.out.println("Received object: " + received);
                    if (TransformationMessage.class.isAssignableFrom(received.getClass())) {
                        sceneRoot.applyTransformationToChild((TransformationMessage)received);
                    }
                    if (VisualMessage.class.isAssignableFrom(received.getClass())) {
                        VisualNode newNode = ((VisualMessage)received).createNode();
                        synchronized(sceneRoot) {
                            sceneRoot.attachChild(newNode);
                            mirrorWindow.addModel(newNode);
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CMUCell.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnknownHostException ex) {
                Logger.getLogger(CMUCell.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CMUCell.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Standard constructor.
     */
    public CMUCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        new VisualChangeReceiver().start();
    }

    public Node getSceneRoot() {
        return this.sceneRoot;
    }

    /**
     * If a URI has been provided, load the given CMU scene.
     * @param configData the config data to initialize the cell with
     */
    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);

        CMUCellClientState cmuClientState = (CMUCellClientState) clientState;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new CMUCellRenderer(this);
            return this.renderer;
        } else {
            return super.createCellRenderer(rendererType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        ChannelComponent channel = getComponent(ChannelComponent.class);
        switch (status) {
            case DISK:
                if (listener != null) {
                    listener.removeFromEntity(renderer.getEntity());
                    listener = null;
                }
                if (messageReceiverAdded) {
                    if (channel != null) {
                        channel.removeMessageReceiver(CMUCellChangeMessage.class);
                    }
                    messageReceiverAdded = false;
                }
                break;

            case ACTIVE:

                if (listener == null) {
                    listener = new MouseEventListener();
                    listener.addToEntity(renderer.getEntity());
                }
                if (!messageReceiverAdded) {
                    if (channel != null) {
                        channel.addMessageReceiver(CMUCellChangeMessage.class, new CMUCellMessageReceiver());
                    }
                    messageReceiverAdded = true;
                }
                break;

            default:
                break;
        }
    }

    //TODO: Show HUD
    protected void showHUD() {
    }

    protected void hideHUD() {
    }
}
