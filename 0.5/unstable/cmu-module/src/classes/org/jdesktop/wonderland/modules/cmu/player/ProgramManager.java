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
import org.jdesktop.wonderland.modules.cmu.common.CreateProgramResponseMessage;

/**
 *
 * @author kevin
 */
public class ProgramManager {

    private final Map<CellID, ProgramPlayer> programs = new HashMap<CellID, ProgramPlayer>();
    private ProgrammaticLogin<WonderlandSession> login;

    public ProgramManager(String serverURL, String username,
            File passwordFile) throws ConnectionFailureException,
            InterruptedException {
        // initialize the login object
        login = new ProgrammaticLogin<WonderlandSession>(serverURL);

        // log in to the server
        WonderlandSession session = login.login(username, passwordFile);
        session.connect(new ProgramConnection(this));
    }

    public CreateProgramResponseMessage createProgram(MessageID messageID, CellID cellID, String assetURI) {
        // Load local cache file, and send it to the program.
        ProgramPlayer newProgram = null;
        try {
            URL url = AssetUtils.getAssetURL(assetURI);
            Asset a = AssetManager.getAssetManager().getAsset(new ContentURI(url.toString()));
            if (AssetManager.getAssetManager().waitForAsset(a)) {
                newProgram = new ProgramPlayer(a.getLocalCacheFile());
                programs.put(cellID, newProgram);
            } else {
                System.out.println("Couldn't load asset: " + a);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(ProgramPlayer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ProgramPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new CreateProgramResponseMessage(messageID, cellID, newProgram.getServer(), newProgram.getPort());
    }

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: ConnectionClientMain serverURL" +
                    " username [password]");
            System.exit(-1);
        }

        String serverURL = args[0];
        String username = args[1];
        File passwordFile = null;

        // if there is an optional password, write it to a file to use during
        // login
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
            try {
                new ProgramManager(serverURL, username, passwordFile);
            } catch (ConnectionFailureException ex) {
                Logger.getLogger(ProgramManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ProgramManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}