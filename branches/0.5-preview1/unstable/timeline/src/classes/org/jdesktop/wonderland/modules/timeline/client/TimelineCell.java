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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.ProximityComponent;
import org.jdesktop.wonderland.client.cell.ProximityListener;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.timeline.client.audio.TimelineAudioComponent;
import org.jdesktop.wonderland.modules.timeline.client.jme.cell.TimelineCellRenderer;
import org.jdesktop.wonderland.modules.timeline.client.provider.TimelineProviderComponent;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellChangeMessage;
import org.jdesktop.wonderland.modules.timeline.common.TimelineCellClientState;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration.TimelineUnits;
import org.jdesktop.wonderland.modules.timeline.common.TimelineSegment;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedNews;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedSet;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;

/**
 *
 *  
 */
public class TimelineCell extends Cell implements ProximityListener, TransformChangeListener {

    private static final Logger logger =
            Logger.getLogger(TimelineCell.class.getName());
    TimelineCellRenderer renderer = null;
    @UsesCellComponent
    private ProximityComponent prox;
    @UsesCellComponent
    private ContextMenuComponent menuComponent;
    private SimpleContextMenuItem editTimelineMenuItem;
    private SimpleContextMenuItem curateTimelineMenuItem;
    private HUD mainHUD;
    private TimelineCreationHUDPanel creationPanel;
    private HUDComponent timelineCreationHUD;
    private TimelineCurationHUDPanel curationPanel;
    private HUDComponent timelineCurationHUD;
    private TimelineMovementHUDPanel navigationPanel;
    private HUDComponent navigationHUD;
    private AvatarCell localAvatarCell;
    private DatedSet sortedSegments = new DatedSet();
    private TimelineClientConfiguration config;
    private TimelineSegment curSegment;

    @UsesCellComponent
    private TimelineProviderComponent provider;

    @UsesCellComponent
    private TimelineAudioComponent audio;


    public TimelineCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.ACTIVE && increasing) {
            mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");

            ChannelComponent channel = getComponent(ChannelComponent.class);
            channel.addMessageReceiver(TimelineCellChangeMessage.class, new TimelineCellMessageReceiver());

            // TODO matt
            // should this = bounds created in new cell renderer?
            // yep, probably - this is just what I set for the sake of starting out (drew)
            this.setLocalBounds(new BoundingBox(Vector3f.ZERO, 100.0f, 100.0f, 100.0f));

            BoundingVolume[] bounds = new BoundingVolume[]{this.getLocalBounds()};
            prox.addProximityListener(this, bounds);

            localAvatarCell = (AvatarCell) getCellCache().getViewCell();

            // a context menu for editing the timeline
            menuComponent.addContextMenuFactory(new TimelineContextMenuFactory());
        } else if (status == CellStatus.DISK && !increasing) {
            prox.removeProximityListener(this);
        } else if (status == CellStatus.RENDERING && increasing) {
            // TODO matt
            // establish relation ship between segments built in rendrere and these
            this.renderer.buildSegments(config);
        }

    }

    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);

        // Unpack the TimelineConfiguration object from the client stae.
        TimelineCellClientState tccs = (TimelineCellClientState) state;

        // Do we need to do a setConfig setup with listeners here? Maybe...
        this.config = new TimelineClientConfiguration(tccs.getConfig(), getComponent(ChannelComponent.class));

        logger.info("client config: " + config.getDateRange() + "; " + config.getHeight() + "; " + config.getNumSegments());
        logger.info("client date range: " + config.getDateRange());

//        config.setDateRange(new TimelineDate(new Date(0), new Date()));
//        config.sendUpdatedConfiguration();

        if (this.sortedSegments.size() == 0) {
            this.sortedSegments = config.generateSegments();
        }
    }

    private void createCreationHUD() {
	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            creationPanel = new TimelineCreationHUDPanel(config);
            creationPanel.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent pe) {
                    if (pe.getPropertyName().equals("create")) {
                        timelineCreationHUD.setVisible(false);
                        // fetch new config information
                        // rebuildClientConfiguration();
                    } else if (pe.getPropertyName().equals("update")) {
                        // TODO: handle update case
                    } else if (pe.getPropertyName().equals("cancel")) {
                        // timeline creation was canceled
                        timelineCreationHUD.setVisible(false);
                    }
                }
            });

            timelineCreationHUD = mainHUD.createComponent(creationPanel);
            timelineCreationHUD.setPreferredLocation(Layout.CENTER);
            timelineCreationHUD.setName("Create Timeline");
	    }
	});
    }

    /**
     * rebuild configuration based on creation hud
     * this will cause the spiral to repaint
     */
    private void rebuildClientConfiguration(){
      logger.info("ADJUSTED client config's pitch:" + config.getPitch());
      logger.info("ADJUSTED client config's inner:" + config.getInnerRadius());
      logger.info("ADJUSTED client config's outer:" + config.getOuterRadius());
      logger.info("ADJUSTED client config's width:" + config.getWidth());
      this.renderer.buildSegments(config);
    }

    private void createCurationHUD() {
        curationPanel = new TimelineCurationHUDPanel(this);
        curationPanel.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pe) {
                System.out.println("Property change: " + pe.getPropertyName());

                if (pe.getPropertyName().equals("add")) {
                    logger.info("--- add curated item");
                    // TODO: add curated items to the Timeline
                    try {
                        addCuratedItem(curationPanel.getDate(),
                                       curationPanel.getText(),
                                       curationPanel.getFile());
                        
                        curationPanel.setText("");
                        curationPanel.setFile("");
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }

                } else if (pe.getPropertyName().equals("done")) {
                    // finished adding curated items
                    timelineCurationHUD.setVisible(false);
                }
            }
        });
        timelineCurationHUD = mainHUD.createComponent(curationPanel);
        timelineCurationHUD.setPreferredLocation(Layout.CENTER);
        timelineCurationHUD.setName("Curate Timeline");
    }

    private void createNavigationHUD() {
	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
        	navigationPanel = new TimelineMovementHUDPanel(TimelineCell.this);
        	navigationHUD = mainHUD.createComponent(navigationPanel);
        	navigationHUD.setPreferredLocation(Layout.EAST);
        	navigationHUD.setName("Navigation");
        	TimelineDate range = config.getDateRange();
        	navigationPanel.setStartDate(range.getMinimum());
        	navigationPanel.setEndDate(range.getMaximum());
	    }
	});
    }

    private void addCuratedItem(Date date, String text, String file) {
        if (date == null) {
            throw new IllegalStateException("No date");
        }

        if (file == null && text == null) {
            throw new IllegalStateException("No file or text");
        }

        if (file == null) {
            // use the text to create a news item
            DatedNews news = new DatedNews(new TimelineDate(date), null, null,
                                           text);
            provider.addManualObject(news);
        } else {
            ManualObjectCreator creator =
                    new ManualObjectCreator(new TimelineDate(date), file, text);
            DatedObject obj = creator.create();
            if (obj != null) {
                provider.addManualObject(obj);
            }
        }
    }
    
    public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume, int proximityIndex) {

        // If the person entering is the local avatar...
        if (cell.getCellCache().getViewCell().getCellID() == viewCellID) {
            if (entered) {
                // create HUDs
                createNavigationHUD();
                createCreationHUD();
                createCurationHUD();

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                	mainHUD.addComponent(timelineCreationHUD);
                	mainHUD.addComponent(timelineCurationHUD);
                	mainHUD.addComponent(navigationHUD);

                        navigationHUD.setVisible(true);
                    }
                });

                // Now setup a transform change listener on the avatarcell,
                // so we can properly update the navigation slider as they move
                // around.
                AvatarCell avatar = (AvatarCell) cell.getCellCache().getCell(viewCellID);
                avatar.addTransformChangeListener(this);

//                // generate a bunch of fake segments for testing purposes.
//                long msPerSegment = config.getDateRange().getRange() / config.getNumSegments();
//                long curTime = config.getDateRange().getMinimum().getTime();
//                for (int i = 0; i < config.getNumSegments(); i++) {
//                    TimelineSegment newSeg = new TimelineSegment(new TimelineDate(new Date(curTime), new Date(curTime + msPerSegment)));
//                    newSeg.setTransform(new CellTransform(new Quaternion(), new Vector3f(0.0f, (this.config.getHeight() / config.getNumSegments()) * i, 0.0f), 1.0f));
//                    this.addSegment(newSeg);
//
//                    curTime += msPerSegment;
//                }

                logger.warning("test segments: " + this.sortedSegments);

            } else {
                // Remove the HUDs
                mainHUD.removeComponent(navigationHUD);
                mainHUD.removeComponent(timelineCurationHUD);
                mainHUD.removeComponent(timelineCreationHUD);
            }
        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new TimelineCellRenderer(this);
            return this.renderer;
        } else {
            return super.createCellRenderer(rendererType);
        }
    }

    public void transformChanged(Cell cell, ChangeSource source) {
        // For now, when an avatar moves, just output our current guess
        // at the date.
//        logger.warning("date of local avatar: " + this.getDateByPosition(cell.getWorldTransform().getTranslation(null)));
//        logger.warning("local av pos: " + cell.getWorldTransform().getTranslation(null));

        TimelineDate date = this.getDateByPosition(cell.getWorldTransform().getTranslation(null));

        if (date != null) {

            TimelineSegment newSegment = getSegmentByDate(date.getMiddle());

            if (this.curSegment != null && !this.curSegment.equals(newSegment)) {
                // Trigger a segment-changed message for the benefit of the
                // audio system. If there get to be more things that need
                // this info, will need to convert this into a listener sytem.
                
                if (audio != null) {
                    audio.changeSegment(curSegment, newSegment);
                }
            }

            this.curSegment = newSegment;
        }

        if (navigationPanel != null) {
            if (date != null) {

                navigationPanel.setDateLabel(date, config.getUnits());
            }

            navigationPanel.setSliderLocation(getHeightFraction(cell.getWorldTransform().getTranslation(null)));
        }
    }

    class TimelineCellMessageReceiver implements ComponentMessageReceiver {

        public void messageReceived(CellMessage message) {
            TimelineCellChangeMessage msg = (TimelineCellChangeMessage) message;

            config = new TimelineClientConfiguration(msg.getConfig(), getComponent(ChannelComponent.class));
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

        Vector3f avPosition = localAvatarCell.getWorldTransform().getTranslation(null);

        Vector3f cellPositionAtHeight = this.getWorldTransform().getTranslation(null);

        // Set the heights the same so we can get just the distance to the center, not the
        // distance to the actual center of the cell.
        cellPositionAtHeight.y = avPosition.y;

        float radius = avPosition.distance(cellPositionAtHeight);

        // Given the fraction up the helix we want to be (position), figure out
        // what the angle (t) of that is.

        // Since the height = pitch*t, just divide it to get the number of turns,
        // and the angle is 2PI times that.
        float targetHeight = (position * config.getHeight());
        float angle = (float) ((float) (targetHeight / (float) config.getPitch()) * 2.0f * Math.PI);

        Vector3f positionRelativeToCell = new Vector3f((float) (radius * Math.sin(angle)), targetHeight, (float) (radius * Math.cos(angle)));

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
        Set<DatedObject> segments = this.sortedSegments.containsSet(new TimelineDate(date));

        // because the time ranges of segments are non-overlapping, containsSet
        // will always return a single element.
        if (segments.size() == 0) {
            return null;
        }

        assert segments.size() == 1;

        return (TimelineSegment) segments.iterator().next();
    }

    /**
     * Given a date, returns the center position for that date.
     * 
     * @param date
     * @return
     */
    public Vector3f getPositionByDate(Date date) {
        // Look up the center of the segment associated with the specified dates.
        return getSegmentByDate(date).getTransform().getTranslation(null);
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

        float heightFraction = getHeightFraction(pos);

        Date testDate = new Date(config.getDateRange().getMinimum().getTime() + ((long) (config.getDateRange().getRange() * heightFraction)));

        TimelineSegment segByPosition = getSegmentByDate(testDate);

        if (segByPosition == null) {
            return null;
        } else {
            return segByPosition.getDate();
        }
    }

    /**
     * Calculates the percentage the specified position is within the height
     * of the timeline.
     * 
     * @param pos The test position. Only the y coordinate is used. 
     * @return The percentage up the timeline that height is. The bottom is 0.0, the top is 1.0.
     */
    private float getHeightFraction(Vector3f pos) {

        float minimumHeight = 0;

        float avatarHeightRelativeToTimeline = pos.y - minimumHeight;

        float height = avatarHeightRelativeToTimeline / this.config.getHeight();

        // clamp the hight.
        if (height > 1.0f) {
            height = 1.0f;
        }
        if (height < 0.0f) {
            height = 0.0f;
        }

        return height;
    }

    public void addSegment(TimelineSegment seg) {
        System.out.println("Adding segment " + seg.getDate());
        this.sortedSegments.add(seg);

    }

    public void clearSegments() {
        this.sortedSegments.clear();
    }

    public void removeSegment(TimelineSegment seg) {
        this.sortedSegments.remove(seg);
    }

    /**
     * Context menu factory for the Timeline menu
     */
    class TimelineContextMenuFactory implements ContextMenuFactorySPI {

        public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
            editTimelineMenuItem = new SimpleContextMenuItem("Edit Timeline...", null,
                    new TimelineContextMenuListener());
            curateTimelineMenuItem = new SimpleContextMenuItem("Curate Timeline...", null,
                    new TimelineContextMenuListener());
            return new ContextMenuItem[]{
                        editTimelineMenuItem,
                        curateTimelineMenuItem};
        }
    }

    /**
     * Listener for Timeline context menu item selection
     */
    class TimelineContextMenuListener implements ContextMenuActionListener {

        public void actionPerformed(final ContextMenuItemEvent event) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (event.getContextMenuItem() == editTimelineMenuItem) {
                        if (timelineCreationHUD == null) {
                            createCreationHUD();
                        }
                        timelineCreationHUD.setVisible(true);
                    } else if (event.getContextMenuItem() == curateTimelineMenuItem) {
                        if (timelineCurationHUD == null) {
                            createCurationHUD();
                        }
                        timelineCurationHUD.setVisible(true);
                    }
                }
            });
        }
    }

    public TimelineClientConfiguration getClientConfiguration() {
        return config;
    }

}
