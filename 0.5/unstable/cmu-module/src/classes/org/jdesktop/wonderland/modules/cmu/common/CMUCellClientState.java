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
package org.jdesktop.wonderland.modules.cmu.common;

import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 *
 * @author kevin
 */
public class CMUCellClientState extends CellClientState {

    private boolean serverAndPortInitialized = false;
    private float playbackSpeed;
    private String server;
    private int port;

    public synchronized void setServerAndPort(String server, int port) {
        this.serverAndPortInitialized = true;
        this.server = server;
        this.port = port;
    }

    public synchronized String getServer() {
        return server;
    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public synchronized void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
    }

    public synchronized boolean isServerAndPortInitialized() {
        return serverAndPortInitialized;
    }
}
