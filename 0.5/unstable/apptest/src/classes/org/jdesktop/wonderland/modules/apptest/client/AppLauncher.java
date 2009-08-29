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
package org.jdesktop.wonderland.modules.apptest.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;

/**
 * Launches the given app repeatedly on a separate thread.
 * Waits for the app to stop running before it launches again.
 */
public class AppLauncher implements Runnable{

    private static final Logger logger = Logger.getLogger(AppLauncher.class.getName());

    private App app;
    private Thread thread;
    private boolean appIsRunning;
    private boolean testStopped;
    private int numLaunches;

    public AppLauncher (App app) {
        this.app = app;
        thread = new Thread(this, "App Test Thread for app " + app.getDisplayName());
    }

    public void startTest () {
        testStopped = false;
        numLaunches = 0;
        thread.start();
    }        

    public void stopTest () {
        testStopped = true;
        synchronized (this) {
            notifyAll();
        }
    }

    public void appIsNoLongerRunning () {
        appIsRunning = false;
        synchronized (this) {
            notifyAll();
        }
    }

    public void run () {

        while (!testStopped) {

            // Launch the app
            app.launch(this);
            appIsRunning = true;
            logger.warning("AppLauncher for app " + app.getDisplayName()  + ": App launch " + 
                           (++numLaunches));

            // Wait until app is stopped or the launch attempt is abandonned.
            while (!testStopped && appIsRunning) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            logger.info("AppLauncher for app " + app.getDisplayName() + ": App is no longer running");
        }

        logger.warning("AppLauncher for app " + app.getDisplayName() + ": Test stopped.");
    }

    public void registerLaunchedCell (Cell cell) {
        if (!(cell instanceof App2DCell)) {
            logger.severe("The launched app " + app.getDisplayName() + " is not an App2DCell.");
            return;
        }
        app.setCell((App2DCell)cell);
        app.setState(App.State.LAUNCH_COMPLETE);
        logger.info("The launched app " + app.getDisplayName() + " has registered with AppTest.");
    }
}
