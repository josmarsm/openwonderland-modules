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
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.eventplayer.common.EventPlayerCellChangeMessage;
import org.jdesktop.wonderland.modules.eventplayer.common.EventPlayerClientState;
import org.jdesktop.wonderland.modules.eventplayer.common.Tape;

/**
 *
 * @author Bernard Horan
 * @author Joe Provino
 */
public class EventPlayerCell extends Cell {

    private static final Logger eventPlayerLogger = Logger.getLogger(EventPlayerCell.class.getName());

    private boolean isPlaying;
    private String userName;
    private EventPlayerCellRenderer renderer;
    private DefaultListModel tapeListModel;
    private DefaultListSelectionModel tapeSelectionModel;
    private ReelForm reelForm;

    public EventPlayerCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        isPlaying = false;
        eventPlayerLogger.info("cellID: " + cellID);
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
        eventPlayerLogger.info("status: " + status);
        super.setStatus(status);
        if (status.equals(CellStatus.ACTIVE)) {
            //About to become visible, so add the message receiver
            getChannel().addMessageReceiver(EventPlayerCellChangeMessage.class, new EventPlayerCellMessageReceiver());
        }
        if (status.equals(CellStatus.DISK)) {
            //Cleanup
            getChannel().removeMessageReceiver(EventPlayerCellChangeMessage.class);
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
        createTapeModels(tapes, selectedTape);

        isPlaying = ((EventPlayerClientState)setupData).isPlaying();
        userName = ((EventPlayerClientState)setupData).getUserName();
        reelForm = new ReelForm(this);
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

    private void createTapeModels(Set<Tape> tapes, Tape selectedTape) {
        List sortedTapes = new ArrayList(tapes);
        Collections.sort(sortedTapes);
        tapeListModel = new DefaultListModel();
        for (Iterator it = sortedTapes.iterator(); it.hasNext();) {
            tapeListModel.addElement(it.next());
        }

        tapeSelectionModel = new DefaultListSelectionModel();
        tapeSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        int selectionIndex = sortedTapes.indexOf(selectedTape);
        tapeSelectionModel.setSelectionInterval(selectionIndex, selectionIndex);
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
        eventPlayerLogger.info("Children: " + getNumChildren());
        List<Cell> children = this.getChildren();
        for (Cell cell : children) {
            eventPlayerLogger.info(cell.toString());
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

    private String getCurrentUserName() {
        return "USERNAME NOT SET";
    }

    void setReelFormVisible(boolean aBoolean) {
        eventPlayerLogger.info("set visible: " + aBoolean);
        reelForm.setVisible(aBoolean);
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

