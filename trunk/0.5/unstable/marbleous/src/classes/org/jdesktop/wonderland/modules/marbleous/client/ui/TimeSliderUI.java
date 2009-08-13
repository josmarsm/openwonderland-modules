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
import org.jdesktop.wonderland.modules.marbleous.client.SimTrace;
import org.jdesktop.wonderland.modules.marbleous.client.cell.TrackCell;

/************************************************************
 * TimeSliderUI: The marbleous sim trace investigation slider
 * @author deronj@dev.java.net
 */

public class TimeSliderUI {

    /** The Cell. */
    private TrackCell cell;

    /** The Swing panel. */
    private TimeSliderPanel panel;

    /** The HUD. */
    private HUD mainHUD;

    /** The HUD component for the panel. */
    private HUDComponent hudComponent;

    public TimeSliderUI (final TrackCell cell) {
        this.cell = cell;

        mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        try {
            SwingUtilities.invokeAndWait(new Runnable () {
                public void run () {
                    panel = new TimeSliderPanel(cell);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot create time sliderpanel");
        }
        //constructPanel.setTrack(track);

        hudComponent = mainHUD.createComponent(panel);
        hudComponent.setPreferredLocation(Layout.SOUTH);

        mainHUD.addComponent(hudComponent);
    }

    public void setSimTrace (SimTrace trace) {
        panel.setSimTrace(trace);
    }

    /** Control the visibility of the window. */
    public void setVisible (boolean visible) {
        System.err.println("***** setVisible = " + visible);

        hudComponent.setVisible(visible);
    }
}
