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

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.mtgame.JBulletDynamicCollisionSystem;
import org.jdesktop.mtgame.JBulletPhysicsSystem;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.component.CellPhysicsPropertiesComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.marbleous.client.jme.TrackRenderer;
import org.jdesktop.wonderland.modules.marbleous.client.ui.TrackListModel;
import org.jdesktop.wonderland.modules.marbleous.client.ui.UI;
import org.jdesktop.wonderland.modules.marbleous.common.RightTurnTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.StraightDropTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.StraightLevelTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.Track;
import org.jdesktop.wonderland.modules.marbleous.common.TrackSegment;
import org.jdesktop.wonderland.modules.marbleous.common.cell.TrackCellClientState;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimulationStateMessage;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimulationStateMessage.SimulationState;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.TrackCellMessage;

/**
 * Client-side cell for rendering JME content
 */
public class TrackCell extends Cell {

    @UsesCellComponent
    private MarblePhysicsComponent marblePhysicsComponent;
    private final Set<SimulationStateChangeListener> simulationStateListeners = new HashSet<SimulationStateChangeListener>();
    private SimulationState simulationState = SimulationState.STOPPED;
    private TrackRenderer cellRenderer = null;
    private TrackListModel trackListModel;
    private UI ui;

    public TrackCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        /**track = new Track();

        track.addTrackSegment(new StraightDropTrackSegmentType().createSegment());
        track.addTrackSegment(new RightTurnTrackSegmentType().createSegment());
        track.addTrackSegment(new StraightLevelTrackSegmentType().createSegment());
        track.addTrackSegment(new RightTurnTrackSegmentType().createSegment());
        track.addTrackSegment(new StraightLevelTrackSegmentType().createSegment());
//        track.addTrackSegment(new LoopTrackSegmentType().createSegment());
        track.buildTrack();
         */

        
    }

    /**
     * Get the track for this cell
     * 
     * @return
     */
    public Track getTrack() {
        return trackListModel.getTrack();
    }

    public TrackListModel getTrackListModel() {
        return trackListModel;
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
        Track track = ((TrackCellClientState)clientState).getTrack();
        System.out.println("TrackCell, track: " + track);
        trackListModel = new TrackListModel(track);
        ui = new UI(this);
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
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        ChannelComponent channel = getComponent(ChannelComponent.class);

        switch (status) {
            case ACTIVE:
                if (increasing) {

//                    Node node = ((BasicRenderer)cellRenderer).getSceneRoot();
//                    Vector3f currentLoc = node.getLocalTranslation();
//                    Vector3f dest = new Vector3f(currentLoc);
//                    dest.y+=1;
//
//                    Timeline translation = AnimationUtils.newTranslationTimeline(node, currentLoc, dest, 5000);
//                    translation.playLoop(RepeatBehavior.LOOP);
//                    hudTest.setActive(true);
                    channel.addMessageReceiver(SimulationStateMessage.class, new SimulationStateMessageReceiver());
                    channel.addMessageReceiver(TrackCellMessage.class, new TrackCellMessageReceiver());
                    
                    ui.setVisible(true);
                }

                break;
            case INACTIVE:
                if (!increasing) {
//                    hudTest.setActive(false);
                    channel.removeMessageReceiver(SimulationStateMessage.class);
                    channel.removeMessageReceiver(TrackCellMessage.class);
                }
                break;
            case DISK:
                // TODO cleanup
                break;
        }

    }

    /**
     * Context menu factory for the Sample menu item
     */
    class SampleContextMenuFactory implements ContextMenuFactorySPI {

        public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
            return new ContextMenuItem[]{
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

    /**
     * Convenience method to set the started/stopped state of the simulation.
     * @param state The started/stopped state of the simulation
     */
    public void setSimulationState(SimulationState simulationState) {

        if (simulationState.equals(getSimulationState())) {
            return;
        }

        setSimulationStateInternal(simulationState);

        JBulletPhysicsSystem physicsSystem = getPhysicsSystem();
        if (physicsSystem != null) {
            if (simulationState == SimulationState.STARTED) {
                physicsSystem.setStarted(true);
            } else {
                physicsSystem.setStarted(false);
            }
        } else {
            logger.warning("Marble physics system not yet initialized!");
        }
        sendCellMessage(new SimulationStateMessage(simulationState));
    }

    /**
     * Set the simulation state without sending a message to notify other cells.
     * @param simulationState The new simulation state
     */
    private void setSimulationStateInternal(SimulationState simulationState) {
        this.simulationState = simulationState;
        fireSimulationStateChanged(simulationState);
    }

    /**
     * Get the current start/stop state of the simulation.
     * @return Current start/stop state of the simulation
     */
    public SimulationState getSimulationState() {
        return simulationState;
    }

    /**
     * Processes state change messages received from the server and/or
     * other clients.
     */
    private class SimulationStateMessageReceiver implements ComponentMessageReceiver {

        /**
         * {@inheritDoc}
         */
        @Override
        public void messageReceived(CellMessage message) {
            final boolean fromMe = message.getSenderID() != null && message.getSenderID().equals(getCellCache().getSession().getID());

            if (message instanceof SimulationStateMessage) {
                if (!fromMe) {
                    setSimulationStateInternal(((SimulationStateMessage) message).getSimulationState());
                }
            }
        }
    }

    /**
     * Processes cell messages received from the server and/or
     * other clients.
     */
    private class TrackCellMessageReceiver implements ComponentMessageReceiver {

        /**
         * {@inheritDoc}
         */
        @Override
        public void messageReceived(CellMessage message) {
            System.out.println("TrackCellMessageReceiver, received message: " + message);
            TrackCellMessage tcm = (TrackCellMessage) message;
            BigInteger senderID = tcm.getSenderID();
            if (senderID == null) {
                //Broadcast from server
                senderID = BigInteger.ZERO;
            }
            TrackSegment aSegment;
            if (!senderID.equals(getCellCache().getSession().getID())) {
                switch (tcm.getAction()) {
                    case ADD_SEGMENT:
                        aSegment = tcm.getTrackSegment();
                        trackListModel.addSegment(aSegment);
                        break;
                    case REMOVE_SEGMENT:
                        aSegment = tcm.getTrackSegment();
                        trackListModel.addSegment(aSegment);
                        break;
                    default:
                        logger.severe("Unknown action type: " + tcm.getAction());

                }


            }
        }
    }

    /**
     * Add a listener for simulation state changes.
     * @param listener The listener to add
     */
    public void addSimulationStateChangeListener(SimulationStateChangeListener listener) {
        synchronized (simulationStateListeners) {
            simulationStateListeners.add(listener);
        }
    }

    /**
     * Remove a simulation state change listener.
     * @param listener The listener to remove
     */
    public void removeSimulationStateChangeListener(SimulationStateChangeListener listener) {
        synchronized (simulationStateListeners) {
            simulationStateListeners.add(listener);
        }
    }

    /**
     * Notify listeners that the simulation state has changed.
     * @param newState The new simulation state
     */
    private void fireSimulationStateChanged(SimulationState newState) {
        synchronized (simulationStateListeners) {
            for (SimulationStateChangeListener listener : simulationStateListeners) {
                listener.simulationStateChanged(newState);
            }
        }
    }

    /**
     * Interface to listen for changes to the start/stop state of the simulation.
     */
    public interface SimulationStateChangeListener {

        /**
         * Called when the SimulationState for the cell is changed.
         * @param simulationState The new SimulationState
         */
        public void simulationStateChanged(SimulationState simulationState);
    }

    /**
     * Convenience method to get the physics system from the attached physics
     * component.
     * @return Physics system for this cell
     */
    public JBulletPhysicsSystem getPhysicsSystem() {
        if (marblePhysicsComponent == null) {
            return null;
        }
        return marblePhysicsComponent.getPhysicsSystem();
    }

    /**
     * Convenience method to get the collision system from the attached physics
     * component.
     * @return Collision system for this cell
     */
    public JBulletDynamicCollisionSystem getCollisionSystem() {
        if (marblePhysicsComponent == null) {
            return null;
        }
        return marblePhysicsComponent.getCollisionSystem();
    }
}
