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
package org.jdesktop.wonderland.modules.timeline.client;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellServerState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;

/**
 *
 *  
 */
@CellFactory
public class TimelineCellFactory implements CellFactorySPI {

    private static final Logger logger =
            Logger.getLogger(TimelineCellFactory.class.getName());
    private TimelineCreationHUDPanel creationPanel;
    private HUDComponent timelineCreationHUD;

    public String[] getExtensions() {
        return null;
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {

        TimelineCellServerState state = new TimelineCellServerState();
        createCreationHUD();
        state.setConfig(new TimelineConfiguration());
        return (T) state;
    }

    private void createCreationHUD() {
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

        creationPanel = new TimelineCreationHUDPanel();
        creationPanel.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pe) {
                if ((pe.getPropertyName().equals("create")) || (pe.getPropertyName().equals("update"))) {
                    logger.info("--- create/update timeline");
                    timelineCreationHUD.setVisible(false);
                    // TODO: actually create a Timeline!
                } else if (pe.getPropertyName().equals("cancel")) {
                    // timeline creation was canceled
                    timelineCreationHUD.setVisible(false);
                }
            }
        });
        timelineCreationHUD = mainHUD.createComponent(creationPanel);
        timelineCreationHUD.setPreferredLocation(Layout.CENTER);
        timelineCreationHUD.setName("Create Timeline");
        mainHUD.addComponent(timelineCreationHUD);
        timelineCreationHUD.setVisible(true);
    }

    public String getDisplayName() {
        return "Timeline";
    }

    public Image getPreviewImage() {
        return null;
    }
}
