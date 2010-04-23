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
package org.jdesktop.wonderland.modules.cmu.player;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.CreateProgramMessage;
import org.jdesktop.wonderland.modules.cmu.common.ProgramConnectionType;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.CMUEventResponseMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.DeleteProgramMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.EventListUpdateMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.MouseClickMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.ProgramPlaybackSpeedChangeMessage;

/**
 * Used to connect CMU as clients to the Wonderland server.  Interfaces with
 * a ProgramManager, which passes messages from the server on to individual
 * CMU programs.
 * @author kevin
 */
public class ProgramConnection extends BaseConnection {

    /**
     * The manager associated with this connection.
     */
    protected final ProgramManager programManager;

    /**
     * Standard constructor.
     * @param programManager The program manager to interact with
     */
    public ProgramConnection(ProgramManager programManager) {
        super();
        assert programManager != null;
        this.programManager = programManager;
    }

    /**
     * Pass the given message on to the program manager in an appropriate way,
     * depending on the message class.
     * @param message The message to pass on
     */
    @Override
    public void handleMessage(final Message message) {
        new Thread(new Runnable() {

            public void run() {
                // Create program
                if (message instanceof CreateProgramMessage) {

                    ResponseMessage response = handleCreateProgram((CreateProgramMessage) message);
                    send(response);

                } // Change program playback speed
                else if (message instanceof ProgramPlaybackSpeedChangeMessage) {
                    handlePlaybackSpeedChange((ProgramPlaybackSpeedChangeMessage) message);
                } // Mouse click
                else if (message instanceof MouseClickMessage) {
                    handleMouseClick((MouseClickMessage) message);
                } // Wonderland event
                else if (message instanceof CMUEventResponseMessage) {
                    handleWonderlandEvent((CMUEventResponseMessage) message);
                } // Delete program
                else if (message instanceof DeleteProgramMessage) {
                    handleDeleteProgram((DeleteProgramMessage) message);
                } // Update event list
                else if (message instanceof EventListUpdateMessage) {
                    handleEventListUpdate((EventListUpdateMessage) message);
                } // Unrecognized message
                else {
                    Logger.getLogger(ProgramConnection.class.getName()).log(Level.SEVERE, "Unknown message: " + message);
                }
            }
        }, "Respond to message: " + message).start();
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
        return programManager.createProgram(message.getMessageID(), message.getCellID(), message.getProgramURI(), message.getInitialPlaybackSpeed());
    }

    /**
     * Tell the program manager to change the playback speed of a particular program.
     * @param message The message containing speed change information and identification
     */
    protected void handlePlaybackSpeedChange(ProgramPlaybackSpeedChangeMessage message) {
        programManager.setPlaybackSpeed(message.getCellID(), message.getPlaybackSpeed());
    }

    /**
     * Tell the program manager to dispose of a particular program.
     * @param message The message pointing to the program to delete
     */
    protected void handleDeleteProgram(DeleteProgramMessage message) {
        programManager.deleteProgram(message.getCellID(), message.getReason());
    }

    /**
     * Tell the program manager to forward a mouse click to a particular program.
     * @param message Message containing click information
     */
    protected void handleMouseClick(MouseClickMessage message) {
        programManager.click(message.getCellID(), message.getNodeID());
    }

    protected void handleWonderlandEvent(CMUEventResponseMessage message) {
        programManager.eventResponse(message.getCellID(), message.getResponse());
    }

    protected void handleEventListUpdate(EventListUpdateMessage message) {
        programManager.eventListUpdate(message.getCellID(), message.getEventList());
    }
}
