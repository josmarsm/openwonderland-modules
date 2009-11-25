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

package org.jdesktop.wonderland.modules.movierecorder.client;

import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

/*********************************************
 * UI: The movie recorder control panel
 * @author Bernard Horan
 */

public class ControlPanelUI {

    /** The Swing panel. */
    private MovieControlPanel controlPanel;

    /** The HUD. */
    private HUD mainHUD;

    /** The HUD component for the panel. */
    private HUDComponent hudComponent;

    public ControlPanelUI (final MovieRecorderCell cell) {

        mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        try {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    controlPanel = new MovieControlPanel(cell);
                    hudComponent = mainHUD.createComponent(controlPanel);
                    hudComponent.setPreferredLocation(Layout.SOUTHWEST);
                    mainHUD.addComponent(hudComponent);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot create construct panel");
        }

        

         try {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot add hud component to main hud");
        }

    }

    /** Control the visibility of the window.
     * @param visible if true, show the hud, otherwise hide it
     */
    public void setVisible (final boolean visible) {
        try {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    System.out.append("Setting hud component to be visible");
                    hudComponent.setVisible(visible);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot add hud component to main hud");
        }
        
    }

    void enableLocalButtons() {
        controlPanel.enableLocalButtons();
    }

    MovieControlPanel getControlPanel() {
        return controlPanel;
    }

    void setRemoteRecording(boolean b) {
        controlPanel.setRemoteRecording(b);
    }

}
