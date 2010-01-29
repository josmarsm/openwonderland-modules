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

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.apptest.common.cell.AppTestCellComponentServerState;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.apptest.client.cell.AppTestCell;

public class App {

    private static final Logger logger = Logger.getLogger(AppLauncher.class.getName());

    private static final int LAUNCH_WAIT_TIME_SECS = 45;

    public enum State { NOT_RUNNING, LAUNCHED, LAUNCH_COMPLETE };

    private AppTestCell appTestCell;

    private String displayName;
    private int takeDownSecs;
    private AppLauncher launcher;

    private State state = State.NOT_RUNNING;
    private App2DCell cell;

    private Timer launchTimer;
    private Timer runningTimer;

    public App (AppTestCell appTestCell, String displayName, int takeDownSecs) {
        this.appTestCell = appTestCell;
        this.displayName = displayName;
        this.takeDownSecs = takeDownSecs;
    }

    public String getDisplayName () {
        return displayName;
    }

    // Derived from CellPalette.createActionPerformed
    private void createCell () {
        CellRegistry registry = CellRegistry.getCellRegistry();
        Set<CellFactorySPI> factorySet = registry.getAllCellFactories();
        for (CellFactorySPI spi : factorySet) {
            String spiDisplayName = spi.getDisplayName();
            if (spiDisplayName != null && spiDisplayName.equals(displayName)) {
                CellServerState serverState = spi.getDefaultCellServerState(null);

                // Arrange for cell to be automatically deleted 
                logger.info("cell = " + cell);
                AppTestCellComponentServerState comp = 
                    new AppTestCellComponentServerState(appTestCell.getCellID(), displayName);
                serverState.addComponentServerState(comp);

                try {
                    CellUtils.createCell(serverState);
                } catch (CellCreationException ex) {
                    logger.warning("Cannot create cell for app " + displayName);
                    return;
                }
            }
        } 
    }

    public synchronized void setState (State state) {
        if (this.state == state) return;

        if (!isStateValid(state)) return;

        switch (state) {

        case LAUNCHED:
            // Make sure we don't wait too long for the launch to complete
            startLaunchTimer();
            break;

        case LAUNCH_COMPLETE:
            stopLaunchTimer();
            // Arrange for the app to be taken down in a bit
            startRunningTimer();
            break;

        case NOT_RUNNING:
            stopLaunchTimer();
            stopRunningTimer();
            if (launcher != null) {
                launcher.appIsNoLongerRunning();
            }
            break;
        }

        this.state = state;
    }

    public void setCell (App2DCell cell) {
        this.cell = cell;
    }

    private boolean isStateValid (State state) {
        boolean valid = true;

        switch (state) {

        case LAUNCHED:
            if (this.state != State.NOT_RUNNING) {
                valid = false;
            }
            break;

        case LAUNCH_COMPLETE:
            if (this.state != State.LAUNCHED) {
                valid = false;
            }
            break;

        case NOT_RUNNING:
            break;
        }

        if (!valid) {
            logger.warning("Invalid state change. Current state = " + this.state +
                           ", new state = " + state);
            stop();
        }

        return valid;
    }

    private void startLaunchTimer () {
        logger.info("Start launch timer");
        launchTimer = new Timer();
        launchTimer.schedule(new TimerTask() {
            public void run() {
                logger.info("Launch timer triggered");
                synchronized (App.this) {
                    if (state == State.LAUNCHED) {
                        logger.warning("App " + displayName + " took too long to launch.");
                        stop();
                    }
                }
            }
        }, LAUNCH_WAIT_TIME_SECS * 1000);
    }

    private void stopLaunchTimer () {
        logger.info("Stop launch timer");
        launchTimer.cancel();
    }

    private void startRunningTimer () {
        logger.info("Start running timer");
        runningTimer = new Timer();
        runningTimer.schedule(new TimerTask() {
            public void run() {
                logger.info("Running timer triggered");
                synchronized (App.this) {
                    if (state != State.NOT_RUNNING) {
                        logger.info("Taking down app " + displayName);
                        stop();
                    }
                }
            }
        }, takeDownSecs * 1000);
    }

    private void stopRunningTimer () {
        logger.info("Stop running timer");
        runningTimer.cancel();
    }

    private void deleteCell () {
        if (cell == null) return;
        cell.destroy();
    }

    public synchronized void launch (AppLauncher launcher) {
        this.launcher = launcher;
        createCell();
        setState(State.LAUNCHED);
    }

    public synchronized void stop () {
        if (state == State.NOT_RUNNING) return;

        setState(State.NOT_RUNNING);

        // Take the app down
        deleteCell();
    }
}

