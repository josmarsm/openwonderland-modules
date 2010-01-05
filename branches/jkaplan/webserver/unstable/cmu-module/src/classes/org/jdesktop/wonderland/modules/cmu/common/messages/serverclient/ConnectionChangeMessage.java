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
package org.jdesktop.wonderland.modules.cmu.common.messages.serverclient;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Wonderland message containing connection information for a CMU cell
 * to connect to a CMU program instance.
 * @author kevin
 */
public class ConnectionChangeMessage extends CellMessage {

    private String server;
    private int port;

    /**
     * Standard constructor.
     * @param hostname Host address to connect to
     * @param port Port to connect to
     */
    public ConnectionChangeMessage(String hostname, int port) {
        super();
        this.setServer(hostname);
        this.setPort(port);
    }

    /**
     * Get port to connect to
     * @return Current port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set port to connect to
     * @param port New port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get host address to connect to.
     * @return Current host address
     */
    public String getHostname() {
        return server;
    }

    /**
     * Set host address to connect to.
     * @param server New host address
     */
    public void setServer(String server) {
        this.server = server;
    }
}
