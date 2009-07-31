/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.cmu.common.messages.serverclient;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author kevin
 */
public class ConnectionChangeMessage extends CellMessage {

    private String server;
    private int port;

    public ConnectionChangeMessage(CellID cellID, String server, int port) {
        super(cellID);
        this.setServer(server);
        this.setPort(port);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public String toString() {
        return "Connection change [Server:" + getServer() + "] [Port:" + getPort() + "]";
    }
}
