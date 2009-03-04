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

package org.jdesktop.wonderland.modules.audiorecorder.common;

import java.util.Set;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 *
 * @author Bernard Horan
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

