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
package org.jdesktop.wonderland.modules.telepointer.client.cell;

import java.awt.event.MouseEvent;
import org.jdesktop.wonderland.client.cell.*;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.telepointer.client.cell.TelePointerRenderer.SharedPointer;
import org.jdesktop.wonderland.modules.telepointer.common.cell.messages.TelePointerMessage;

/**
 * A component that provides a shared pointer
 * 
 * @author paulby
 */
@ExperimentalAPI
public class TelePointerComponent extends CellComponent {
    protected static Logger logger = Logger.getLogger(TelePointerComponent.class.getName());

    @UsesCellComponent
    protected ChannelComponent channelComp;
    
    protected ChannelComponent.ComponentMessageReceiver msgReceiver=null;

    private MonitorThread monitorThread;

    private MouseEvent3D lastEvent = null;

    private MouseEvent3DListener listener;

    private SharedPointer pointer;

    private boolean enabled = false;
    
    public TelePointerComponent(Cell cell) {
        super(cell);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled==enabled)
            return;

        this.enabled = enabled;

        if (enabled) {
            listener = new MouseEvent3DListener();
            ClientContext.getInputManager().addGlobalEventListener(listener);
            monitorThread = new MonitorThread();
        } else {
            if (listener!=null) {
                ClientContext.getInputManager().removeGlobalEventListener(listener);
                listener.commitEvent(new FakeExitEvent());
            }
            monitorThread.setDone(true);
            monitorThread = null;
            listener = null;
        }
    }
    
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        switch (status) {
            case DISK:
                if (!increasing) {
                    setEnabled(false);
                    if (msgReceiver != null && channelComp != null) {
                        channelComp.removeMessageReceiver(getMessageClass());
                        msgReceiver = null;
                    }
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
                                handleMessage((TelePointerMessage) message);
                            }
                        }
                    };
                    channelComp.addMessageReceiver(getMessageClass(), msgReceiver);
                }
            }
        }
    }

    private void handleMessage(TelePointerMessage msg) {
        if (pointer==null) {
            pointer = TelePointerRenderer.getSharedPointerRenderer().createSharedPointer(((AvatarCell)cell).getIdentity().getUsername());
        }
        pointer.handleMessage(msg);
    }

    /**
     * @return the class of the message this component handles.
     */
    protected Class getMessageClass() {
        return TelePointerMessage.class;
    }
    


    /**
     * Temporary throttle. TODO we should not have a thread for every TelePointerComponent
     */
    class MonitorThread extends Thread {

        private TelePointerMessage msg;
        private boolean done = false;

        public MonitorThread() {
            msg = new TelePointerMessage();
            setDaemon(true);
            this.start();
        }

        @Override
        public void run() {
            while(!done) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MovableAvatarComponent.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                synchronized(this) {
                    if (lastEvent!=null) {
                        msg.setPointerPosition(lastEvent.getIntersectionPointWorld());
                        msg.setMouseEventID(((MouseEvent)lastEvent.getAwtEvent()).getID());
                        channelComp.send(msg);
                        lastEvent = null;
                        handleMessage(msg);     // Render the local cursor
                    }
                }

            }
        }

        public void setDone(boolean isDone) {
            done = isDone;
        }
    }

    class MouseEvent3DListener extends EventClassFocusListener {
        public Class[] eventClassesToConsume () {
            return new Class[] { MouseEvent3D.class };
        }

        public void commitEvent (Event event) {
            synchronized(monitorThread) {
                // TODO we should not discard enter/exit events
                // Only moves & drags can be merged
                if (lastEvent!=null) {
                    if (((MouseEvent)lastEvent.getAwtEvent()).getID()!=MouseEvent.MOUSE_ENTERED)
                        lastEvent = (MouseEvent3D) event;
                } else {
                    lastEvent = (MouseEvent3D) event;
                }

            }
        }

    }
    
    class FakeExitEvent extends MouseEnterExitEvent3D {
        public FakeExitEvent() {
            super();
            awtEvent = new MouseEvent(new JPanel(), MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), 0, 0,0, 0, false);
        }
    }
}
