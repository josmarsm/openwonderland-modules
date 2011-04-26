/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.server;

import java.util.Properties;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.ezscript.common.CommonConnectionType;
import org.jdesktop.wonderland.modules.ezscript.common.CommonMessage;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 *
 * @author JagWire
 */
public class CommonServerConnectionHandler implements ClientConnectionHandler {

    public ConnectionType getConnectionType() {
        return CommonConnectionType.COMMON_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clientConnected(WonderlandClientSender sender, WonderlandClientID clientID, Properties properties) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, Message message) {
        //throw new UnsupportedOperationException("Not supported yet.");
        if(message instanceof CommonMessage) {
            sender.send(message);
        }
    }

    public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

}
