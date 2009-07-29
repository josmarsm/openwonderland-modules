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
package org.jdesktop.wonderland.modules.cmu.player;

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.cmu.common.CreateProgramMessage;
import org.jdesktop.wonderland.modules.cmu.common.ProgramConnectionType;

/**
 *
 * @author kevin
 */
public class ProgramConnection extends BaseConnection {

    protected ProgramManager programManager;

    public ProgramConnection(ProgramManager programManager) {
        super();
        assert programManager != null;
        this.programManager = programManager;
    }

    @Override
    public void handleMessage(Message message) {
        if (CreateProgramMessage.class.isAssignableFrom(message.getClass())) {
            ResponseMessage response = handleCreateProgram((CreateProgramMessage)message);
            this.send(response);
        }
    }

    @Override
    public ConnectionType getConnectionType() {
        return ProgramConnectionType.TYPE;
    }

    protected ResponseMessage handleCreateProgram(CreateProgramMessage message) {
        return programManager.createProgram(message.getMessageID(), message.getCellID(), message.getProgramURI());
    }
}
