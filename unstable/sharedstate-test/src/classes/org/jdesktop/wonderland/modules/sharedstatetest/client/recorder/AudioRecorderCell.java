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

package org.jdesktop.wonderland.modules.sharedstatetest.client.recorder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedMap;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstatetest.common.recorder.RecorderConstants;
import org.jdesktop.wonderland.modules.sharedstatetest.common.recorder.RecorderState;
import org.jdesktop.wonderland.modules.sharedstatetest.common.recorder.Tape;

/**
 *
 * @author Bernard Horan
 * @author Joe Provino
 */
public class AudioRecorderCell extends Cell implements SharedMapListenerCli {

    private static final Logger audioRecorderLogger = Logger.getLogger(AudioRecorderCell.class.getName());

    private AudioRecorderCellRenderer renderer;
    private DefaultListModel tapeListModel;
    private DefaultListSelectionModel tapeSelectionModel;
    private ReelForm reelForm;

    @UsesCellComponent
    private SharedStateComponent ssc;

    private SharedMapCli statusMap;
    private SharedMapCli tapeMap;

    public AudioRecorderCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    @Override
    public boolean setStatus(CellStatus status) {
        super.setStatus(status);

        if (status.equals(CellStatus.ACTIVE)) {
            System.out.println("XXXXXXXXX Status active");

            // load the maps
            statusMap = ssc.get(RecorderConstants.STATUS_MAP);
            tapeMap = ssc.get(RecorderConstants.TAPE_MAP);

            // create the tapes model based on the tape map
            SharedString selectedTape = statusMap.get(RecorderConstants.CURRENT_TAPE,
                                                      SharedString.class);
            String tapeName = null;
            if (selectedTape != null) {
                tapeName = selectedTape.getValue();
            }

            createTapeModels(tapeMap, tapeName);
            
            // add listeners
            statusMap.addSharedMapListener(this);
            tapeMap.addSharedMapListener(this);
        
            // create the reel frame
            reelForm = new ReelForm(this);

            // now set the buttons to the right state
            renderer.setRecorderState(getRecorderState());
        }
        
        //No change in my status, so...
        return false;
    }

    Tape addTape(String tapeName) {
        audioRecorderLogger.info("add " + tapeName);
        Tape newTape = new Tape(tapeName);
        tapeListModel.addElement(newTape);

        tapeMap.put(tapeName, SharedBoolean.FALSE);
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

            statusMap.put(RecorderConstants.CURRENT_TAPE,
                          SharedString.valueOf(selectedTape.getTapeName()));
        }
    }

    private void createTapeModels(SharedMap tapeMap, String selectedTape) {
        int selectionIndex = -1;
        int count = 0;

        List<Tape> sortedTapes = new ArrayList<Tape>(tapeMap.size());
        
        for (Map.Entry<String, SharedData> tapeEntry : tapeMap.entrySet()) {
            Tape t = new Tape(tapeEntry.getKey());
            SharedBoolean used = (SharedBoolean) tapeEntry.getValue();
            if (used.getValue()) {
                t.setUsed();
            }

            if (t.getTapeName().equals(selectedTape)) {
                selectionIndex = count;
            }

            sortedTapes.add(t);

            count++;
        }


        Collections.sort(sortedTapes);

        tapeListModel = new DefaultListModel();
        for (Iterator it = sortedTapes.iterator(); it.hasNext();) {
            tapeListModel.addElement(it.next());
        }

        tapeSelectionModel = new DefaultListSelectionModel();
        tapeSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
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
        if (getRecorderState() == RecorderState.STOPPED) {
            Tape selectedTape = getSelectedTape();
            if (selectedTape == null) {
                audioRecorderLogger.warning("Can't record when there's no selected tape");
                return;
            }
            if (!selectedTape.isFresh()) {
                audioRecorderLogger.warning("Overwriting existing recording");
            } else {
                setUsed(selectedTape);
            }
            
            // update the value in the status map.  The reaction will happen
            // when the server updates the status
            statusMap.put(RecorderConstants.STATUS,
                          SharedString.valueOf(RecorderState.RECORDING.name()));
        } else {
            logger.warning("Can't start recording when already playing");
        }
    }

    private void setUsed(Tape aTape) {
        audioRecorderLogger.info("setUsed: " + aTape);
        aTape.setUsed();

        tapeMap.put(aTape.getTapeName(), SharedBoolean.TRUE);
    }

    void startPlaying() {
        audioRecorderLogger.info("start playing");
        if (getRecorderState() == RecorderState.STOPPED) {
            Tape selectedTape = getSelectedTape();
            if (selectedTape == null) {
                logger.warning("Can't playback when there's no selected tape");
                return;
            }
            if (selectedTape.isFresh()) {
                logger.warning("Can't playback a tape that's not ben recorded");
                return;
            }

            // update the value in the status map.  The reaction will happen
            // when the server updates the status
            statusMap.put(RecorderConstants.STATUS,
                          SharedString.valueOf(RecorderState.PLAYING.name()));
        } else {
            audioRecorderLogger.warning("Can't start playing when already recording");
        }
    }

    void stop() {
        audioRecorderLogger.info("stop");

        // update the value in the status map.  The reaction will happen
        // when the server updates the status
        statusMap.put(RecorderConstants.STATUS,
                      SharedString.valueOf(RecorderState.STOPPED.name()));
    }

    public RecorderState getRecorderState() {
        SharedString recStr = statusMap.get(RecorderConstants.STATUS,
                                            SharedString.class);
        return RecorderState.valueOf(recStr.getValue());
    }

    private void setRecorderState(RecorderState state) {
        audioRecorderLogger.info("recording state: " + state);
        renderer.setRecorderState(state);
    }

    private Tape getSelectedTape() {
       int selectionIndex = tapeSelectionModel.getMaxSelectionIndex();
       if (selectionIndex == -1) {
           return null;
       } else {
           return (Tape) tapeListModel.elementAt(selectionIndex);
       }
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

    public void propertyChanged(SharedMapCli map, BigInteger senderID,
                                String key, SharedData prevData,
                                SharedData curData)
    {
        if (map.getName().equals(RecorderConstants.TAPE_MAP)) {
            handleTapeChange(key, prevData, curData);
        } else if (map.getName().equals(RecorderConstants.STATUS_MAP)) {
            handleStatusChange(key, prevData, curData);
        }
    }

    private void handleTapeChange(String tapeName, SharedData prevData,
                                  SharedData curData)
    {
        if (prevData == null) {
            // this tape was added
            Tape t = new Tape(tapeName);
            if (((SharedBoolean) curData).getValue()) {
                t.setUsed();
            }

            tapeListModel.addElement(t);
        } else if (curData != null && ((SharedBoolean) curData).getValue()) {
            setTapeUsed(tapeName);
        }
    }

    private void handleStatusChange(String key, SharedData prevData,
                                    SharedData curData)
    {
        if (key.equals(RecorderConstants.STATUS)) {
            String stateStr = ((SharedString) curData).getValue();
            setRecorderState(RecorderState.valueOf(stateStr));
        } else if (key.equals(RecorderConstants.CURRENT_TAPE)) {
            String tapeStr = ((SharedString) curData).getValue();
            selectTape(tapeStr);
        }
    }
}

