/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.eventplayer.client;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.eventplayer.common.EventPlayerCellChangeMessage;
import org.jdesktop.wonderland.modules.eventplayer.common.EventPlayerClientState;
import org.jdesktop.wonderland.modules.eventplayer.common.Tape;
import org.jdesktop.wonderland.modules.eventplayer.common.TapeStateMessageResponse;

/**
 *
 * @author Bernard Horan
 * @author Joe Provino
 */
public class EventPlayerCell extends Cell {

    private static final Logger eventPlayerLogger = Logger.getLogger(EventPlayerCell.class.getName());

    @UsesCellComponent private ContextMenuComponent contextComp = null;
    private ContextMenuFactorySPI menuFactory = null;

    private boolean isPlaying;
    private String userName;
    private EventPlayerCellRenderer renderer;
    private DefaultListModel tapeListModel;
    private DefaultListSelectionModel tapeSelectionModel;
    private ReelForm reelForm;

    public EventPlayerCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        isPlaying = false;
        createTapeModels();
        reelForm = new ReelForm(this);
    }

    /**
     * Set the status of this cell
     *
     *
     * Cell states
     *
     * DISK - Cell is on disk with no memory footprint
     * BOUNDS - Cell object is in memory with bounds initialized, NO geometry is loaded
     * INACTIVE - All cell data is in memory
     * ACTIVE - Cell is within the avatars proximity bounds
     * VISIBLE - Cell is in the view frustum
     *
     * The system guarantees that if a change is made between non adjacent status, say from BOUNDS to VISIBLE
     * that setStatus will automatically be called for the intermediate values.
     *
     * If you overload this method in your own class you must call super.setStatus(...) as the first operation
     * in your method.
     *
     * @param status the cell status
     * @return true if the status was changed, false if the new and previous status are the same
     */
    @Override
    public boolean setStatus(CellStatus status) {
        //eventPlayerLogger.info("status: " + status);
        super.setStatus(status);
        if (status == CellStatus.ACTIVE) {
            //About to become visible, so add the message receiver
            getChannel().addMessageReceiver(EventPlayerCellChangeMessage.class, new EventPlayerCellMessageReceiver());
            if (menuFactory == null) {
                final ContextMenuActionListener l = new ContextMenuActionListener() {

                    public void actionPerformed(ContextMenuItemEvent event) {
                        openReelForm();
                    }
                };
                menuFactory = new ContextMenuFactorySPI() {

                    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                        return new ContextMenuItem[]{
                                    new SimpleContextMenuItem("Open Tape...", l)
                                };
                    }
                };
                contextComp.addContextMenuFactory(menuFactory);
            }
        }
        if (status == CellStatus.DISK) {
            //Cleanup
            ChannelComponent channel = getChannel();
            if (channel != null) {
                getChannel().removeMessageReceiver(EventPlayerCellChangeMessage.class);
            }
            if (menuFactory != null) {
                contextComp.removeContextMenuFactory(menuFactory);
                menuFactory = null;
            }
        }
        //No change in my status, so...
        return false;
    }

    /**
     * Called when the cell is initially created and any time there is a
     * major configuration change. The cell will already be attached to its parent
     * before the initial call of this method
     *
     * @param setupData
     */
    @Override
    public void setClientState(CellClientState setupData) {
        super.setClientState(setupData);
        Set<Tape> tapes = ((EventPlayerClientState)setupData).getTapes();
        Tape selectedTape = ((EventPlayerClientState)setupData).getSelectedTape();
        updateTapeModels(tapes, selectedTape);

        isPlaying = ((EventPlayerClientState)setupData).isPlaying();
        userName = ((EventPlayerClientState)setupData).getUserName();
        if(isPlaying) {
            if (userName == null) {
                logger.warning("userName should not be null");
            }
        }
        if (!isPlaying) {
            if (userName != null) {
                logger.warning("userName should be null");
            }
        }
    }

    /**
     * Create the renderer for this cell
     * @param rendererType The type of renderer required
     * @return the renderer for the specified type if available, or null
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            renderer = new EventPlayerCellRenderer(this);
            return renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    private ChannelComponent getChannel() {
        return getComponent(ChannelComponent.class);
    }

    ListModel getTapeListModel() {
        return tapeListModel;
    }

    Set<String> getTapeNames() {
       Enumeration tapes = tapeListModel.elements();
       Set<String> tapeNames = new HashSet<String>();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            tapeNames.add(aTape.getTapeName());
        }
       return tapeNames;
   }

    ListSelectionModel getTapeSelectionModel() {
        return tapeSelectionModel;
    }

    void selectedTapeChanged() {
        eventPlayerLogger.info("selectedTape changed");
        int index = tapeSelectionModel.getMaxSelectionIndex();
        if (index >= 0) {
            Tape selectedTape = (Tape) tapeListModel.elementAt(index);
            logger.info("selected tape: " + selectedTape);
            EventPlayerCellChangeMessage msg = EventPlayerCellChangeMessage.loadRecording(getCellID(), selectedTape.getTapeName());
            getChannel().send(msg);
        }
    }

    private void createTapeModels() {
        tapeListModel = new DefaultListModel();
        tapeSelectionModel = new DefaultListSelectionModel();
        tapeSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    }
    private void updateTapeModels(Set<Tape> tapes, Tape selectedTape) {
        List sortedTapes = new ArrayList(tapes);
        Collections.sort(sortedTapes);
        tapeListModel.clear();
        tapeSelectionModel.clearSelection();
        for (Iterator it = sortedTapes.iterator(); it.hasNext();) {
            tapeListModel.addElement(it.next());
        }
        reelForm.selectTape(selectedTape);
    }

    private void loadRecording(String tapeName) {
        eventPlayerLogger.info("load recording: " + tapeName);
        Enumeration tapes = tapeListModel.elements();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            if (aTape.getTapeName().equals(tapeName)) {
                reelForm.selectTape(aTape);
            }
        }
    }

    

    void startPlaying() {
        logger.info("start playing");

        Tape selectedTape = getSelectedTape();
        if (selectedTape == null) {
            logger.warning("Can't playback when there's no selected tape");
            return;
        }
        if (userName != null) {
            logger.warning("userName should be null");
        }
        userName = getCurrentUserName();
        setPlaying(true);
        EventPlayerCellChangeMessage msg = EventPlayerCellChangeMessage.playRecording(getCellID(), isPlaying, userName);
        getChannel().send(msg);

    }


    void stop() {
        eventPlayerLogger.info("stop");
        if (userName.equals(getCurrentUserName())) {
            EventPlayerCellChangeMessage msg = null;
            if (isPlaying) {
                msg = EventPlayerCellChangeMessage.playRecording(getCellID(), false, userName);
            }
            if (msg != null) {
                getChannel().send(msg);
            }
            setPlaying(false);
        } else {
            eventPlayerLogger.warning("Attempt to stop by non-initiating user");
        }
    }

    private void setPlaying(boolean b) {
        eventPlayerLogger.info("setPlaying: " + b);
        renderer.setPlaying(b);
        isPlaying = b;
    }


    boolean isPlaying() {
        return isPlaying;
    }

    private Tape getSelectedTape() {
       int selectionIndex = tapeSelectionModel.getMaxSelectionIndex();
       if (selectionIndex == -1) {
           return null;
       } else {
           return (Tape) tapeListModel.elementAt(selectionIndex);
       }
   }

     private void selectTape(String tapeName) {
        eventPlayerLogger.info("select tape: " + tapeName);
        Enumeration tapes = tapeListModel.elements();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            if (aTape.getTapeName().equals(tapeName)) {
                reelForm.selectTape(aTape);
            }
        }
    }

    private String getCurrentUserName() {
        return getCellCache().getSession().getUserID().getUsername();
    }

    void openReelForm() {
        //Let the server know I'm selecting a tape and wait to get a message back (processTapeStateMessage())
        EventPlayerCellChangeMessage msg = EventPlayerCellChangeMessage.selectingTape(getCellID());
        try {
            TapeStateMessageResponse response = (TapeStateMessageResponse) getChannel().sendAndWait(msg);
            if (response.getAction() == TapeStateMessageResponse.TapeStateAction.TAPE_STATE) {
                //Need to open the form BEFORE updating models, otherwise ignored
                reelForm.setVisible(true);
                updateTapeModels(response.getTapes(), response.getSelectedTape());
            } else {
                eventPlayerLogger.severe("Failed response from server");
            }
        } catch (InterruptedException ex) {
            eventPlayerLogger.log(Level.SEVERE, "connection with server interrupted", ex);
        }
    }
    

    

    

    class EventPlayerCellMessageReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            EventPlayerCellChangeMessage sccm = (EventPlayerCellChangeMessage) message;
            BigInteger senderID = sccm.getSenderID();
            if (senderID == null) {
                //Broadcast from server
                senderID = BigInteger.ZERO;
            }
            if (!senderID.equals(getCellCache().getSession().getID())) {
                switch (sccm.getAction()) {
                    case PLAY:
                        setPlaying(sccm.isPlaying());
                        userName = sccm.getUserName();
                        break;
                    case LOAD:
                        loadRecording(sccm.getTapeName());
                        break;
                    default:
                        eventPlayerLogger.severe("Unknown action type: " + sccm.getAction());

                }
            }
        }
    }

}

