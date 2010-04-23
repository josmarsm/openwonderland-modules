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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.assetmgr.Asset;
import org.jdesktop.wonderland.client.assetmgr.AssetManager;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.ProgrammaticLogin;
import org.jdesktop.wonderland.common.ContentURI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.UnloadSceneReason;
import org.jdesktop.wonderland.modules.cmu.common.events.EventResponseList;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.CMUResponseFunction;
import org.jdesktop.wonderland.modules.cmu.common.messages.servercmu.CreateProgramResponseMessage;
import org.jdesktop.wonderland.modules.cmu.player.connections.ContentManager;

/**
 * Processes messages sent to control CMU programs.
 * @author kevin
 */
public class ProgramManager {

    private final Map<CellID, ProgramPlayer> programs = new HashMap<CellID, ProgramPlayer>();
    private final ProgrammaticLogin<WonderlandSession> login;

    /**
     * Standard constructor.
     * @param serverURL Wonderland server URL
     * @param username Login username
     * @param passwordFile Login password file
     * @throws org.jdesktop.wonderland.client.comms.ConnectionFailureException
     * @throws java.lang.InterruptedException
     */
    public ProgramManager(String serverURL, String username,
            File passwordFile) throws ConnectionFailureException,
            InterruptedException {

        // Initialize the login object
        login = new ProgrammaticLogin<WonderlandSession>(serverURL);

        // Log in to the server
        WonderlandSession session = login.login(username, passwordFile);
        ContentManager.initialize(session.getSessionManager(), username);

        // Initialize the connection
        session.connect(new ProgramConnection(this));
        Logger.getLogger(ProgramManager.class.getName()).info("Connected to " + serverURL + " as " + username);
    }

    /**
     * Create a program from the .a3p file given by the asset, and respond
     * with information about the socket which that program created for other
     * clients.
     * @param messageID ID of the message sent from the CMUCellMO
     * @param cellID ID of the cell which will host this program
     * @param assetURI The asset pointing to the .a3p file
     * @param initialPlaybackSpeed The speed at which to start playing the program
     * @return A response message containing socket information for the
     * created program.
     */
    public CreateProgramResponseMessage createProgram(MessageID messageID, CellID cellID, String assetURI, float initialPlaybackSpeed) {
        Logger.getLogger(ProgramManager.class.getName()).info("Starting scene: " + assetURI);

        // Delete the existing program (if any) first
        deleteProgram(cellID, UnloadSceneReason.RESTARTING);

        // Load local cache file, and send it to the program
        ProgramPlayer newProgram = null;
        try {
            URL url = AssetUtils.getAssetURL(assetURI);
            Asset a = AssetManager.getAssetManager().getAsset(new ContentURI(url.toString()));
            if (AssetManager.getAssetManager().waitForAsset(a)) {
                // Create program
                newProgram = new ProgramPlayer(a.getLocalCacheFile(), url.getFile());

                // Set initial playback speed
                newProgram.setPlaybackSpeed(initialPlaybackSpeed);

                // Store in map for later access
                addProgram(cellID, newProgram);
            } else {
                Logger.getLogger(ProgramManager.class.getName()).log(Level.SEVERE, "Couldn't load asset: " + a);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(ProgramPlayer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ProgramPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }

        Logger.getLogger(ProgramManager.class.getName()).info(assetURI +
                " accepting connections on " + newProgram.getHostname() +
                ":" + newProgram.getPort());

        // Create and send response message.
        return new CreateProgramResponseMessage(messageID, cellID, newProgram.getHostname(), newProgram.getPort(),
                newProgram.getAllowedResponses(), newProgram.getEventList());
    }

    /**
     * Set the playback speed for a particular program.  Notification of
     * Wonderland clients viewing the cell should be handled by the server,
     * not here.
     * @param cellID The Wonderland cell ID associated with the program
     * @param playbackSpeed The speed to set
     */
    public void setPlaybackSpeed(CellID cellID, float playbackSpeed) {
        ProgramPlayer program = getProgram(cellID);
        if (program != null) {
            program.setPlaybackSpeed(playbackSpeed);
        }
    }

    /**
     * Simulate a mouse click in a particular program on a particular node.
     * @param cellID ID for the cell associated with the program receiving the click
     * @param nodeID ID for the node which received the click
     */
    public void click(CellID cellID, NodeID nodeID) {
        ProgramPlayer program = getProgram(cellID);
        if (program != null) {
            program.click(nodeID);
        }
    }

    public void eventResponse(CellID cellID, CMUResponseFunction response) {
        ProgramPlayer program = getProgram(cellID);
        if (program != null) {
            program.eventResponse(response);
        }
    }

    /**
     * Stop playback of the program associated with the given cell and send
     * a message to connected clients noting that this has happened.
     * @param cellID The cell whose program is to be deleted
     * @param reason The reason for the program's deletion
     */
    public void deleteProgram(CellID cellID, UnloadSceneReason reason) {
        synchronized (programs) {
            ProgramPlayer program = programs.get(cellID);
            if (program != null) {
                program.disconnectProgram(reason);
                programs.remove(cellID);
            }
        }
    }

    public void eventListUpdate(CellID cellID, EventResponseList list) {
        ProgramPlayer program = getProgram(cellID);
        if (program != null) {
            program.setEventList(list);
        }
    }

    /**
     * Synchronously adds a program to our map.
     * @param key The Wonderland cell ID associated with the program
     * @param program The program instance
     */
    private void addProgram(CellID key, ProgramPlayer program) {
        synchronized (programs) {
            // Check for an existing program, and stop it if it's there.
            deleteProgram(key, UnloadSceneReason.RESTARTING);
            programs.put(key, program);
        }
    }

    /**
     * Synchronously retrieves a program from our map.
     * @param key The Wonderland cell ID associated with the program
     * @return The program instance, or null if none is found
     */
    private ProgramPlayer getProgram(CellID key) {
        synchronized (programs) {
            return programs.get(key);
        }
    }

    /**
     * Create a new ProgramManager instance to establish a connection
     * with the Wonderland server.
     * @param args server URL, username, [password]
     */
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            Logger.getLogger(ProgramManager.class.getName()).warning("Usage: ProgramManager serverURL username [password]");
            System.exit(-1);
        }

        String serverURL = args[0];
        String username = args[1];

        File passwordFile = null;

        // if there is an optional password, write it to a file to use during login
        if (args.length == 3) {
            String password = args[2];

            // write the password to a temporary file for login
            try {
                passwordFile = File.createTempFile("wonderlandpw", "tmp");
                passwordFile.deleteOnExit();
                PrintWriter pr = new PrintWriter(new FileWriter(passwordFile));
                pr.write(password);
                pr.close();

            } catch (IOException ex) {
                Logger.getLogger(ProgramManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            new ProgramManager(serverURL, username, passwordFile);
        } catch (ConnectionFailureException ex) {
            Logger.getLogger(ProgramManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ProgramManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
