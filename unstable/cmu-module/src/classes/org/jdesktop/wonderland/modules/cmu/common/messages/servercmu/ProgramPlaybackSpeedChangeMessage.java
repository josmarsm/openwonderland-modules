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
package org.jdesktop.wonderland.modules.cmu.common.messages.servercmu;

import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Message from a CMUCellMO to a CMU program manager to represent a
 * change in playback speed on a particular program.
 * @author kevin
 */
public class ProgramPlaybackSpeedChangeMessage extends ServerCMUMessage {
    private float playbackSpeed;

    /**
     * Standard constructor.
     * @param cellID The ID of the relevant cell
     * @param playbackSpeed The playback speed which is being set
     */
    public ProgramPlaybackSpeedChangeMessage(CellID cellID, float playbackSpeed) {
        super(cellID);
        setPlaybackSpeed(playbackSpeed);
    }

    /**
     * Get the playback speed which this message represents.
     * @return Current playback speed
     */
    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     * Set the playback speed.
     * @param playbackSpeed New playback speed
     */
    public void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
    }
}
