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
package org.jdesktop.wonderland.modules.mediaboard.client;

import java.util.logging.Logger;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UpdateManagerListener;

/**
 *
 * @author bh37721
 * @author nsimpson
 */
public class WhiteboardUpdateManagerListener implements UpdateManagerListener {

    private static final Logger logger =
            Logger.getLogger(WhiteboardUpdateManagerListener.class.getName());
    private WhiteboardApp whiteboardApp;

    WhiteboardUpdateManagerListener(WhiteboardApp whiteboardApp) {
        this.whiteboardApp = whiteboardApp;
    }

    /**
     * Called when the manager was started.
     */
    public void managerStarted(UpdateManagerEvent e) {
        logger.fine("whiteboard: manager started: " + e);
    }

    /**
     * Called when the manager was suspended.
     */
    public void managerSuspended(UpdateManagerEvent e) {
        logger.fine("whiteboard: manager suspended: " + e);
    }

    /**
     * Called when the manager was resumed.
     */
    public void managerResumed(UpdateManagerEvent e) {
        logger.fine("whiteboard: manager resumed: " + e);
    }

    /**
     * Called when the manager was stopped.
     */
    public void managerStopped(UpdateManagerEvent e) {
        logger.fine("whiteboard: manager stopped: " + e);
    }

    /**
     * Called when an update started.
     */
    public void updateStarted(UpdateManagerEvent e) {
        logger.fine("whiteboard: update started: " + e);
    }

    /**
     * Called when an update was completed.
     */
    public void updateCompleted(UpdateManagerEvent e) {
        logger.info("whiteboard: update completed:" + e);
        whiteboardApp.repaintCanvas();
    }

    /**
     * Called when an update failed.
     */
    public void updateFailed(UpdateManagerEvent e) {
        logger.fine("whiteboard: update failed: " + e);
    }
}
