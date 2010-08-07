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
import com.sun.sgs.app.ManagedReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.messages.OKMessage;
import org.jdesktop.wonderland.modules.programmingdemo.common.CodeCellServerState;
import org.jdesktop.wonderland.modules.programmingdemo.common.SortCellSwapMessage;
import org.jdesktop.wonderland.modules.programmingdemo.common.SortCellClientState;
import org.jdesktop.wonderland.modules.programmingdemo.common.SortCellOrderMessage;
import org.jdesktop.wonderland.modules.programmingdemo.common.SortCellServerState;
import org.jdesktop.wonderland.modules.programmingdemo.common.SortConstants;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapEventSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapListenerSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.CellMOFactory;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.DependsOnCellComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Server cell code for sort cell
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@DependsOnCellComponentMO(CodeTooltipComponentMO.class)
public class SortCellMO extends CellMO implements SharedMapListenerSrv {
    private static final Logger logger =
            Logger.getLogger(SortCellMO.class.getName());

    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> stateRef;

    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelRef;

    private List<Integer> originalOrder;
    private List<Integer> currentOrder;

    public SortCellMO() {
    }
        
    @Override
    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
         return "org.jdesktop.wonderland.modules.programmingdemo.client.SortCell";
    }

    @Override
    public CellClientState getClientState(CellClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new SortCellClientState();
        }
        SortCellClientState sccs = (SortCellClientState) state;
        sccs.setCurrentOrder(currentOrder);

        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new SortCellServerState();
        }
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        // as this cell is created, be sure to also add the corresponding
        // code cell
        SortCellServerState scss = (SortCellServerState) state;
        if (scss.getCodeState() != null) {
            addCodeCell(scss.getCodeState());
        }

        // if the cell is live, re-read values from the shared state
        // component
        if (isLive()) {
            initializeState();
        }
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if (live == true) {
            channel.addMessageReceiver(SortCellSwapMessage.class,
                (ChannelComponentMO.ComponentMessageReceiver) new SortCellMessageReceiver(this));

            initializeState();
        } else {
            channel.removeMessageReceiver(SortCellSwapMessage.class);
        }
    }

    private void initializeState() {
        // initialize the shared state
        SharedMapSrv settings = stateRef.get().get(SortConstants.SETTINGS);
        settings.addSharedMapListener(this);

        SharedInteger itemCount = settings.get(SortConstants.ITEM_COUNT,
                                               SharedInteger.class);
        if (itemCount == null) {
            itemCount = SharedInteger.valueOf(8);
            settings.put(SortConstants.ITEM_COUNT, itemCount);
        }

        SharedString spacing = settings.get(SortConstants.SPACING,
                                            SharedString.class);
        if (spacing == null) {
            spacing = SharedString.valueOf("1");
            settings.put(SortConstants.SPACING, spacing);
        }

        SharedString scale = settings.get(SortConstants.SCALE,
                                          SharedString.class);
        if (scale == null) {
            scale = SharedString.valueOf("0.5");
            settings.put(SortConstants.SCALE, scale);
        }

        SharedInteger color = settings.get(SortConstants.COLOR,
                                           SharedInteger.class);
        if (color == null) {
            color = SharedInteger.valueOf(255 << 16);
            settings.put(SortConstants.COLOR, color);
        }

        if (currentOrder == null || currentOrder.size() != itemCount.getValue()) {
            currentOrder = initializeOrder(itemCount.getValue());
            originalOrder = new ArrayList<Integer>(currentOrder);
        }
    }

    private List<Integer> initializeOrder(int count) {
        List<Integer> out = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            out.add(i);
        }
        Collections.shuffle(out);
        return out;
    }

    public boolean propertyChanged(SharedMapEventSrv smes) {
        if (smes.getPropertyName().equals(SortConstants.ITEM_COUNT)) {
            // the number of items has changed. Reset the objects to
            // be the right size, and notify clients
            int itemCount = ((SharedInteger) smes.getNewValue()).getValue();
            handleReset(itemCount, true);
        } else if (smes.getPropertyName().equals(SortConstants.RESET)) {
            SharedString type = (SharedString) smes.getNewValue();
            boolean random = type.getValue().equals(SortConstants.RANDOM);
            handleReset(random);
            return false;
        }

        return true;
    }

    private void handleSwap(WonderlandClientSender sender,
                            WonderlandClientID id,
                            SortCellSwapMessage message)
    {
        int minIdx = Math.min(message.getIndex1(), message.getIndex2());
        int maxIdx = Math.max(message.getIndex1(), message.getIndex2());

        Integer val1 = currentOrder.get(minIdx);
        Integer val2 = currentOrder.get(maxIdx);

        currentOrder.remove(maxIdx);
        currentOrder.remove(minIdx);

        currentOrder.add(minIdx, val2);
        currentOrder.add(maxIdx, val1);

        // send the requester back an OK so they know the swap happened
        sender.send(id, new OKMessage(message.getMessageID()));

        // send everyone the swap
        channelRef.get().sendAll(id, new SortCellSwapMessage(getCellID(),
                                                             message.getIndex1(),
                                                             message.getIndex2()));
    }

    private void handleReset(boolean random) {
        handleReset(getItemCount(), random);
    }

    private void handleReset(int itemCount, boolean random) {
        if (random) {
            currentOrder = initializeOrder(itemCount);
            originalOrder = new ArrayList<Integer>(currentOrder);
        } else {
            currentOrder = new ArrayList<Integer>(originalOrder);
        }

        // reset the highlights
        SharedMapSrv settings = stateRef.get().get(SortConstants.SETTINGS);
        settings.remove(SortConstants.HIGHLIGHT_ITEMS);

        // send the new order
        sendCellMessage(null, new SortCellOrderMessage(getCellID(), currentOrder));
    }

    private int getItemCount() {
        SharedMapSrv settings = stateRef.get().get(SortConstants.SETTINGS);
        SharedInteger itemCount = settings.get(SortConstants.ITEM_COUNT,
                                               SharedInteger.class);
        return itemCount.getValue();
    }

    private void addCodeCell(CodeCellServerState setup) {
        // fetch the server-side cell class name and create the cell
        String className = setup.getServerClassName();
        CellMO codeMO = CellMOFactory.loadCellMO(className);
        
        // call the cell's setup method
        try {
            codeMO.setServerState(setup);
            addChild(codeMO);
        } catch (ClassCastException cce) {
            logger.log(Level.WARNING, "Error setting up new cell "
                    + codeMO.getName() + " of type "
                    + codeMO.getClass() + ", it does not implement "
                    + "BeanSetupMO.", cce);
            return;
        } catch (MultipleParentException excp) {
            logger.log(Level.WARNING, "Error adding new cell " + codeMO.getName()
                    + " of type " + codeMO.getClass() + ", has multiple parents", excp);
        }
    }

    private static class SortCellMessageReceiver extends AbstractComponentMessageReceiver {
        public SortCellMessageReceiver(SortCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            SortCellMO cellMO = (SortCellMO) getCell();

            if (message instanceof SortCellSwapMessage) {
                cellMO.handleSwap(sender, clientID, (SortCellSwapMessage) message);
            } else {
                logger.warning("Received unknown messge type: " +
                               message.getClass().getName());
            }
        }
    }
}
