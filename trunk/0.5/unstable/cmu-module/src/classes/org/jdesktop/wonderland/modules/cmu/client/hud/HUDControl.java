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
package org.jdesktop.wonderland.modules.cmu.client.hud;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jdesktop.wonderland.modules.cmu.client.events.SceneTitleChangeListener;
import org.jdesktop.wonderland.modules.cmu.client.events.SceneTitleChangeEvent;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.modules.cmu.client.CMUCell;
import org.jdesktop.wonderland.modules.cmu.client.CMUCell.ConnectionState;

/**
 *
 * @author kevin
 */
public class HUDControl implements HUDEventListener, SceneTitleChangeListener {

    private final CMUCell parentCell;

    // UI stuff
    private CMUPanel hudPanel = null;
    private final JPanel hudContainer = new JPanel();
    private final ActiveHUD activePanel;
    private final DisconnectedHUD disconnectedPanel;
    private final LoadingHUD loadingPanel;
    private HUDComponent hudComponent = null;
    private boolean componentSizeSet = false;
    private boolean hudShowing = false;
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private final Object hudShowingLock = new Object();

    public HUDControl(CMUCell parentCell) {
        this.parentCell = parentCell;
        parentCell.addSceneTitleChangeListener(this);

        activePanel = new ActiveHUD(parentCell);
        disconnectedPanel = new DisconnectedHUD();
        loadingPanel = new LoadingHUD(parentCell);

        hudContainer.setLayout(new GridLayout());
        hudContainer.add(activePanel);
    }

    private class HUDDisplayer implements Runnable {

        private final boolean showing;

        public HUDDisplayer(boolean showing) {
            this.showing = showing;
        }

        @Override
        public void run() {
            synchronized (hudShowingLock) {
                // Set up UI

                final CMUPanel oldHUDPanel = hudPanel;
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

                // Reload HUD if necessary
                if (oldHUDPanel != hudPanel) {
                    hudContainer.removeAll();
                    hudContainer.add(hudPanel);
                    hudContainer.repaint();
                }

                if (showing && hudComponent == null) {
                    // Create the HUD component
                    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                    assert mainHUD != null;
                    hudComponent = mainHUD.createComponent(hudContainer);
                    hudComponent.setPreferredTransparency(0.0f);
                    hudComponent.setName(parentCell.getSceneTitle());
                    hudComponent.setPreferredLocation(Layout.NORTHWEST);
                    hudComponent.addEventListener(HUDControl.this);
                    mainHUD.addComponent(hudComponent);
                }
                if (hudComponent != null) {
                    hudComponent.setVisible(showing);
                }
                hudShowing = showing;

                if (!componentSizeSet && hudPanel == activePanel && hudShowing) {
                    hudContainer.setPreferredSize(new Dimension(hudContainer.getWidth(), hudContainer.getHeight()));
                    componentSizeSet = true;
                }
            }
        }
    }

    private class HUDKiller extends HUDDisplayer {

        public HUDKiller() {
            super(false);
        }

        @Override
        public void run() {
            synchronized (hudShowingLock) {
                super.run();

                //TODO: Remove from HUD manager?
                hudComponent = null;
            }
        }
    }

    public void setConnectionState(ConnectionState state) {
        synchronized (hudShowingLock) {
            this.connectionState = state;
            
            // Make a specific runnable, so that we make sure we've got the
            // correct showing state.
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    synchronized (hudShowingLock) {
                        new HUDDisplayer(isHUDShowing()).run();
                    }
                }
            });
        }
    }

    public void setHUDShowing(boolean showing) {
        synchronized (hudShowingLock) {
            if (showing != isHUDShowing() || hudComponent == null) {
                SwingUtilities.invokeLater(new HUDDisplayer(showing));
            }
        }
    }

    public boolean isHUDShowing() {
        synchronized (hudShowingLock) {
            return hudShowing;
        }
    }

    public void unloadHUD() {
        SwingUtilities.invokeLater(new HUDKiller());
    }

    public void HUDObjectChanged(HUDEvent event) {
        synchronized (hudShowingLock) {
            if (event.getObject().equals(this.hudComponent)) {
                if (event.getEventType().equals(HUDEventType.DISAPPEARED) || event.getEventType().equals(HUDEventType.CLOSED)) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            if (!hudComponent.isVisible()) {
                                new HUDDisplayer(false).run();
                            }
                        }
                    });
                }
            }
        }
    }

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
