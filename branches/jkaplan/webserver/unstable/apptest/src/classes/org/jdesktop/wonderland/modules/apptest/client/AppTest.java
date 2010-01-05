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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.modules.apptest.client.cell.AppTestCell;

@ExperimentalAPI
public class AppTest {

    private static final Logger logger = Logger.getLogger(AppTest.class.getName());

    private LinkedList<App> apps = new LinkedList<App>();
    private HashMap<String,AppLauncher> appLaunchers = new HashMap<String,AppLauncher>();
    
    private AppTestCell appTestCell;

    private boolean testIsRunning;

    public AppTest (AppTestCell appTestCell) {
        this.appTestCell = appTestCell;

        boolean isMaster = determineMaster();
        if (!isMaster) {
            // Do nothing;
            return;
        }

        // Initialize the list of apps to be launched during the test
        // NOTE: in this test, the display names of the apps must be unique.
        //apps.add(new App(appTestCell, "gt", 20));
        apps.add(new App(appTestCell, "gt", 5));
        //apps.add(new App(appTestCell, "Swing Test Cell", 5));
    }

    /** 
     * Spawn a unique thread to repeatedly launch ach of the apps in the list. The test will run
     * until stopTest is called.
     */
    // TODO: need to make sure that another person doesn't try to start a test which is already running
    public synchronized void startTest () {
        testIsRunning = true;

        for (App app : apps) {
            AppLauncher appLauncher = new AppLauncher(app);
            appLaunchers.put(app.getDisplayName(), appLauncher);
            appLauncher.startTest();
        }

        logger.warning("AppTest started for all apps.");
    }


    /** Stop the test. */
    public synchronized void stopTest () {
        if (!testIsRunning) return;

        for (String displayName : appLaunchers.keySet()) {
            AppLauncher appLauncher = appLaunchers.get(displayName);
            appLauncher.stopTest();
            appLaunchers.remove(displayName);
        }

        logger.warning("AppTest terminated for all apps.");
    }
    
    // TODO: make this ask the server who is the master
    private boolean determineMaster () {
        // TODO: temp
        return true;
    }

    public void registerLaunchedCell (String displayName, Cell cell) {
        AppLauncher appLauncher = appLaunchers.get(displayName);
        if (appLauncher == null) {
            logger.severe("Cannot register launched app " + displayName);
            return;
        }

        appLauncher.registerLaunchedCell(cell);
    }
}
