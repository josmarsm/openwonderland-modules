/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.weblib;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.isocial.common.CurrentInstanceMessage;
import org.jdesktop.wonderland.modules.isocial.common.ISocialConnectionType;
import org.jdesktop.wonderland.modules.isocial.common.ResultMessage;

/**
 * Web connection for isocial data
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ISocialWebConnection extends BaseConnection {
    private static final Logger LOGGER =
            Logger.getLogger(ISocialWebConnection.class.getName());
    
    public ConnectionType getConnectionType() {
        return ISocialConnectionType.CONNECTION_TYPE;
    }

    public void resultAdded(String resultId) {
        send(new ResultMessage(resultId, ResultMessage.Type.ADDED));
    }

    public void resultUpdated(String resultId) {
        send(new ResultMessage(resultId, ResultMessage.Type.UPDATED));
    }

    public void currentInstanceChanged(String instanceId) {
        send(new CurrentInstanceMessage(instanceId));
    }

    @Override
    public void handleMessage(Message message) {
    }
}
