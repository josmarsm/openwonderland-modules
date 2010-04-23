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
package org.jdesktop.wonderland.modules.cmu.client.ui.hud;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.SceneTitleChangeListener;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.SceneTitleChangeEvent;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.modules.cmu.client.CMUCell;
import org.jdesktop.wonderland.modules.cmu.client.CMUCell.ConnectionState;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.ConnectionStateChangeEvent;
import org.jdesktop.wonderland.modules.cmu.client.events.cmu.ConnectionStateChangeListener;

/**
 * Class to manage the HUD for a CMU Cell; listens to changes in
 * the cell's connection state, and updates the HUD accordingly.
 * @author kevin
 */
public class HUDControl implements HUDEventListener, SceneTitleChangeListener, ConnectionStateChangeListener {

    private final CMUCell parentCell;

    // UI stuff
    private CMUPanel hudPanel = null;
    private final JPanel hudContainer = new JPanel();
    private final ActiveHUD activePanel;
    private final DisconnectedHUD disconnectedPanel;
    private final LoadingHUD loadingPanel;
    @UsesCellComponent
    private HUDComponent hudComponent = null;
    private boolean componentSizeSet = false;
    private boolean hudShowing = false;
    private ConnectionState connectionState = null;
    private final Object hudShowingLock = new Object();

    /**
     * Standard constructor.
     * @param parentCell The cell whose HUD this object is controlling
     */
    public HUDControl(CMUCell parentCell) {
        this.parentCell = parentCell;
        parentCell.addSceneTitleChangeListener(this);
        parentCell.addConnectionStateChangeListener(this);

        connectionState = parentCell.getConnectionState();

        activePanel = new ActiveHUD(parentCell);
        disconnectedPanel = new DisconnectedHUD();
        loadingPanel = new LoadingHUD(parentCell);
    }

    /**
     * Runnable to set the showing state of the HUD according to the current
     * connection state of the cell which this HUDControl is controlling.
     */
    protected class HUDDisplayer implements Runnable {

        private final boolean showing;

        /**
         * Standard constructor.
         * @param showing The visibility which this HUDDisplayer should enforce
         */
        public HUDDisplayer(boolean showing) {
            this.showing = showing;
        }

        /**
         * Load the appropriate panel into the HUD based on the current
         * connection state of the HUDControl, and set the HUD visibility
         * appropriately.
         */
        @Override
        public void run() {
            final CMUPanel oldHUDPanel = hudPanel;

            synchronized (hudShowingLock) {
                // Choose the panel
                if (connectionState == ConnectionState.DISCONNECTED) {
                    hudPanel = disconnectedPanel;
                } else if (connectionState == ConnectionState.WAITING ||
                        connectionState == ConnectionState.LOADING ||
                        connectionState == ConnectionState.RECONNECTING) {
                    hudPanel = loadingPanel;
                } else if (connectionState == ConnectionState.LOADED) {
                    hudPanel = activePanel;
                } else {
                    Logger.getLogger(HUDControl.HUDDisplayer.class.getName()).
                            log(Level.SEVERE, "Unrecognized connection state: " + connectionState);
                }
            }

            // Reload HUD if necessary
            if (oldHUDPanel != hudPanel) {
                hudContainer.removeAll();
                hudContainer.add(hudPanel);
                hudContainer.repaint();
            }
            boolean hudComponentNull = false;
            synchronized (hudShowingLock) {
                hudComponentNull = (hudComponent == null);
            }
            if (showing && hudComponentNull) {
                // Create the HUD component
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                assert mainHUD != null;
                hudComponent = mainHUD.createComponent(hudContainer);
                hudComponent.setPreferredTransparency(0.0f);
                hudComponent.setName(parentCell.getSceneTitle());
                hudComponent.setPreferredLocation(Layout.NORTHEAST);
                hudComponent.addEventListener(HUDControl.this);
                mainHUD.addComponent(hudComponent);
            }
            synchronized (hudShowingLock) {
                hudComponentNull = (hudComponent == null);
            }
            if (!hudComponentNull) {
                hudComponent.setVisible(showing);
            }
            synchronized (hudShowingLock) {
                hudShowing = showing;
            }

            if (!componentSizeSet && hudPanel instanceof ActiveHUD && showing) {
                //TODO: Calculate size dynamically
                //hudContainer.setPreferredSize(new Dimension(hudContainer.getWidth(), hudContainer.getHeight()));
                hudContainer.setLayout(new GridLayout());
                hudContainer.setPreferredSize(new Dimension(194, 82));
                componentSizeSet = true;
            }
        }
    }

    /**
     * Unload the HUD and perform necessary cleanup to get rid of it.
     */
    protected class HUDKiller extends HUDDisplayer {

        /**
         * Standard constructor.
         */
        public HUDKiller() {
            super(false);
        }

        /**
         * Make the HUD invisible, and sever ties with the current
         * HUD component of this HUDControl.
         */
        @Override
        public void run() {
            synchronized (hudShowingLock) {
                super.run();

                //TODO: Remove from HUD manager?
                hudComponent = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectionStateChanged(ConnectionStateChangeEvent e) {
        if (e.getCell().equals(this.parentCell)) {
            setConnectionState(e.getConnectionState());
        }
    }

    /**
     * Set the connection state and update the HUD accordingly.
     * @param state The new connection state
     */
    public void setConnectionState(ConnectionState state) {
        synchronized (hudShowingLock) {
            this.connectionState = state;

            // Make an explicit runnable, so that we make sure we've got the
            // correct showing state once the runnable is run.
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    synchronized (hudShowingLock) {
                        new HUDDisplayer(isHUDShowing()).run();
                    }
                }
            });
        }
    }

    /**
     * Turn the HUD visibility on or off.
     * @param showing Whether the HUD is visible
     */
    public void setHUDShowing(boolean showing) {
        synchronized (hudShowingLock) {
            if (showing != isHUDShowing() || hudComponent == null) {
                SwingUtilities.invokeLater(new HUDDisplayer(showing));
            }
        }
    }

    /**
     * Find out whether the HUD is showing.
     * @return Whether the HUD is showing
     */
    public boolean isHUDShowing() {
        synchronized (hudShowingLock) {
            return hudShowing;
        }
    }

    /**
     * Destroy the HUD and stop listening for changes from the CMU cell.
     */
    public void unloadHUD() {
        parentCell.removeConnectionStateChangeListener(this);
        parentCell.removeSceneTitleChangeListener(this);
        SwingUtilities.invokeLater(new HUDKiller());
    }

    /**
     * If the HUD has been closed, mark that this is the case with a HUDDisplayer.
     * @param event {@inheritDoc}
     */
    public void HUDObjectChanged(HUDEvent event) {
        synchronized (hudShowingLock) {
            if (event.getObject().equals(this.hudComponent)) {
                if (event.getEventType().equals(HUDEventType.DISAPPEARED) || event.getEventType().equals(HUDEventType.CLOSED)) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            // Check to make sure the HUD is still not visible;
                            // this might have changed between the time the HUD
                            // was closed and the time the runnable executes
                            if (hudComponent != null && !hudComponent.isVisible()) {
                                new HUDDisplayer(false).run();
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sceneTitleChanged(SceneTitleChangeEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (hudComponent != null) {
                    hudComponent.setName(parentCell.getSceneTitle());
                }
            }
        });
    }
}
