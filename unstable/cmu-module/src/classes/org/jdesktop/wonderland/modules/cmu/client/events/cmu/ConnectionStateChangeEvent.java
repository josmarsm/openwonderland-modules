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
package org.jdesktop.wonderland.modules.cmu.client.events.cmu;

import org.jdesktop.wonderland.modules.cmu.client.CMUCell;
import org.jdesktop.wonderland.modules.cmu.client.CMUCell.ConnectionState;

/**
 * Event to represent a change in the connection state of a particular
 * CMU scene.
 * @author kevin
 */
public class ConnectionStateChangeEvent extends CMUChangeEvent {

    private ConnectionState connectionState;

    /**
     * Standard constructor.
     * @param cell The cell whose connection state has changed
     * @param connectionState The new connection state of the cell
     */
    public ConnectionStateChangeEvent(CMUCell cell, ConnectionState connectionState) {
        super(cell);
        setConnectionState(connectionState);
    }

    /**
     * Get the new connection state represented by this event.
     * @return Connection state for this event
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * Set the new connection state represented by this event
     * @param connectionState Connection state for thise event
     */
    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }
}
