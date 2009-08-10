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
import com.jme.math.Quaternion;
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
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
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

    private AvatarCell localAvatarCell;

    /**
     * The pitch of the helix (which is the vertical distance of one complete
     * turn).
     */
    private float pitch = 2.0f;

    /**
     * The height in meters of the helix. (There are lots of alternative ways
     * to represent this - could be the number of turns or just the raw number
     * of radians the helix covers. They're interchangeable, though, so I just
     * picked the one that seems the most accessible to users. If we want to
     * switch go something that is easier for all of us to work with internally,
     * that's super easy to do.)
     */
    private float height = 30.0f;

    public TimelineCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        
        if(status==CellStatus.ACTIVE && increasing) {

            this.setLocalBounds(new BoundingBox(Vector3f.ZERO, 100.0f, 100.0f, 100.0f));

            BoundingVolume[] bounds = new BoundingVolume[]{this.getLocalBounds()};
            prox.addProximityListener(this, bounds);

            localAvatarCell = (AvatarCell)getCellCache().getViewCell();
 
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

                navigationHUD = mainHUD.createComponent(new TimelineMovementHUDPanel(this));                
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

    /**
     *
     * @param position A nondimensionalized variable representing where in the timeline the avatar should move to.
     */
    public void moveAvatar(float position) {

        // Move the avatar on their current radius; ie no need to detect which
        // layer/track they're on, just pick their current distance to 0,0
        // and use that as the radius. 

        Vector3f avPosition = localAvatarCell.getWorldTransform().getTranslation(Vector3f.ZERO);

        Vector3f cellPositionAtHeight = this.getWorldTransform().getTranslation(null);

        // Set the heights the same so we can get just the distance to the center, not the
        // distance to the actual center of the cell.
        cellPositionAtHeight.y = avPosition.y;
        
        float radius = avPosition.distance(cellPositionAtHeight);

        // Given the fraction up the helix we want to be (position), figure out
        // what the angle (t) of that is.

        // Since the height = pitch*t, just divide it to get the number of turns,
        // and the angle is 2PI times that.
        float targetHeight = (position * height);
        float angle = (float) ((float) (targetHeight / (float)pitch) * 2.0f * Math.PI);

        Vector3f positionRelativeToCell = new Vector3f((float)(radius * Math.sin(angle)), targetHeight, (float)(radius * Math.cos(angle)));

        // This is definitely not quite right - the height of the cell is not
        // interesting to us and we should be knocking it out. But we do care
        // about x,z positions, since the avatar isn't a child of the timeline
        // and so needs to have its world positions set.
        Vector3f targetPosition = this.getWorldTransform().getTranslation(null).add(positionRelativeToCell);

        logger.warning("r=" + radius + "; moving avatar to position: " + targetPosition + " (" + position + ")");

        localAvatarCell.triggerGoto(targetPosition, new Quaternion());
    }
}
