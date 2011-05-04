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
package org.jdesktop.wonderland.modules.ezscript.client.headless;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellCacheBasicImpl;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveListener;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveSource;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.MainFrame;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.LoginUI;
import org.jdesktop.wonderland.client.login.PluginFilter;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager.EitherLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.NoAuthLoginControl;
import org.jdesktop.wonderland.client.login.ServerSessionManager.UserPasswordLoginControl;
import org.jdesktop.wonderland.client.login.SessionCreator;
import org.jdesktop.wonderland.common.JarURI;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * A test client that simulates a 3D client
 */
public class Client3DSim
        implements RequestProcessor, SessionStatusListener {

    /** a logger */
    private static final Logger logger =
            Logger.getLogger(Client3DSim.class.getName());
    private static final Logger messageTimerLogger =
            Logger.getLogger(MessageTimer.class.getName());
    /** the name of this client */
    private String username;
    /** the session we are attached to */
    private CellClientSession session;
    /** the mover thread */
    private UserSimulator userSim;
    private MessageTimer messageTimer = null;// = new MessageTimer();

    public Client3DSim() {
    }

    public String getName() {
        return "Client3DSim";
    }

    public void initialize(String username, Properties props, ReplySender replyHandler)
            throws ProcessingException {
        this.username = username;
        messageTimer = new MessageTimer(username);

        // set the user directory to one specific to this client
        File userDir = new File(ClientContext.getUserDirectory("test"),
                username);

        ClientContext.setUserDirectory(userDir, true);
        ClientContext.setRendererType(RendererType.NONE);

        // set up the login system to

        // read the server URL from a property
        String serverURL = props.getProperty("serverURL");
        if (serverURL == null) {
            throw new ProcessingException("No serverURL found");
        }

        // set the login callback to give the right user name
        LoginManager.setLoginUI(new HeadlessClientLoginUI(username, props));
        logger.warning("Created mock LoginUI.");
        // for now, load all plugins.  We should modify this to only load
        // some plugins, depending on the test
        LoginManager.setPluginFilter(new BlacklistPluginFilter());
        logger.warning("Downloading plugins...");
        // create a fake mainframe
        JmeClientMain.setFrame(new FakeMainFrame());
        logger.warning("Created fake Frame.");

        try {        
            ServerSessionManager mgr = LoginManager.getSessionManager(serverURL);
            session = mgr.createSession(new SessionCreator<CellClientSession>() {

                public CellClientSession createSession(ServerSessionManager sessionMgr,
                        WonderlandServerInfo serverInfo, ClassLoader loader) {
                    logger.warning("INSIDE CREATESESSION");
                    CellClientSession ccs = new CellClientSession(sessionMgr, serverInfo, loader) {

                        @Override
                        protected CellCache createCellCache() {
                            CellCacheBasicImpl impl = new CellCacheBasicImpl(this,
                                    getClassLoader(), getCellCacheConnection(),
                                    getCellChannelConnection()) {

                                @Override
                                protected CellRenderer createCellRenderer(Cell cell) {
                                    logger.warning("INSIDE createCellRenderer");
                                    return null;
                                }
                            };

                            getCellCacheConnection().addListener(impl);
                            return impl;
                        }
                    };
                    ccs.addSessionStatusListener(Client3DSim.this);

                    final LocalAvatar avatar = ccs.getLocalAvatar();
                    avatar.addViewCellConfiguredListener(new ViewCellConfiguredListener() {

                        public void viewConfigured(LocalAvatar localAvatar) {
//                            MovableComponent mc =
//                                    avatar.getViewCell().getComponent(MovableComponent.class);
//                            mc.addServerCellMoveListener(messageTimer);

                            // start the simulator
                            logger.warning("VIEW CONFIGURED!");
                            userSim.start();
                        }
                    });
                    userSim = new UserSimulator(avatar);
                    logger.warning("END OF CREATESESSION");
                    return ccs;
                }
            });
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new ProcessingException(ioe);
        } catch (LoginFailureException lfe) {
            lfe.printStackTrace();
            throw new ProcessingException(lfe);
        }
    }

    public void destroy() {
        if (session != null) {
            session.logout();
        }
    }

    public void processRequest(TestRequest request) {
        if (request instanceof Client3DRequest) {
            processClient3DRequest((Client3DRequest) request);
        } else {
            Logger.getAnonymousLogger().severe("Unsupported request " + request.getClass().getName());
        }
    }

    private void processClient3DRequest(Client3DRequest request) {
        switch (request.getAction()) {
            case WALK:
                userSim.walkLoop(request.getDesiredLocations(), new Vector3f(1f, 0f, 0f), request.getSpeed(), request.getLoopCount());
                break;
            default:
                Logger.getAnonymousLogger().severe("Unsupported Client3DRequest " + request.getAction());
        }
    }

    public String getUsername() {
        return username;
    }

    public void sessionStatusChanged(WonderlandSession session,
            Status status) {
        logger.info(getName() + " change session status: " + status);
        if (status == Status.DISCONNECTED && userSim != null) {
            userSim.quit();
        }
    }

    public void waitForFinish() throws InterruptedException {
        if (userSim == null) {
            return;
        }

        // wait for the thread to end
        userSim.join();
    }
}