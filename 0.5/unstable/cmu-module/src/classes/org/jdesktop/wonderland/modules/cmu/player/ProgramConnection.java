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
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.CreateProgramMessage;
import org.jdesktop.wonderland.modules.cmu.common.ProgramConnectionType;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.DeleteProgramMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.ProgramPlaybackSpeedChangeMessage;

/**
 * Used to connect CMU as clients to the Wonderland server.  Interfaces with
 * a ProgramManager, which passes messages from the server on to individual
 * CMU programs.
 * @author kevin
 */
public class ProgramConnection extends BaseConnection {

    protected final ProgramManager programManager;

    /**
     * Standard constructor.
     * @param programManager The program manager to interact with
     */
    public ProgramConnection(ProgramManager programManager) {
        super();
        assert programManager != null;
        this.programManager = programManager;
        System.out.println("ProgramConnection created.");
    }

    /**
     * Pass the given message on to the program manager in an appropriate way,
     * depending on the message class.
     * @param message The message to pass on
     */
    @Override
    public void handleMessage(Message message) {
        System.out.println("Message received: " + message);

        // Create program
        if (CreateProgramMessage.class.isAssignableFrom(message.getClass())) {
            ResponseMessage response = handleCreateProgram((CreateProgramMessage) message);
            System.out.println("Sending response: " + response);
            this.send(response);
        }

        // Change program playback speed
        if (ProgramPlaybackSpeedChangeMessage.class.isAssignableFrom(message.getClass())) {
            handlePlaybackSpeedChange((ProgramPlaybackSpeedChangeMessage) message);
        }

        // Delete program
        if (DeleteProgramMessage.class.isAssignableFrom(message.getClass())) {
            handleDeleteProgram((DeleteProgramMessage)message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionType getConnectionType() {
        return ProgramConnectionType.TYPE;
    }

    /**
     * Tell the program manager to create the program with uri specified by the message.
     * @param message The sent message
     * @return The program manager's response, containing socket information
     */
    protected ResponseMessage handleCreateProgram(CreateProgramMessage message) {
        return programManager.createProgram(message.getMessageID(), message.getCellID(), message.getProgramURI());
    }

    /**
     * Tell the program manager to change the playback speed of a particular program.
     * @param message The message containing speed change information and identification
     */
    protected void handlePlaybackSpeedChange(ProgramPlaybackSpeedChangeMessage message) {
        programManager.setPlaybackSpeed(message.getCellID(), message.getPlaybackSpeed());
    }

    protected void handleDeleteProgram(DeleteProgramMessage message) {
        programManager.deleteProgram(message.getCellID());
    }
}
