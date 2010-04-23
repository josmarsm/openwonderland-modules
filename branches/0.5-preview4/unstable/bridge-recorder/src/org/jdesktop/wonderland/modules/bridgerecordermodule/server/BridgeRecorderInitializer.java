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
package org.jdesktop.wonderland.modules.bridgerecordermodule.server;

import com.sun.voip.NewRecorderListener;
import com.sun.voip.Recorder;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpURL;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.ProgrammaticLogin;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.login.AuthenticationException;
import org.jdesktop.wonderland.common.login.AuthenticationManager;
import org.jdesktop.wonderland.common.login.AuthenticationService;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.webdav.client.WebdavContentRepository;
import org.jdesktop.wonderland.modules.webdav.common.AuthenticatedWebdavResource;
import org.jdesktop.wonderland.modules.webdav.common.FileContentCollection;
import org.jdesktop.wonderland.modules.webdav.common.WebdavContentCollection;

/**
 * Provides a bridge from the voice bridge to the recorder.<br>
 * The default constructor on this class is called when the module in which it is
 * contained is loaded (according to the manifest.mf file, in the nb project
 * directory.<br>
 * @author Joe Provino
 * @author Bernard Horan
 */
public class BridgeRecorderInitializer implements NewRecorderListener {

    private static final Logger logger = Logger.getLogger(BridgeRecorderInitializer.class.getName());
    /** The property with the password file location */
    private static final String PASSWORD_FILE_PROP = "voicebridge.password.file";
    /**
     * The login object provides an abstraction for creating a session with
     * the server.
     */
    private ProgrammaticLogin<WonderlandSession> login;
    private WonderlandSession session;
    /** the local repository */
    private FileContentCollection localRepo;

    public BridgeRecorderInitializer() {
        logger.info("adding new recorder listener...");
        createConnection();
        Recorder.addNewRecorderListener(this);
    }

    public void newRecorder(Recorder recorder) {
        new BridgeRecorderListener(recorder, this);
    }

    private void createConnection() {
        // read the server URL property
        String serverURL = System.getProperty("com.sun.voip.server.WEBSERVER_URL", "http://localhost:8080");
        // parse the password file from the system property.  This may be
        // null if no password file is specified (for insecure deployments)
        String passwordFileName = System.getProperty(PASSWORD_FILE_PROP);
        File passwordFile = null;
        if (passwordFileName != null && passwordFileName.trim().length() > 0) {
            passwordFile = new File(passwordFileName);
        }
        String username = "admin";
        // initialize the login object
        login = new ProgrammaticLogin<WonderlandSession>(serverURL);
        // log in to the server
        logger.info("Logging in");
        session = login.login(username, passwordFile);

        logger.info("Login succeeded, registering repository");
        registerRepository();
        logger.info("Registering repository succeeded");
    }

    private void registerRepository() {
        ServerSessionManager loginInfo = getSessionManager();
        try {
            HttpURL baseURL = new HttpURL(loginInfo.getServerURL());
            baseURL = new HttpURL(baseURL, "webdav/content");

            // get the authentication service for this session
            AuthenticationService as =
                    AuthenticationManager.get(loginInfo.getCredentialManager().getAuthenticationURL());

            // activate the webdav repository for this session
            String authCookieName = as.getCookieName();
            String authCookieValue = as.getAuthenticationToken();
            AuthenticatedWebdavResource wdr =
                    new AuthenticatedWebdavResource(baseURL,
                    authCookieName,
                    authCookieValue);
            WebdavContentCollection root =
                    new WebdavContentCollection(wdr, null) {
                        // don't include the root node in the path

                        @Override
                        public String getPath() {
                            return "";
                        }
                    };
            WebdavContentRepository repo =
                    new WebdavContentRepository(root, loginInfo.getUsername());

            ContentRepositoryRegistry.getInstance().registerRepository(loginInfo, repo);
        } catch (AuthenticationException ae) {
            logger.log(Level.WARNING, "Unable to get auth cookie name", ae);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Unable to start content repository", ioe);
        }

        String dirName = "localRepo/" + loginInfo.getUsername();
        final File userDir = ClientContext.getUserDirectory(dirName);
        localRepo = new FileContentCollection(userDir, null) {

            @Override
            protected URL getBaseURL() {
                try {
                    return userDir.toURI().toURL();
                } catch (MalformedURLException ex) {
                    logger.log(Level.WARNING, "Unable to create local repository", ex);
                    return null;
                }
            }

            @Override
            public String getPath() {
                return "";
            }
        };
        ContentRepositoryRegistry.getInstance().registerLocalRepository(localRepo);
    }

    ServerSessionManager getSessionManager() {
        return session.getSessionManager();
    }
}

