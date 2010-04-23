/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

package org.jdesktop.wonderland.modules.eventplayer.client;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
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
import org.jdesktop.wonderland.client.jme.ClientContextJME;
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
 * A cell that plays back eevents recorded by the event recorder.
 * Currently has no audio playback.
 * @author Bernard Horan
 */
public class EventPlayerCell extends Cell {

    private static final Logger eventPlayerLogger = Logger.getLogger(EventPlayerCell.class.getName());

    @UsesCellComponent private ContextMenuComponent contextComp = null;
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/eventplayer/client/resources/Bundle");
    private ContextMenuFactorySPI menuFactory = null;

    private boolean isPlaying;
    private boolean isPaused;
    private String userName;
    private EventPlayerCellRenderer renderer;
    private DefaultListModel tapeListModel;
    private DefaultListSelectionModel tapeSelectionModel;
    private ReelForm reelForm;
    private int replayedChildren;

    /**
     * Constructor, give
     * @param cellID the cell's unique ID
     * @param cellCache the cell cache which instantiated, and owns, this cell
     */
    public EventPlayerCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        isPlaying = false;
        isPaused = false;
        replayedChildren = 0;
        createTapeModels();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    reelForm = new ReelForm(EventPlayerCell.this);
                }
            });
        } catch (InterruptedException ex) {
            eventPlayerLogger.log(Level.SEVERE, "Failed to create reel form", ex);
        } catch (InvocationTargetException ex) {
            eventPlayerLogger.log(Level.SEVERE, "Failed to create reel form", ex);
        }
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
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if (increasing && status == CellStatus.ACTIVE) {
            //About to become visible, so add the message receiver
            getChannel().addMessageReceiver(EventPlayerCellChangeMessage.class, new EventPlayerCellMessageReceiver());
            //Add menu item to open a tape to the right-hand button context menu
            if (menuFactory == null) {
                final ContextMenuActionListener l = new ContextMenuActionListener() {

                    public void actionPerformed(ContextMenuItemEvent event) {
                        openReelForm();
                    }
                };
                menuFactory = new ContextMenuFactorySPI() {

                    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                        return new ContextMenuItem[]{
                                    new SimpleContextMenuItem(bundle.getString("OPEN_TAPE..."), l)
                                };
                    }
                };
                contextComp.addContextMenuFactory(menuFactory);
            }
        }
        if (!increasing && status == CellStatus.DISK) {
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
    }

    /**
     * Called when the cell is initially created and any time there is a
     * major configuration change. The cell will already be attached to its parent
     * before the initial call of this method
     *
     * @param setupData the data received from the server that describes the state of the
     * corresponding server-side cell
     */
    @Override
    public void setClientState(CellClientState setupData) {
        super.setClientState(setupData);
        Set<Tape> tapes = ((EventPlayerClientState)setupData).getTapes();
        Tape selectedTape = ((EventPlayerClientState)setupData).getSelectedTape();
        updateTapeModels(tapes, selectedTape);

        isPlaying = ((EventPlayerClientState)setupData).isPlaying();
        isPaused = ((EventPlayerClientState)setupData).isPaused();
        userName = ((EventPlayerClientState)setupData).getUserName();
        if(isPlaying) {
            if (userName == null) {
                eventPlayerLogger.warning("userName should not be null");
            }
        }
        if (!isPlaying) {
            if (userName != null) {
                eventPlayerLogger.warning("userName should be null");
            }
        }
        replayedChildren = ((EventPlayerClientState)setupData).getReplayedChildren();
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

    /**
     * Only managed to change the tape if we weren't playing (and not paused)
     * AND there were no cells from earlier replays
     */
    public void selectedTapeChanged() {
        //the selected tape has changed
        eventPlayerLogger.info("selectedTape changed");
        int index = tapeSelectionModel.getMaxSelectionIndex();
        if (index >= 0) {
            //if there's a selected tape let the server know that the selected tape has changed
            Tape selectedTape = (Tape) tapeListModel.elementAt(index);
            //logger.info("selected tape: " + selectedTape);
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
        List<Tape> sortedTapes = new ArrayList<Tape>(tapes);
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
                isPlaying = false;
                isPaused = false;
            }
        }
    }


    void togglePlaying() {
        eventPlayerLogger.info("togglePlaying");
        if (!isPlaying) {
                //not yet started, 
                startPlaying();          
        } else {
            //already started
            if (isPaused) {
                //Paused
                resume();
            } else {
                //Not paused
                pause();
            }
        }
    }
    

    private void startPlaying() {
        eventPlayerLogger.info("start playing");

        Tape selectedTape = getSelectedTape();
        if (selectedTape == null) {
            eventPlayerLogger.warning("Can't playback when there's no selected tape");
            return;
        }
        resume();
    }

    void resume() {
        eventPlayerLogger.info("resume");
        if (userName != null) {
            eventPlayerLogger.warning("userName should be null");
        }
        isPlaying = true;
        isPaused = false;
        userName = getCurrentUserName();
        renderer.setPlaying(true);
        EventPlayerCellChangeMessage msg = EventPlayerCellChangeMessage.playRecording(getCellID(), true, userName);
        getChannel().send(msg);
    }


    void pause() {
        eventPlayerLogger.info("pause");
        if (userName.equals(getCurrentUserName())) {
            isPlaying = true;
            isPaused = true;
            renderer.setPlaying(false);
            EventPlayerCellChangeMessage msg = EventPlayerCellChangeMessage.playRecording(getCellID(), false, userName);
            getChannel().send(msg);
        } else {
            eventPlayerLogger.warning("Attempt to stop by non-initiating user");
            SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(getParentFrame(), bundle.getString("YOU_CAN'T_STOP_A_PLAYBACK_STARTED_BY_ANOTHER_USER"));
                    }
                });
        }
    }

    


    boolean isPlayingTape() {
        return isPlaying  && !isPaused;
    }

    boolean isRecordingLoaded() {
        return getSelectedTape() != null;
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
        return getCellCache().getSession().getUserID().getUsername();
    }

    private JFrame getParentFrame() {
        return ClientContextJME.getClientMain().getFrame().getFrame();
    }

    /**
     * Open the form to select a recording
     * Can only open the reel if we are not playing (and not paused) AND if there are no children from previous replays
     */
    void openReelForm() {
        if (isPlayingTape()) {
            eventPlayerLogger.warning("Can't load a new reel when one is already playing");
            return;
        }
        if (replayedChildren > 0) {
            //Still showing child cells that are being replayed
            logger.warning("can't load a new tape when there's cells from an earlier replay replayed");
            //Ask the user if s/he wishes to remove the existing cells
            int response = JOptionPane.showConfirmDialog(getParentFrame(), bundle.getString("DELETE_CELLS_FROM_REPLAYING_") + getSelectedTape().getTapeName() + bundle.getString("?"), bundle.getString("EXISTING_CELLS"), JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.NO_OPTION) {
                return;
            } else {
                //Delete the existing cells
                logger.warning("Deleting existing cells");
                replayedChildren = 0;
            }
        }
        //Let the server know I'm selecting a tape and wait to get a message back (processTapeStateMessage())
        EventPlayerCellChangeMessage msg = EventPlayerCellChangeMessage.selectingTape(getCellID());
        try {
            final TapeStateMessageResponse response = (TapeStateMessageResponse) getChannel().sendAndWait(msg);
            if (response.getAction() == TapeStateMessageResponse.TapeStateAction.TAPE_STATE) {
                Rectangle parentBounds = getParentFrame().getBounds();
                Rectangle formBounds = reelForm.getBounds();
                reelForm.setLocation(parentBounds.width / 2 - formBounds.width / 2 + parentBounds.x, parentBounds.height - formBounds.height - parentBounds.y);

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        //Need to open the form BEFORE updating models, otherwise ignored
                        reelForm.setVisible(true);
                        updateTapeModels(response.getTapes(), response.getSelectedTape());
                    }
                });

            } else {
                eventPlayerLogger.severe("Failed response from server");
            }
        } catch (InterruptedException ex) {
            eventPlayerLogger.log(Level.SEVERE, "connection with server interrupted", ex);
        }
    }
    

    private void playbackDone() {
        eventPlayerLogger.info("playback done");
        //stop the renderer's playback animation
        renderer.setPlaying(false);
        //remove the button so that no user can click play
        renderer.reduceButtonPyramid();
        isPlaying = false;
        isPaused = false;
        eventPlayerLogger.info("remaining children: " + replayedChildren);
    }

    private void setCellsRetrieved() {
        //Add the button so that the user can click it
        renderer.enlargeButtonPyramid();
        isPlaying = false;
        isPaused = false;
    }

    private void addReplayedChild() {
        eventPlayerLogger.info("adding child");
        replayedChildren++;
    }

    private void removeReplayedChild() {
        eventPlayerLogger.info("removing child");
        replayedChildren--;
    }

    

    class EventPlayerCellMessageReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            //eventPlayerLogger.info("Message Received: " + message);
            EventPlayerCellChangeMessage sccm = (EventPlayerCellChangeMessage) message;
            BigInteger senderID = sccm.getSenderID();
            if (senderID == null) {
                //Broadcast from server
                senderID = BigInteger.ZERO;
            }
            if (!senderID.equals(getCellCache().getSession().getID())) {
                switch (sccm.getAction()) {
                    case PLAY:
                        eventPlayerLogger.info("PLAY: " + sccm.isPlaying());
                        //setPlaying(sccm.isPlaying());
                        userName = sccm.getUserName();
                        break;
                    case LOAD:
                        loadRecording(sccm.getTapeName());
                        break;
                    case PLAYBACK_DONE:
                        playbackDone();
                        userName = null;
                        break;
                    case ALL_CELLS_RETRIEVED:
                        setCellsRetrieved();
                        userName = null;
                        break;
                    case ADD_REPLAYED_CHILD:
                        addReplayedChild();
                        break;
                    case REMOVE_REPLAYED_CHILD:
                        removeReplayedChild();
                        break;
                    default:
                        eventPlayerLogger.severe("Unknown action type: " + sccm.getAction());

                }
            }
        }



        
    }

}

