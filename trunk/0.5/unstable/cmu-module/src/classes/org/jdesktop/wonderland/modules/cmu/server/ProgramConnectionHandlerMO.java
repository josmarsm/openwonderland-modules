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
package org.jdesktop.wonderland.modules.cmu.server;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.NameNotBoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.cmu.common.ProgramConnectionType;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.CreateProgramMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.DeleteProgramMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.ProgramPlaybackSpeedChangeMessage;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellManagerMO;

/**
 * Managed object with singleton instance to interface between cells and
 * the ProgramConnectionHandler.  Gracefully handles failure cases such
 * as disconnects on behalf of the program manager.
 * @author kevin
 */
public final class ProgramConnectionHandlerMO implements ManagedObject, Serializable {

    private static final String HANDLER_MO_NAME = "__CMU_PROGRAM_CONNECTION_HANDLER";
    private final ProgramConnectionHandler connectionHandler;
    private final Map<CellID, CreateProgramMessage> programsCreated = new HashMap<CellID, CreateProgramMessage>();

    /**
     * Get the relevant connection handler.
     * This class should never be externally instantiated.
     */
    private ProgramConnectionHandlerMO() {
        connectionHandler = (ProgramConnectionHandler) WonderlandContext.getCommsManager().getClientHandler(ProgramConnectionType.TYPE);
        assert connectionHandler != null;
    }

    /**
     * Get the singleton instance of this class.
     * @return Singleton instance
     */
    static public ProgramConnectionHandlerMO getInstance() {
        try {
            return (ProgramConnectionHandlerMO) AppContext.getDataManager().getBinding(HANDLER_MO_NAME);
        } catch (NameNotBoundException ex) {
            // If no object is registered yet, create one and register it.
            ProgramConnectionHandlerMO retVal = new ProgramConnectionHandlerMO();
            AppContext.getDataManager().setBinding(HANDLER_MO_NAME, retVal);
            return retVal;
        }
    }

    /**
     * Send the given message using this instance of the class.
     * @param message Message to send
     */
    private void sendMessage(Message message) {
        connectionHandler.sendMessage(message);
    }

    /**
     * Store information about the program to create so that it can
     * be retrieved later in the case of a disconnect.
     * @param message The program being created
     */
    private void registerProgram(CreateProgramMessage message) {
        synchronized (this.programsCreated) {
            // Overwrite previously written message if applicable
            programsCreated.put(message.getCellID(), message);
        }
    }

    /**
     * Create a program from the given asset; program manager should send
     * a response with socket information.
     * @param cellID The cell wishing to create the program instance
     * @param assetURI The URI of the program file
     */
    static public void createProgram(CellID cellID, String assetURI) {
        ProgramConnectionHandlerMO instance = getInstance();
        CreateProgramMessage message = new CreateProgramMessage(cellID, assetURI);

        instance.registerProgram(message);
        instance.sendMessage(message);
    }

    /**
     * Change playback speed for a particular program.
     * @param cellID The cell connected to the program
     * @param playbackSpeed The new playback speed
     */
    static public void changePlaybackSpeed(CellID cellID, float playbackSpeed) {
        getInstance().sendMessage(new ProgramPlaybackSpeedChangeMessage(cellID, playbackSpeed));
    }

    static public void removeProgram(CellID cellID) {
        getInstance().deleteProgram(cellID);
    }

    private void deleteProgram(CellID cellID) {
        synchronized(this.programsCreated) {
            programsCreated.remove(cellID);
        }
        sendMessage(new DeleteProgramMessage(cellID));
    }

    static public void reconnect() {
        getInstance().recreatePrograms();
    }

    private void recreatePrograms() {
        synchronized (this.programsCreated) {
            for (CellID cellID : programsCreated.keySet()) {
                CMUCellMO cellMO = (CMUCellMO)CellManagerMO.getCell(cellID);
                cellMO.createProgram();
            }
        }
    }
}
