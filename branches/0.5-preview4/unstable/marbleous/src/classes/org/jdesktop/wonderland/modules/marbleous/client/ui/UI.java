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

package org.jdesktop.wonderland.modules.marbleous.client.ui;

import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.modules.marbleous.client.cell.TrackCell;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimulationStateMessage.SimulationState;

/*********************************************
 * UI: The marbleous control window
 * @author deronj@dev.java.net
 */

public class UI {

    /** The Swing panel. */
    private ConstructPanel constructPanel;

    /** The HUD. */
    private HUD mainHUD;

    /** The HUD component for the panel. */
    private HUDComponent hudComponent;

    public UI (final TrackCell cell, final TimeSliderUI uiTimeSlider) {

        mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        try {
            SwingUtilities.invokeAndWait(new Runnable () {
                public void run () {
                    constructPanel = new ConstructPanel(cell, uiTimeSlider);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot create construct panel");
        }
        //constructPanel.setTrack(track);

        hudComponent = mainHUD.createComponent(constructPanel);
        hudComponent.setPreferredLocation(Layout.SOUTHWEST);
        hudComponent.setName("Marbleous Control Panel");

        mainHUD.addComponent(hudComponent);
    }

    /** Control the visibility of the window. */
    public void setVisible (boolean visible) {
        hudComponent.setVisible(visible);
    }

    /**
     * Tells the control panel that another client has started/stopped/reset the
     * simulation
     */
    public void externalSimulationState(SimulationState state) {
        constructPanel.externalSimulationState(state);
    }
}
