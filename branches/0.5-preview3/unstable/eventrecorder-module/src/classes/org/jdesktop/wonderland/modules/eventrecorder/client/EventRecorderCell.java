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


package org.jdesktop.wonderland.modules.eventrecorder.client;

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
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.eventrecorder.common.EventRecorderCellChangeMessage;
import org.jdesktop.wonderland.modules.eventrecorder.common.EventRecorderClientState;
import org.jdesktop.wonderland.modules.eventrecorder.common.Tape;
import org.jdesktop.wonderland.modules.eventrecorder.common.TapeStateMessageResponse;

/**
 *
 * @author Bernard Horan
 * @author Joe Provino
 */
public class EventRecorderCell extends Cell {

    private static final Logger eventRecorderLogger = Logger.getLogger(EventRecorderCell.class.getName());

    @UsesCellComponent private ContextMenuComponent contextComp = null;
    private ContextMenuFactorySPI menuFactory = null;
    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/jdesktop/wonderland/modules/eventrecorder/client/resources/Bundle");

    private boolean isRecording;
    private String userName;
    private EventRecorderCellRenderer renderer;
    private DefaultListModel tapeListModel;
    private DefaultListSelectionModel tapeSelectionModel;
    private ReelForm reelForm;

    public EventRecorderCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        isRecording = false;
        createTapeModels();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    reelForm = new ReelForm(EventRecorderCell.this);
                }
            });
        } catch (InterruptedException ex) {
            eventRecorderLogger.log(Level.SEVERE, "Failed to create reel form", ex);
        } catch (InvocationTargetException ex) {
            eventRecorderLogger.log(Level.SEVERE, "Failed to create reel form", ex);
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
        //eventRecorderLogger.info("status: " + status);
        super.setStatus(status, increasing);
        if (increasing && status == CellStatus.ACTIVE) {
            //About to become visible, so add the message receiver
            getChannel().addMessageReceiver(EventRecorderCellChangeMessage.class, new EventRecorderCellMessageReceiver());
            if (menuFactory == null) {
                    final MenuItemListener l = new MenuItemListener();
                    menuFactory = new ContextMenuFactorySPI() {
                        public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                            return new ContextMenuItem[] {
                                new SimpleContextMenuItem(bundle.getString("OPEN_TAPE..."), l)
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
                channel.removeMessageReceiver(EventRecorderCellChangeMessage.class);
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
     * @param setupData
     */
    @Override
    public void setClientState(CellClientState setupData) {
        super.setClientState(setupData);
        Set<Tape> tapes = ((EventRecorderClientState)setupData).getTapes();
        Tape selectedTape = ((EventRecorderClientState)setupData).getSelectedTape();
        updateTapeModels(tapes, selectedTape);

        isRecording = ((EventRecorderClientState)setupData).isRecording();
        userName = ((EventRecorderClientState)setupData).getUserName();
        if(isRecording) {
            if (userName == null) {
                logger.warning("userName should not be null");
            }
        }
        if (!isRecording) {
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
            renderer = new EventRecorderCellRenderer(this);
            return renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    private ChannelComponent getChannel() {
        return getComponent(ChannelComponent.class);
    }

    Tape addTape(String tapeName) {
        eventRecorderLogger.info("add " + tapeName);
        Tape newTape = new Tape(tapeName);
        tapeListModel.addElement(newTape);
        EventRecorderCellChangeMessage msg = EventRecorderCellChangeMessage.newTape(getCellID(), tapeName);
        getChannel().send(msg);
        return newTape;
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
        eventRecorderLogger.info("selectedTape changed");
        int index = tapeSelectionModel.getMaxSelectionIndex();
        if (index >= 0) {
            Tape selectedTape = (Tape) tapeListModel.elementAt(index);
            logger.info("selected tape: " + selectedTape);
            EventRecorderCellChangeMessage msg = EventRecorderCellChangeMessage.tapeSelected(getCellID(), selectedTape.getTapeName());
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

    private void selectTape(String tapeName) {
        eventRecorderLogger.info("select tape: " + tapeName);
        Enumeration tapes = tapeListModel.elements();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            if (aTape.getTapeName().equals(tapeName)) {
                reelForm.selectTape(aTape);
            }
        }
    }

    private void setTapeUsed(String tapeName) {
        //eventRecorderLogger.info("setTapeUsed: " + tapeName);
        Enumeration tapes = tapeListModel.elements();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            if (aTape.getTapeName().equals(tapeName)) {
                aTape.setUsed();
            }
        }
    }

    /**
     * This is the start of the recording process.
     * Send a message to the server to try to start recording, but don't
     * change MY state
     * I get informed that the server has successfully started recording
     * via the message receiver.
     * See method setRecording()
     */
    void startRecording() {
        //eventRecorderLogger.info("start recording");

        final Tape selectedTape = getSelectedTape();
        if (selectedTape == null) {
            eventRecorderLogger.warning("Can't record when there's no selected tape");
            return;
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    if (!selectedTape.isFresh()) {
                        int response = JOptionPane.showConfirmDialog(getParentFrame(), bundle.getString("OVERWRITE_EXISTING_RECORDING_NAMED_") + selectedTape.getTapeName() + bundle.getString("?"), bundle.getString("EXISTING_TAPE"), JOptionPane.YES_NO_OPTION);
                        if (response == JOptionPane.NO_OPTION) {
                            return;
                        }
                        eventRecorderLogger.warning("Overwriting existing recording named " + selectedTape.getTapeName());

                    }
                    userName = getCurrentUserName();

                    EventRecorderCellChangeMessage msg = EventRecorderCellChangeMessage.recordingMessage(getCellID(), true, userName);
                    getChannel().send(msg);
                }
            });
        } catch (InterruptedException ex) {
            eventRecorderLogger.log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            eventRecorderLogger.log(Level.SEVERE, null, ex);
        }


    }

    void stop() {
        //eventRecorderLogger.info("stop");
        if (userName.equals(getCurrentUserName())) {
            EventRecorderCellChangeMessage msg = null;
            if (isRecording) {
                msg = EventRecorderCellChangeMessage.recordingMessage(getCellID(), false, userName);
            }
            if (msg != null) {
                getChannel().send(msg);
            }
            setRecording(false);
        } else {
            eventRecorderLogger.warning("Attempt to stop by non-initiating user");
            SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(getParentFrame(), bundle.getString("YOU_CAN'T_STOP_A_RECORDING_STARTED_BY_ANOTHER_USER"));
                    }
                });
        }
    }

    private void setRecording(boolean b) {
        //eventRecorderLogger.info("setRecording: " + b);
        //If the recording has started the selected tape is no longer fresh
        if (b) {
            getSelectedTape().setUsed();
        }

        renderer.setRecording(b);
        isRecording = b;
    }


    boolean isRecording() {
        return isRecording;
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
        return JmeClientMain.getFrame().getFrame();
    }

    void openReelForm() {
        //Let the server know I'm selecting a tape and wait to get a message back (processTapeStateMessage())
        EventRecorderCellChangeMessage msg = EventRecorderCellChangeMessage.selectingTape(getCellID());
        try {
            final TapeStateMessageResponse response = (TapeStateMessageResponse) getChannel().sendAndWait(msg);
            if (response.getAction() == TapeStateMessageResponse.TapeStateAction.TAPE_STATE) {
                Rectangle parentBounds = getParentFrame().getBounds();
                Rectangle formBounds = reelForm.getBounds();
                reelForm.setLocation(parentBounds.width/2 - formBounds.width/2 + parentBounds.x, parentBounds.height - formBounds.height - parentBounds.y);

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        //Need to open the form BEFORE updating models, otherwise ignored
                    reelForm.setVisible(true);
                    updateTapeModels(response.getTapes(), response.getSelectedTape());
                    }
                });
                               
            } else {
                eventRecorderLogger.severe("Failed response from server");
            }
        } catch (InterruptedException ex) {
            eventRecorderLogger.log(Level.SEVERE, "connection with server interrupted", ex);
        }
    }
    

    class EventRecorderCellMessageReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            EventRecorderCellChangeMessage sccm = (EventRecorderCellChangeMessage) message;
            BigInteger senderID = sccm.getSenderID();
            if (senderID == null) {
                //Broadcast from server
                senderID = BigInteger.ZERO;
            }
            if (!senderID.equals(getCellCache().getSession().getID())) {
                switch (sccm.getAction()) {
                    case RECORD:
                        setRecording(sccm.isRecording());
                        userName = sccm.getUserName();
                        break;
                    case TAPE_USED:
                        setTapeUsed(sccm.getTapeName());
                        break;
                    case NEW_TAPE:
                        Tape newTape = new Tape(sccm.getTapeName());
                        tapeListModel.addElement(newTape);
                        break;
                    case TAPE_SELECTED:
                        selectTape(sccm.getTapeName());
                        break;
                    default:
                        eventRecorderLogger.severe("Unknown action type: " + sccm.getAction());

                }
            }
        }

    }

    class MenuItemListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            openReelForm();
        }
    }

}

