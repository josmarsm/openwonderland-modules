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

package org.jdesktop.wonderland.modules.eventrecorder.common;

import java.io.Serializable;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Bernard Horan
 */
@XmlRootElement(name="eventrecorder-cell")
@ServerState
public class EventRecorderCellServerState extends CellServerState implements Serializable {
    private int instanceNumber;
    private Set<Tape> tapes;
    private Tape selectedTape;
    private boolean isPlaying, isRecording;
    private String userName;
    private String recordingDirectory;

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.eventrecorder.server.EventRecorderCellMO";
    }

    public void setInstanceNumber(int instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }

    public void setRecordingDirectory(String recordingDirectory) {
        this.recordingDirectory = recordingDirectory;
    }

    public void setSelectedTape(Tape selectedTape) {
        this.selectedTape = selectedTape;
    }

    public void setTapes(Set<Tape> tapes) {
        this.tapes = tapes;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
