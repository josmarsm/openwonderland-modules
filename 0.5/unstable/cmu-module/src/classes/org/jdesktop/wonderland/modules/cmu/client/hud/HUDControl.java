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

/**
 *
 * @author kevin
 */
public class HUDControl implements HUDEventListener, SceneTitleChangeListener {

    private final CMUCell parentCell;

    // UI stuff
    private ActiveHUD hudPanel = null;
    private HUDComponent hudComponent = null;
    private boolean hudShowing = false;
    private final Object hudShowingLock = new Object();

    public HUDControl(CMUCell parentCell) {
        this.parentCell = parentCell;
        parentCell.addSceneTitleChangeListener(this);
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
                if (showing && hudComponent == null) {
                    // Create the panel
                    hudPanel = new ActiveHUD(parentCell);

                    // Create the HUD component
                    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                    assert mainHUD != null;
                    hudComponent = mainHUD.createComponent(hudPanel);
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

    public void updateHUD() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (hudComponent != null) {
                    hudComponent.setName(parentCell.getSceneTitle());
                }
            }
        });
    }

    public void setHUDShowing(boolean showing) {
        SwingUtilities.invokeLater(new HUDDisplayer(showing));
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
                    this.setHUDShowing(false);
                }
            }
        }
    }
    
    public void sceneTitleChanged(SceneTitleChangeEvent e) {
        updateHUD();
    }
}
