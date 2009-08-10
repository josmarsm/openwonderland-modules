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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.timeline.client.jme.cell.TimelineCellRenderer;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellChangeMessage;


/**
 *
 *  
 */
public class TimelineCell extends Cell implements ProximityListener {

    private static final Logger logger =
        Logger.getLogger(TimelineCell.class.getName());

    TimelineCellRenderer renderer = null;

    @UsesCellComponent
    private ProximityComponent prox;

    private HUD mainHUD;
    private HUDComponent navigationHUD;
    
    public TimelineCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        
        if(status==CellStatus.ACTIVE && increasing) {

            this.setLocalBounds(new BoundingBox(Vector3f.ZERO, 10.0f, 10.0f, 10.0f));

            BoundingVolume[] bounds = new BoundingVolume[]{this.getLocalBounds()};
            prox.addProximityListener(this, bounds);
 
        } else if (status==CellStatus.DISK && !increasing) {
            prox.removeProximityListener(this);
        }
    }

    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {

        // If the person entering is the local avatar...
        if (cell.getCellCache().getViewCell().getCellID() == viewCellID) {
            if (entered) {
                // Add the navigation HUD.
                mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

                navigationHUD = mainHUD.createComponent(new TimelineMovementHUDPanel());
                
                navigationHUD.setPreferredLocation(Layout.EAST);
                navigationHUD.setName("Navigation");

                mainHUD.addComponent(navigationHUD);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        navigationHUD.setVisible(true);
                    }
                });

            } else {
                // Remove the navigation HUD.
                mainHUD.removeComponent(navigationHUD);
            }

        }
    }

    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new TimelineCellRenderer(this);
            return this.renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    class TimelineCellMessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            TimelineCellChangeMessage msg = (TimelineCellChangeMessage)message;

            // handle message
        }
    }
}
