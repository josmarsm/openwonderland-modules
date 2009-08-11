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
import java.util.Date;
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
import org.jdesktop.wonderland.modules.timeline.common.TimelineSegment;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;


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

    private DatedSet sortedSegments = new DatedSet();

    /**
     * Radians per segment. Set from the cell properties screen (not yet)
     */
    private float radsPerSegment = (float) (Math.PI / 4);

    /**
     * The total number of segments. Set from the cell properties screen (not yet)
     */
    private int numSegments = 10;

    /**
     * The pitch of the helix (which is the vertical distance of one complete
     * turn).
     */
    private float pitch = 2.0f;

    /**
     * The height in meters of the helix. 
     *
     * This value is never set, and is only derived from the
     * pitch/radsPerSegment/numSegments values and is updated when those
     * values are updated.
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
    public void moveAvatarToHeightFraction(float position) {

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

    /**
     *
     *
     * @param date The date you want the segment for.
     * @return The segment that contains the specified date.
     */
    public TimelineSegment getSegmentByDate(Date date) {
        throw new UnsupportedOperationException("Not implemented yet.");

        // Trivial call against the dated set when the relevant method comes
        // online.
    }

    /**
     * Given a date, returns the center position for that date.
     * 
     * @param date
     * @return
     */
    public Vector3f getPositionByDate(Date date) {
       // Look up the center of the segment associated with the specified dates.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Given a vector position, returns the matching TimelineDate.
     * 
     * @param pos
     * @return The nearest TimelineDate for the specified position.
     */
    public TimelineDate getDateByPosition(Vector3f pos) {

        // Take the height of that position in the timeline.
        // Figure out the fraction of that height of the total height, and
        // then use that fraction date. (eg if our range is 1990 to 2000 and
        // the position is 2.5m in a 10m spiral, our date is 1992)

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public int getNumSegments() {
        return numSegments;
    }

    public void setNumSegments(int numSegments) {
        this.numSegments = numSegments;
        this.updateHeight();
    }

    public float getRadsPerSegment() {
        return radsPerSegment;
    }

    public void setRadsPerSegment(float radsPerSegment) {
        this.radsPerSegment = radsPerSegment;
        this.updateHeight();
    }

    private void updateHeight() {
        float numTurns = (float) ((radsPerSegment * numSegments) / (Math.PI * 2));
        this.height = numTurns * pitch;
    }

    public void addSegment(TimelineSegment seg) {
        this.sortedSegments.add(seg);
    }

    public void clearSegments() {
        this.sortedSegments.clear();
    }

    public void removeSegment(TimelineSegment seg) {
        this.sortedSegments.remove(seg);
    }

}
