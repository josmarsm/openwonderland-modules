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

import com.bulletphysics.dynamics.RigidBody;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JBulletDynamicCollisionSystem;
import org.jdesktop.mtgame.JBulletPhysicsSystem;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.marbleous.client.jme.TrackRenderer;
import org.jdesktop.wonderland.modules.marbleous.client.ui.KnotFrame;
import org.jdesktop.wonderland.modules.marbleous.client.ui.TimeSliderUI;
import org.jdesktop.wonderland.modules.marbleous.client.ui.TrackListModel;
import org.jdesktop.wonderland.modules.marbleous.client.ui.UI;
import org.jdesktop.wonderland.modules.marbleous.common.Track;
import org.jdesktop.wonderland.modules.marbleous.common.TrackSegment;
import org.jdesktop.wonderland.modules.marbleous.common.cell.TrackCellClientState;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SelectedSampleMessage;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimTraceMessage;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimulationStateMessage;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimulationStateMessage.SimulationState;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.TrackCellMessage;
import org.jdesktop.wonderland.modules.marbleous.common.trace.SimTrace;

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
    private TimeSliderUI uiTimeSlider;
    Track track;
    private Object savedSegmentState;

    public TrackCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    public void editSegment(final TrackSegment segment) {
        savedSegmentState = segment.saveToMemento();
            java.awt.EventQueue.invokeLater(new Runnable() {

                public void run() {
                    new KnotFrame(segment, TrackCell.this).setVisible(true);
                }
            });
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

    public void addSegment(TrackSegment newSegment) {
        sendCellMessage(TrackCellMessage.addSegment(getCellID(), newSegment));
    }

    public void modifySegment(TrackSegment segment) {
        trackListModel.modifySegment(segment);
        //sendCellMessage(TrackCellMessage.modifySegment(getCellID(), segment));
    }

    public void removeSegment(TrackSegment selectedSegment) {
        sendCellMessage(TrackCellMessage.removeSegment(getCellID(), selectedSegment));
    }

    public void restoreSegment(TrackSegment aSegment) {
        aSegment.restoreFromMemento(savedSegmentState);
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
        track = ((TrackCellClientState)clientState).getTrack();
        System.out.println("TrackCell, track: " + track);
        trackListModel = new TrackListModel(track);
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

    public Entity getMarbleEntity () {
        return cellRenderer.getMarbleEntity();
    }

    public void addMarbleMouseListener (TrackRenderer.MarbleMouseEventListener listener) {
        cellRenderer.addMarbleMouseListener(listener);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        ChannelComponent channel = getComponent(ChannelComponent.class);

        switch (status) {
            case ACTIVE:
                if (increasing) {

                    if (ui == null) {
                        initUI();
                    }

//                    Node node = ((BasicRenderer)cellRenderer).getSceneRoot();
//                    Vector3f currentLoc = node.getLocalTranslation();
//                    Vector3f dest = new Vector3f(currentLoc);
//                    dest.y+=1;
//
//                    Timeline translation = AnimationUtils.newTranslationTimeline(node, currentLoc, dest, 5000);
//                    translation.playLoop(RepeatBehavior.LOOP);
//                    hudTest.setActive(true);
                    channel.addMessageReceiver(SimulationStateMessage.class, new SimulationStateMessageReceiver());
                    channel.addMessageReceiver(SimTraceMessage.class, new SimTraceMessageReceiver());
                    channel.addMessageReceiver(TrackCellMessage.class, new TrackCellMessageReceiver());
                    channel.addMessageReceiver(SelectedSampleMessage.class, new SelectedSampleMessageReceiver());
                    
                    ui.setVisible(true);
                }

                break;
            case INACTIVE:
                if (!increasing) {
//                    hudTest.setActive(false);
                    ui.setVisible(false);
                    ui = null;
                    channel.removeMessageReceiver(SimulationStateMessage.class);
                    channel.removeMessageReceiver(SimTraceMessage.class);
                    channel.removeMessageReceiver(TrackCellMessage.class);
                    channel.removeMessageReceiver(SelectedSampleMessage.class);
                }
                break;
            case DISK:
                // TODO cleanup
                break;
        }

    }

    private void initUI () {
        uiTimeSlider = new TimeSliderUI(this);
        System.err.println("******** uiTimeSlider = " + uiTimeSlider);

        ui = new UI(this, uiTimeSlider);
        System.err.println("******** ui = " + ui);
        
        /*
        SimTrace trace = new SimTrace(2f, 60);
        trace.appendSample(5f, new Vector3f(0f, 2f, 0f), new Vector3f(1f, 2f, 3f));
        trace.appendSample(5f, new Vector3f(0f, 3f, 0f), new Vector3f(4f, 5f, 6f));
        trace.appendSample(5f, new Vector3f(0f, 4f, 0f), new Vector3f(7f, 8f, 9f));
        */
    }

    /**
     * Convenience method to set the started/stopped state of the simulation.
     * @param state The started/stopped state of the simulation
     */
    public void setSimulationState(SimulationState simulationState) {

        logger.warning("New simulation state " + simulationState);
        if (simulationState.equals(getSimulationState())) {
            return;
        }

        setSimulationStateInternal(simulationState);
        sendCellMessage(new SimulationStateMessage(simulationState));
    }

    /**
     * Set the simulation state without sending a message to notify other cells.
     * @param simulationState The new simulation state
     */
    private void setSimulationStateInternal(SimulationState simulationState) {
        this.simulationState = simulationState;

        // Start or stop the physics system
        JBulletPhysicsSystem physicsSystem = getPhysicsSystem();
        if (physicsSystem != null) {
            if (simulationState == SimulationState.STARTED) {
                logger.warning("Starting physics system...");

                physicsSystem.setStarted(true);
            } else {
                physicsSystem.setStarted(false);
            }
        } else {
            logger.warning("Marble physics system not yet initialized!");
        }
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
     * Handles when a simulation trace is sent to this client
     */
    private class SimTraceMessageReceiver implements ComponentMessageReceiver {
        /**
         * {@inheritDoc}
         */
        @Override
        public void messageReceived(CellMessage message) {
            SimTrace simTrace = ((SimTraceMessage)message).getSimTrace();
            uiTimeSlider.setSimTrace(simTrace);
            uiTimeSlider.setVisible(true);
        }
    }

    /**
     * Processes selection changes on the slider
     */
    private class SelectedSampleMessageReceiver implements ComponentMessageReceiver {

        /**
         * {@inheritDoc}
         */
        @Override
        public void messageReceived(CellMessage message) {
            final boolean fromMe = message.getSenderID() != null && message.getSenderID().equals(getCellCache().getSession().getID());

            if (message instanceof SimulationStateMessage) {
                if (!fromMe) {
                    uiTimeSlider.setSelectedTime(((SelectedSampleMessage)message).getSelectedTime());
                }
            }
        }
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
                    // Turn on/off the simulation
                    setSimulationStateInternal(((SimulationStateMessage) message).getSimulationState());

                    // Since someone else started/stopped the simulation, we
                    // turn off buttons if running
                    boolean isEnabled = ((SimulationStateMessage)message).getSimulationState() == SimulationState.STARTED;
                    ui.externalSimulationEnabled(isEnabled);
                    
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
            TrackSegment aSegment;
            
                switch (tcm.getAction()) {
                    case ADD_SEGMENT:
                        aSegment = tcm.getTrackSegment();
                        trackListModel.addSegment(aSegment);
                        break;
                    case REMOVE_SEGMENT:
                        aSegment = tcm.getTrackSegment();
                        trackListModel.removeSegment(aSegment);
                        break;
                    default:
                        logger.severe("Unknown action type: " + tcm.getAction());

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

    /**
     * Convenience method to set the marble's rigid body to the physics
     * component
     *
     * @param rigidBody The marble's rigid body
     */
    public void setMarbleRigidBody(RigidBody rigidBody) {
        if (marblePhysicsComponent == null) {
            logger.warning("Unable to find marble physics component.");
            return;
        }
        marblePhysicsComponent.setMarbleRigidBody(rigidBody);
    }

    /**
     * Convenience method to get the marble's rigid body from the physics
     * component.
     *
     * @return The marble's rigid body
     */
    public RigidBody getMarbleRigidBody() {
        if (marblePhysicsComponent == null) {
            logger.warning("Unable to find marble physics component.");
            return null;
        }
        return marblePhysicsComponent.getMarbleRigidBody();
    }
}
