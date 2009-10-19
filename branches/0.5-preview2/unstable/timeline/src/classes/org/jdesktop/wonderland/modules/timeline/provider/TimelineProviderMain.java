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
package org.jdesktop.wonderland.modules.timeline.provider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.ProgrammaticLogin;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Main class for the standalone client.  This class reads the server URL,
 * username and password from the command line, and creates a session
 * connected to the given Wonderland server.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@ExperimentalAPI
public class TimelineProviderMain {
    /** A logger for error messages */
    private static final Logger logger =
            Logger.getLogger(TimelineProviderMain.class.getName());

    /**
     * The login object provides an abstraction for creating a session with
     * the server.
     */
    private ProgrammaticLogin<WonderlandSession> login;

    /** A timer to periodically change the cell color */
    private Timer timer;

    /**
     * Create a new TimelineProviderMain
     * @param serverURL the url of the server to connect to
     * @param username the username to connect with
     * @param passwordFile a file storing the user's password for logins
     * that require a password.
     * @throws ConnectionFailureException if the client can't connect to
     * the given server
     */
    public TimelineProviderMain(String serverURL, String username,
                                File passwordFile)
        throws ConnectionFailureException, InterruptedException
    {
        // initialize the login object
        login = new ProgrammaticLogin<WonderlandSession>(serverURL);
        timer = new Timer();

        // log in to the server
        System.out.println("Logging in");
        WonderlandSession session = login.login(username, passwordFile);

        // attach the color change connection
        System.out.println("Login succeeded, attaching connection");
        final TimelineProviderConnection tpc = new TimelineProviderConnection();
        session.connect(tpc);
    }

    /**
     * Main method
     * @param args the arguments: serverURL (required), username (required),
     * password (optional)
     */
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
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Error creating password file", ioe);
                System.exit(-1);
            }
        }

        // create the connection object
        try {
            new TimelineProviderMain(serverURL, username, passwordFile);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error starting client", ex);
            System.exit(-1);
        }
    }
}
