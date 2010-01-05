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
package org.jdesktop.wonderland.modules.sessiontest.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Client-side plugin for testing Wonderland sessions
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class SessionTestClientPlugin extends BaseClientPlugin
        implements SessionLifecycleListener, SessionStatusListener,
        ViewManagerListener {

    private static Logger logger =
            Logger.getLogger(SessionTestClientPlugin.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ServerSessionManager loginInfo) {
        logger.warning("SESSION TEST: INITIALIZE SERVER SESSION MANGER " +
                loginInfo.toString());
        loginInfo.addLifecycleListener(this);
        super.initialize(loginInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void activate() {
        logger.warning("SESSION TEST: ACTIVATE PRIMARY SESSION MANAGER " +
                getSessionManager().toString());
        logger.warning("SESSION TEST: PRIMARY VIEW CELL " +
                ViewManager.getViewManager().getPrimaryViewCell());
        ViewManager.getViewManager().addViewManagerListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deactivate() {
        logger.warning("SESSION TEST: DEACTIVATE PRIMARY SESSION MANAGER " +
                getSessionManager().toString());
        ViewManager.getViewManager().removeViewManagerListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        logger.warning("SESSION TEST: CLEANUP SERVER SESSION MANGER");
        getSessionManager().removeLifecycleListener(this);
        super.cleanup();
    }

    /**
     * @inheritDoc()
     */
    public void sessionCreated(WonderlandSession session) {
        logger.warning("SESSION TEST: SESSION CREATED " + session.toString());
    }

    /**
     * @inheritDoc()
     */
    public void primarySession(WonderlandSession session) {
        logger.warning("SESSION TEST: SESSION IS PRIMARY " + session);
        if (session==null) {
        } else {
            session.addSessionStatusListener(this);
        }
    }

    /**
     * @inheritDoc()
     */
    public void sessionStatusChanged(WonderlandSession session, Status status) {
        logger.warning("SESSION TEST: SESSION STATUS CHANGED " +
                session.toString() + " TO " + status);
    }

    public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {
        logger.warning("SESSION TEST: PRIMARY VIEW CHANGED FROM " +
                oldViewCell + " TO " + newViewCell);
    }
}
