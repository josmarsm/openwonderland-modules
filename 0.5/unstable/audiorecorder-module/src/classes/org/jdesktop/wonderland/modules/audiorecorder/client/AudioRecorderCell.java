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

package org.jdesktop.wonderland.modules.audiorecorder.client;

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
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.audiorecorder.common.AudioRecorderCellChangeMessage;
import org.jdesktop.wonderland.modules.audiorecorder.common.AudioRecorderCellClientState;
import org.jdesktop.wonderland.modules.audiorecorder.common.Tape;

/**
 *
 * @author Bernard Horan
 * @author Joe Provino
 */
public class AudioRecorderCell extends Cell {

    private static final Logger audioRecorderLogger = Logger.getLogger(AudioRecorderCell.class.getName());

    private boolean isPlaying, isRecording;
    private String userName;
    private AudioRecorderCellRenderer renderer;
    private DefaultListModel tapeListModel;
    private DefaultListSelectionModel tapeSelectionModel;
    private ReelForm reelForm;

    public AudioRecorderCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        isRecording = false;
        isPlaying = false;

    }

    @Override
    public boolean setStatus(CellStatus status) {
        super.setStatus(status);
        if (status.equals(CellStatus.ACTIVE)) {
            //About to become visible, so add the message receiver
            getChannel().addMessageReceiver(AudioRecorderCellChangeMessage.class, new AudioRecorderCellMessageReceiver());
        }
        if (status.equals(CellStatus.DISK)) {
            //Cleanup
            if (getChannel() != null) {
                getChannel().removeMessageReceiver(AudioRecorderCellChangeMessage.class);
            }
        }
        //No change in my status, so...
        return false;
    }

    @Override
    public void setClientState(CellClientState cellClientState) {
        super.setClientState(cellClientState);

        Set<Tape> tapes = ((AudioRecorderCellClientState)cellClientState).getTapes();
        Tape selectedTape = ((AudioRecorderCellClientState)cellClientState).getSelectedTape();
        createTapeModels(tapes, selectedTape);

        isPlaying = ((AudioRecorderCellClientState)cellClientState).isPlaying();
        isRecording = ((AudioRecorderCellClientState)cellClientState).isRecording();
        userName = ((AudioRecorderCellClientState)cellClientState).getUserName();
        reelForm = new ReelForm(this);
    }

    private ChannelComponent getChannel() {
        return getComponent(ChannelComponent.class);
    }

    Tape addTape(String tapeName) {
        audioRecorderLogger.info("add " + tapeName);
        Tape newTape = new Tape(tapeName);
        tapeListModel.addElement(newTape);
        AudioRecorderCellChangeMessage msg = AudioRecorderCellChangeMessage.newTape(getCellID(), tapeName);
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
        audioRecorderLogger.info("selectedTape changed");
        int index = tapeSelectionModel.getMaxSelectionIndex();
        if (index >= 0) {
            Tape selectedTape = (Tape) tapeListModel.elementAt(index);
            logger.info("selected tape: " + selectedTape);
            AudioRecorderCellChangeMessage msg = AudioRecorderCellChangeMessage.tapeSelected(getCellID(), selectedTape.getTapeName());
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

    private void selectTape(String tapeName) {
        audioRecorderLogger.info("select tape: " + tapeName);
        Enumeration tapes = tapeListModel.elements();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            if (aTape.getTapeName().equals(tapeName)) {
                reelForm.selectTape(aTape);
            }
        }
    }

    private void setTapeUsed(String tapeName) {
        audioRecorderLogger.info("setTapeUsed: " + tapeName);
        Enumeration tapes = tapeListModel.elements();
        while (tapes.hasMoreElements()) {
            Tape aTape = (Tape) tapes.nextElement();
            if (aTape.getTapeName().equals(tapeName)) {
                aTape.setUsed();
            }
        }
    }

    void startRecording() {
        audioRecorderLogger.info("start recording");
        if (!isPlaying) {
            Tape selectedTape = getSelectedTape();
            if (selectedTape == null) {
                audioRecorderLogger.warning("Can't record when there's no selected tape");
                return;
            }
            if (!selectedTape.isFresh()) {
                audioRecorderLogger.warning("Overwriting existing recording");
            }
            setUsed(selectedTape);
            userName = getCurrentUserName();
            setRecording(true);
            AudioRecorderCellChangeMessage msg = AudioRecorderCellChangeMessage.recordingMessage(getCellID(), isRecording, userName);
            getChannel().send(msg);
        } else {
            logger.warning("Can't start recording when already playing");
        }
    }

    private void setUsed(Tape aTape) {
        audioRecorderLogger.info("setUsed: " + aTape);
        aTape.setUsed();
        AudioRecorderCellChangeMessage msg = AudioRecorderCellChangeMessage.setTapeUsed(getCellID(), aTape.getTapeName());
        getChannel().send(msg);
    }

    void startPlaying() {
        audioRecorderLogger.info("start playing");
        if (!isRecording) {
            Tape selectedTape = getSelectedTape();
            if (selectedTape == null) {
                logger.warning("Can't playback when there's no selected tape");
                return;
            }
            if (selectedTape.isFresh()) {
                logger.warning("Can't playback a tape that's not ben recorded");
                return;
            }
            userName = getCurrentUserName();
            setPlaying(true);
            AudioRecorderCellChangeMessage msg = AudioRecorderCellChangeMessage.playingMessage(getCellID(), isPlaying, userName);
            getChannel().send(msg);
        } else {
            audioRecorderLogger.warning("Can't start playing when already recording");
        }
    }


    void stop() {
        audioRecorderLogger.info("stop");
	if (userName != null && userName.equals(getCurrentUserName())) {
            AudioRecorderCellChangeMessage msg = null;
            if (isRecording) {
                msg = AudioRecorderCellChangeMessage.recordingMessage(getCellID(), false, userName);
            }
            if (isPlaying) {
                msg = AudioRecorderCellChangeMessage.playingMessage(getCellID(), false, userName);
            }
            if (msg != null) {
                getChannel().send(msg);
            }
            setRecording(false);
            setPlaying(false);
        } else {
            audioRecorderLogger.warning("Attempt to stop by non-initiating user");
        }
    }

    private void setRecording(boolean b) {
        audioRecorderLogger.info("setRecording: " + b);
        renderer.setRecording(b);
        isRecording = b;
    }

    private void setPlaying(boolean b) {
        audioRecorderLogger.info("setPlaying: " + b);
        renderer.setPlaying(b);
        isPlaying = b;
    }

    boolean isPlaying() {
        return isPlaying;
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
        return "USERNAME NOT SET";
    }

    void setReelFormVisible(boolean aBoolean) {
        audioRecorderLogger.info("set visible: " + aBoolean);
        reelForm.setVisible(aBoolean);
    }
    

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new AudioRecorderCellRenderer(this);
            return this.renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    

    class AudioRecorderCellMessageReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            AudioRecorderCellChangeMessage sccm = (AudioRecorderCellChangeMessage) message;
            BigInteger senderID = sccm.getSenderID();
            if (senderID == null) {
                //Broadcast from server
                senderID = BigInteger.ZERO;
            }
            if (!senderID.equals(getCellCache().getSession().getID())) {
                switch (sccm.getAction()) {
                    case SET_VOLUME:
                        //AudioRecorderCellMenu menu = AudioRecorderCellMenu.getInstance();
                        //menu.volumeChanged(getCellID().toString(), message.getVolume());
                        break;
                    case PLAYBACK_DONE:
                        setPlaying(false);
                        break;
                    case PLAY:
                        setPlaying(sccm.isPlaying());
                        userName = sccm.getUserName();
                        break;
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
                        audioRecorderLogger.severe("Unknown action type: " + sccm.getAction());

                }
            }
        }
    }

}

