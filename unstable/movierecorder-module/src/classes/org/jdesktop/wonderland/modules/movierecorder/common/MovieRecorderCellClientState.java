/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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

package org.jdesktop.wonderland.modules.movierecorder.common;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 *
 * @author Bernard Horan
 */
public class MovieRecorderCellClientState extends CellClientState {
    private boolean isRecording;
    private String userName;

    /** Default constructor */
    public MovieRecorderCellClientState() {
    }

     public MovieRecorderCellClientState(boolean isRecording, String userName) {
        this.isRecording = isRecording;
        this.userName = userName;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public String getUserName() {
        return userName;
    }
}

