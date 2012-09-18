/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2012, University of Essex, UK, 2012, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package uk.ac.essex.wonderland.modules.countdowntimer.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.ProgrammaticLogin;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import uk.ac.essex.wonderland.modules.countdowntimer.common.CountdownFailureException;

/**
 * Main class for the standalone client.  This class reads the server URL,
 * username and password from the command line, and creates a session
 * connected to the given Wonderland server.  It then connects a new
 * CountdownCellConnection, and proceeds to create a cell and then change its
 * color every second.
 *
 * @author Bernard Horan
 */
@ExperimentalAPI
public class ConnectionClientMain {
    /** A logger for error messages */
    private static final Logger logger =
            Logger.getLogger(ConnectionClientMain.class.getName());

    /**
     * The login object provides an abstraction for creating a session with
     * the server.
     */
    private ProgrammaticLogin<WonderlandSession> login;

    

    /**
     * Create a new ConnectionClientMain
     * @param serverURL the url of the server to connect to
     * @param username the username to connect with
     * @param passwordFile a file storing the user's password for logins
     * that require a password.
     * @throws ConnectionFailureException if the client can't connect to
     * the given server
     * @throws InterruptedException
     */
    public ConnectionClientMain(int cellID, String serverURL, String username,
                                File passwordFile)
        throws ConnectionFailureException, InterruptedException, JAXBException
    {
        // initialize the login object
        login = new ProgrammaticLogin<WonderlandSession>(serverURL);

        // log in to the server
        logger.info("Logging in");
        WonderlandSession session = login.login(username, passwordFile);

        // attach the countdown cell connection
        logger.warning("Login succeeded, attaching connection");
        final CountdownCellConnection ccc = new CountdownCellConnection();
        session.connect(ccc);

        try {
            ccc.startTimer(cellID, 1, 0);
        } catch (CountdownFailureException ex) {
            logger.log(Level.SEVERE, "failed to set simulation", ex);
            System.exit(-1);
        }
        //System.exit(0);
    }

    /**
     * Main method
     * @param args the arguments: serverURL (required), username (required),
     * password (optional)
     */
    public static void main(String[] args) {
        if (args.length < 3 || args.length > 4) {
            System.out.println("Usage: ConnectionClientMain cellID serverURL" +
                               " username [password]");
            System.exit(-1);
        }

        int cellID = Integer.valueOf(args[0]);
        logger.info("cellID: " + cellID);
        String serverURL = args[1];
        String username = args[2];
        File passwordFile = null;
        
        // if there is an optional password, write it to a file to use during
        // login
        if (args.length == 4) {
            String password = args[3];

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
            new ConnectionClientMain(cellID, serverURL, username, passwordFile);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error starting client", ex);
            System.exit(-1);
        }
    }
}
