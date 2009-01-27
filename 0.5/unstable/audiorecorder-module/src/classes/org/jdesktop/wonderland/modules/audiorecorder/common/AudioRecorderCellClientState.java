/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.audiorecorder.common;

import java.util.Set;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 *
 * @author bh37721
 */
public class AudioRecorderCellClientState extends CellClientState {
    private Set<Tape> tapes;
    private Tape selectedTape;
    private boolean isPlaying, isRecording;
    private String userName;

    /** Default constructor */
    public AudioRecorderCellClientState() {
    }

     public AudioRecorderCellClientState(Set<Tape> tapes, Tape selectedTape, boolean isPlaying, boolean isRecording, String userName) {
        this.tapes = tapes;
        this.selectedTape = selectedTape;
        this.isPlaying = isPlaying;
        this.isRecording = isRecording;
        this.userName = userName;
    }

    public Set getTapes() {
        return tapes;
    }

    public Tape getSelectedTape() {
        return selectedTape;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public String getUserName() {
        return userName;
    }
}

