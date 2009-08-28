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

import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

@ExperimentalAPI
public class AppTest implements Runnable {

    private static final Logger logger = Logger.getLogger(AppTest.class.getName());

    private LinkedList<App> apps = new LinkedList<App>();

    private Thread thread;
    private boolean stop;

    private class App {
        private String displayName;
        private int takeDownSecs;
        private Timer takeDownTimer = new Timer();
        private Cell appCell;

        private App (String displayName, int takeDownSecs) {
            this.displayName = displayName;
            this.takeDownSecs = takeDownSecs;
        }

        private void launch () {
            createCell();
            if (appCell == null) {
                logger.severe("Could not launch app " + displayName);
                return;
            }

            startTakeDownTimer();
        }

        // Derived from CellPalette.createActionPerformed
        private void createCell () {
            CellRegistry registry = CellRegistry.getCellRegistry();
            Set<CellFactorySPI> factorySet = registry.getAllCellFactories();
            for (CellFactorySPI spi : factorySet) {
                String spiDisplayName = spi.getDisplayName();
                if (spiDisplayName != null && spiDisplayName.equals(displayName)) {
                    CellServerState state = spi.getDefaultCellServerState(null);
                    try {
                        CellUtils.createCell(state);
                    } catch (CellCreationException ex) {
                        appCell = null;
                        return;
                    }
                }
            } 

            // TODO: how do I get the cell?
            appCell = null;
        }

        private void startTakeDownTimer () {
            takeDownTimer.schedule(new TimerTask() {
                public void run() {
                    App.this.deleteCell();
                }
            }, takeDownSecs * 1000);
        }

        private void stopTakeDownTimer () {
            takeDownTimer.cancel();
        }

        private synchronized void deleteCell () {
            if (appCell == null) return;
        }
    }

    public AppTest () {
        boolean isMaster = determineMaster();
        if (!isMaster) {
            // Do nothing;
            return;
        }

        // Initialize the list of apps to be launched during the test
        apps.add(new App("gt", 20));
    }

    /** Start the test. */
    public synchronized void start () {
        logger.warning("AppTest started.");
        stop = false;

        // Start the thread
        thread = new Thread(this, "App Test Thread");
        thread.start();
    }


    /** Stop the test. */
    public synchronized void stop () {
        stop = true;
        try {
            thread.join();
        } catch (InterruptedException ex) {
           logger.warning("AppTest stopped.");
        }
    }
    
    // TODO: make this ask the server who is the master
    private boolean determineMaster () {
        // TODO: temp
        return true;
    }

    public void run () {
        while (!stop) {
            test();
            testCleanup();
        }
    }

    /** 
     * Launch one of each of the apps in the list. For each app launched, a timer of the specified
     * duration is started which will take the app down when it expires.
     */
    public void test () {
        for (App app : apps) {
            app.launch();
        }
    }

    public void testCleanup () {
        for (App app : apps) {
            app.stopTakeDownTimer();
            app.deleteCell();
        }
    }
    
}
