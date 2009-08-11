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
package org.jdesktop.wonderland.modules.marbleous.client.cell;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.marbleous.client.jme.TrackRenderer;
import org.jdesktop.wonderland.modules.marbleous.common.LoopTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.RightTurnTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.StraightDropTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.StraightLevelTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.Track;

/**
 * Client-side cell for rendering JME content
 */
public class TrackCell extends Cell {
    private TrackRenderer cellRenderer = null;
    private Track track;

    public TrackCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        track = new Track();

        track.addTrackSegment(new StraightDropTrackSegmentType().createSegment());
        track.addTrackSegment(new RightTurnTrackSegmentType().createSegment());
        track.addTrackSegment(new StraightLevelTrackSegmentType().createSegment());
        track.addTrackSegment(new LoopTrackSegmentType().createSegment());
        track.buildTrack();
    }

    /**
     * Get the track for this cell
     * 
     * @return
     */
    public Track getTrack() {
        return track;
    }
    
    /**
     * Called when the cell is initially created and any time there is a 
     * major configuration change. The cell will already be attached to it's parent
     * before the initial call of this method
     * 
     * @param clientState
     */
    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {


        if (rendererType == RendererType.RENDERER_JME) {
//            try {
//                URL url = AssetUtils.getAssetURL("wla://animation/AnimatedDoor.kmz/AnimatedDoor.kmz.dep");
//                DeployedModel m = LoaderManager.getLoaderManager().getLoaderFromDeployment(url);
//
//                return new ModelRenderer(this, m);
//            } catch (MalformedURLException ex) {
//                Logger.getLogger(AnimationTestRenderer.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IOException e) {
//                Logger.getLogger(AnimationTestRenderer.class.getName()).log(Level.SEVERE, null, e);
//            }
            cellRenderer = new TrackRenderer(this);

//            getComponent(AnimationComponent.class).addMouseTrigger(cellRenderer.getEntity(), "ES_Box1");

            return cellRenderer;
        }
        return super.createCellRenderer(rendererType);
    }

    @Override
    protected void setStatus(CellStatus status,boolean increasing) {
        super.setStatus(status,increasing);
        switch(status) {
            case ACTIVE :
                if (increasing) {
//                    Node node = ((BasicRenderer)cellRenderer).getSceneRoot();
//                    Vector3f currentLoc = node.getLocalTranslation();
//                    Vector3f dest = new Vector3f(currentLoc);
//                    dest.y+=1;
//
//                    Timeline translation = AnimationUtils.newTranslationTimeline(node, currentLoc, dest, 5000);
//                    translation.playLoop(RepeatBehavior.LOOP);
                    
//                    hudTest.setActive(true);
                }
                break;
            case INACTIVE :
//                if (!increasing) {
//                    hudTest.setActive(false);
//                }
            case DISK :
                // TODO cleanup
                break;
        }

    }

    /**
     * Context menu factory for the Sample menu item
     */
    class SampleContextMenuFactory implements ContextMenuFactorySPI {
        public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
            return new ContextMenuItem[] {
                        new SimpleContextMenuItem("Sample", null,
                                new SampleContextMenuListener())
            };
        }
    }

    /**
     * Listener for event when the Sample context menu item is selected
     */
    class SampleContextMenuListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            logger.warning("Sample context menu action performed!");
        }
    }


}
