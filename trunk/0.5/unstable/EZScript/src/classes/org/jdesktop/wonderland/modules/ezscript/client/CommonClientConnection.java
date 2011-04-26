/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.ezscript.common.CommonConnectionType;

/**
 *
 * @author JagWire
 */
public class CommonClientConnection extends BaseConnection {

    @Override
    public void handleMessage(Message message) {
        
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public ConnectionType getConnectionType() {
        return CommonConnectionType.COMMON_TYPE;
    }

    

}
