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

package org.jdesktop.wonderland.modules.cmu.common.cell;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 *
 * @author kevin
 */
public class CMUCellClientState extends CellClientState {

    private String cmuURI;
    private float playbackSpeed;
    private float elapsed;

    public float getElapsed() {
        return elapsed;
    }

    public void setElapsed(float elapsed) {
        this.elapsed = elapsed;
    }

    /**
     * Returns the URI of the loaded CMU file.
     * @return The URI of the CMU file.
     */
    public String getCmuURI() {
        return cmuURI;
    }

    /**
     * Set the URI of the CMU file.
     * @param uri The URI of the CMU file.
     */
    public void setCmuURI(String uri) {
        cmuURI = uri;
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
    }

}
