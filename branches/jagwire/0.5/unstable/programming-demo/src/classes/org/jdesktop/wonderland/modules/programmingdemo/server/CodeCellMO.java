/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.programmingdemo.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.modules.appbase.server.cell.App2DCellMO;
import org.jdesktop.wonderland.modules.programmingdemo.common.CodeCellClientState;
import org.jdesktop.wonderland.modules.programmingdemo.common.CodeCellDeleteMessage;
import org.jdesktop.wonderland.modules.programmingdemo.common.CodeCellInsertMessage;
import org.jdesktop.wonderland.modules.programmingdemo.common.CodeCellMessage;
import org.jdesktop.wonderland.modules.programmingdemo.common.CodeCellMultiChangeMessage;
import org.jdesktop.wonderland.modules.programmingdemo.common.CodeCellServerState;
import org.jdesktop.wonderland.modules.programmingdemo.common.CodeConstants;
import org.jdesktop.wonderland.modules.programmingdemo.server.SharedText.AddTransform;
import org.jdesktop.wonderland.modules.programmingdemo.server.SharedText.DeleteTransform;
import org.jdesktop.wonderland.modules.programmingdemo.server.SharedText.MultiTransform;
import org.jdesktop.wonderland.modules.programmingdemo.server.SharedText.OldRevisionException;
import org.jdesktop.wonderland.modules.programmingdemo.server.SharedText.Transform;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Server cell for code viewer
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@ExperimentalAPI
public class CodeCellMO extends App2DCellMO {
    private static final Logger logger =
            Logger.getLogger(CodeCellMO.class.getName());

    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sscRef;

    private SharedText text;

    public CodeCellMO() {
        super();
        addComponent(new SharedStateComponentMO(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.programmingdemo.client.CodeCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        if (live) {
            // make sure we have a text object
            if (text == null) {
                text = new SharedText();
            }

            // get or create the shared maps we use
            SharedMapSrv statusMap = sscRef.get().get(CodeConstants.SETTINGS);
            
            // write the default server state to the map
            initializeState(statusMap);

            // register for messages
            CodeMessageReceiver receiver = new CodeMessageReceiver(this);
            channelRef.get().addMessageReceiver(CodeCellInsertMessage.class, receiver);
            channelRef.get().addMessageReceiver(CodeCellDeleteMessage.class, receiver);
        } else {
            channelRef.get().removeMessageReceiver(CodeCellInsertMessage.class);
            channelRef.get().removeMessageReceiver(CodeCellDeleteMessage.class);
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState cellClientState,
            WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new CodeCellClientState();
        }

        ((CodeCellClientState) cellClientState).setVersion(text.getVersion());
        ((CodeCellClientState) cellClientState).setText(text.getText());
       
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new CodeCellServerState();
        }

        ((CodeCellServerState) state).setText(text.getText());

        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        text = new SharedText(((CodeCellServerState) state).getText());
    }

    private void initializeState(SharedMapSrv map) {
        if (map.get(CodeConstants.PREF_WIDTH) == null) {
            map.put(CodeConstants.PREF_WIDTH,
                    SharedInteger.valueOf(CodeConstants.DEFAULT_WIDTH));
        }

        if (map.get(CodeConstants.PREF_HEIGHT) == null) {
            map.put(CodeConstants.PREF_HEIGHT,
                    SharedInteger.valueOf(CodeConstants.DEFAULT_HEIGHT));
        }

        if (map.get(CodeConstants.DECORATED) == null) {
            map.put(CodeConstants.DECORATED,
                    SharedBoolean.valueOf(true));
        }
    }

    private void handleInsert(WonderlandClientID clientID, 
                              WonderlandClientSender sender,
                              CodeCellInsertMessage insert)
    {
        Transform add = new AddTransform(insert.getInsertionPoint(),
                                         insert.getText());

        try {
            Transform toSend = text.apply(clientID, insert.getVersion(), add);
            channelRef.get().sendAll(clientID, getUpdateMessage(toSend));
        } catch (OldRevisionException ore) {
            logger.log(Level.WARNING, "Old revision detected", ore);
            sender.send(clientID, new ErrorMessage(insert.getMessageID(), ore));
        }
    }

    private void handleDelete(WonderlandClientID clientID, 
                              WonderlandClientSender sender,
                              CodeCellDeleteMessage delete)
    {
        Transform del = new DeleteTransform(delete.getDeletionPoint(),
                                            delete.getLength());

        try {
            Transform toSend = text.apply(clientID, delete.getVersion(), del);
            channelRef.get().sendAll(clientID, getUpdateMessage(toSend));
        } catch (OldRevisionException ore) {
            logger.log(Level.WARNING, "Old revision detected", ore);
            sender.send(clientID, new ErrorMessage(delete.getMessageID(), ore));
        }
    }

    private CodeCellMessage getUpdateMessage(Transform transform) {
        if (transform instanceof AddTransform) {
            AddTransform add = (AddTransform) transform;
            return new CodeCellInsertMessage(cellID, text.getVersion(),
                                             add.getInsertionPoint(),
                                             add.getText());
        } else if (transform instanceof DeleteTransform) {
            DeleteTransform delete = (DeleteTransform) transform;
            return new CodeCellDeleteMessage(cellID, text.getVersion(),
                                             delete.getDeletionPoint(),
                                             delete.getLength());
        } else if (transform instanceof MultiTransform) {
            MultiTransform multi = (MultiTransform) transform;
            CodeCellMultiChangeMessage out =
                    new CodeCellMultiChangeMessage(cellID, text.getVersion());
            for (Transform t : multi.getTransforms()) {
                out.getMessages().add(getUpdateMessage(t));
            }
            return out;
        } else {
            throw new IllegalArgumentException("Unexpected transform: " +
                                               transform.getClass().getName());
        }
    }

    private static class CodeMessageReceiver extends AbstractComponentMessageReceiver {
        public CodeMessageReceiver(CellMO cellMO) {
            super (cellMO);
        }

        @Override
        public void messageReceived(WonderlandClientSender sender,
                                    WonderlandClientID clientID,
                                    CellMessage message)
        {
            if (message instanceof CodeCellInsertMessage) {
                ((CodeCellMO) getCell()).handleInsert(clientID, sender,
                                                      (CodeCellInsertMessage) message);
            } else if (message instanceof CodeCellDeleteMessage) {
                ((CodeCellMO) getCell()).handleDelete(clientID, sender,
                                                      (CodeCellDeleteMessage) message);
            } else {
                logger.warning("Unexpected message type: " + 
                               message.getClass().getName());
            }
        }
    }
}
