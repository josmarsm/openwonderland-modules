/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.viewcachetest.client;

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.viewcachetest.common.CreateMessage;
import org.jdesktop.wonderland.modules.viewcachetest.common.MoveMessage;
import org.jdesktop.wonderland.modules.viewcachetest.common.MoveMessage.Where;
import org.jdesktop.wonderland.modules.viewcachetest.common.ViewCacheTestConnectionType;

/**
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class ViewCacheTestConnection extends BaseConnection {

    public ConnectionType getConnectionType() {
        return ViewCacheTestConnectionType.CONNECTION_TYPE;
    }

    public void add(int count) {
        send(new CreateMessage(count));
    }

    public void move(Where where) {
        send(new MoveMessage(where));
    }

    @Override
    public void handleMessage(Message message) {
    }

}
