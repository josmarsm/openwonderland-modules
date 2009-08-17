/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

    @Override
    public String toString() {
        return "Connection change [Server:" + getHostname() + "] [Port:" + getPort() + "]";
    }
}
