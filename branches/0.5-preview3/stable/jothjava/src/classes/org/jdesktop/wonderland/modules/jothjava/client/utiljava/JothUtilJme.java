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

package org.jdesktop.wonderland.modules.jothjava.client.utiljava;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import java.util.HashMap;
import java.util.LinkedList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import java.util.concurrent.LinkedBlockingQueue;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.EventListener;
import org.jdesktop.wonderland.client.input.EventListenerBaseImpl;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/*************************************************************************************************
 * JothUtil: Provides simplified access to Wonderland JME APIs for a single cell. Provides developers with 
 * easier access to Wonderland programming. The price, however, is that it is not as performant as
 * the basic Wonderland APIs. Note: this is only for use within the Joth module. It doesn't expose
 * the right thread model for general use.
 *
 * @author deronj@dev.java.net
 */
public class JothUtilJme {

    /** The cell. */
    private Cell cell;

    /** The cell renderer of the cell. */
    private BasicRenderer cellRenderer;

    /** The entity of the cell. */
    private Entity cellEntity;

    /** The root entity of the scene graph contained in the cell. */
    private Entity rootEntity;

    /** The root node of the JothUtil object. */
    private Node rootNode;

    /** Is the JothUtil object visible in the cell? */
    private boolean visible;

    /** The event listeners currently in use. */
    private LinkedList<EventListener> eventListeners;

    /** The collision system used for collision and picking. */
    private JMECollisionSystem colSys;

    /** The collision component of the cell. */
    private CollisionComponent cc;

    /** The number of nodes within the cell that are pickable. */
    private int numPickableNodes;

    /** Does this cell need picking? */
    private boolean entityPickable;

    /** Does this cell need collision handling? */
    private boolean entityCollidable;

    /** An JothUtil event handler. */
    private interface BaseEventHandler {
        // TODO: doc
        public void processEvent (Event event);
    }

    /** An JothUtil event handler. */
    public interface EventHandler extends BaseEventHandler{
        // TODO: doc
        public boolean consumesEvent (Event event);
    }

    /** An JothUtil event class handler. */
    public interface EventClassHandler extends BaseEventHandler{
        // TODO: doc
	public Class[] eventClassesToConsume ();
    }

    /** A mapping from JothUtil event handlers to Wonderland event listeners. */
    private HashMap<JothUtilJme.BaseEventHandler,EventListener> eventHandlerToListener =
        new HashMap<JothUtilJme.BaseEventHandler,EventListener>();

    /** The thread which calls the EventHandler processEvent methods. */
    private EventHandlerThread eventHandlerThread = new EventHandlerThread();

    /**
     * Create a new instance of EZApi.
     * @param cell The cell on which the EZApi method calls operates.
     */
    public JothUtilJme (Cell cell) {
        this.cell = cell;

        // Get cell info
        cellRenderer = ((BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME));
        cellEntity = cellRenderer.getEntity();

        // Create root entity and node and connect them
        rootEntity = new Entity("JothUtilJme Root Entity for cell " + cell.getCellID());
        rootNode = new Node("JothUtilJme Root Node for cell " + cell.getCellID());
        RenderComponent rc =
            ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
        rootEntity.addComponent(RenderComponent.class, rc);

        // Create the worker thread for event handler callbacks
        eventHandlerThread = new EventHandlerThread();
        eventHandlerThread.start();

        // TODO: arrange for cleanup to be called on cell destruction
    }

    /**
     * Clean up resources held.
     */
    public void cleanup () {
        eventHandlerThread.terminate();
        if (rootEntity != null) {
            rootEntity.removeComponent(RenderComponent.class);
            rootEntity = null;
        }
        rootNode = null;
        cellRenderer = null;
        cellEntity = null;
        cell = null;
    }

    /**
     * Returns the root node of the cell.
     */
    public Node getRootNode () {
        return rootNode;
    }

    /**
     * Attach the given spatial as a child of the given parent. Do not wait for completion.
     * @param parent The parent to which the child is attached. If this is null the
     * root node is used as the parent.
     * @param child The child which is attached to the parent.
     */
    public void attachChild (Node parent, final Spatial child) {
        if (parent == null) parent = getRootNode();

        final Node parentFinal = parent;
        doJmeOp(new Runnable() {
            public void run () {
                parentFinal.attachChild(child);
            }}, 
            parent);
    }

    /** >>>>>> continue doc here */
    public void detachChild (final Node parent, final Spatial child) {
        final Node parentFinal = parent;
        doJmeOp(new Runnable() {
            public void run () {
                parentFinal.detachChild(child);
            }}, 
            parent);
    }

    public void setLocalTranslation (final Spatial spatial, final Vector3f trans) {
        doJmeOp(new Runnable() {
            public void run () {
                spatial.setLocalTranslation(new Vector3f(trans));
            }}, 
            rootNode);
    }

    /**
     * Controls the visibility of the scene graph in the cell.
     * @param visible Whether the scene graph is visible.
     */
    public void setVisible (boolean visible) {
         if (this.visible == visible) return;
         this.visible = visible;

         if (visible) {
             // Arrange for JothUtil scene graph to be attached to the cell when the cell is visible.
             cellEntity.addEntity(rootEntity);
             RenderComponent rcCellEntity = (RenderComponent) cellEntity.getComponent(RenderComponent.class);
             RenderComponent rcRootEntity = (RenderComponent) rootEntity.getComponent(RenderComponent.class);
             if (rcCellEntity != null && rcCellEntity.getSceneRoot() != null && rcRootEntity != null) {
                 rcRootEntity.setAttachPoint(rcCellEntity.getSceneRoot());
             }
         } else {
             // Arrange for JothUtil root node to be detached from the cell when the cell is no longer visible.
             if (cellEntity != null) {
                 cellEntity.removeEntity(rootEntity);
                 RenderComponent rcRootEntity = 
                     (RenderComponent) rootEntity.getComponent(RenderComponent.class);
                 if (rcRootEntity != null) {
                     rcRootEntity.setAttachPoint(null);
                 }
             }
         }
    }

    /**
     * Specify whether the entity in this cell needs to be pickable.
     */
    private void setEntityPickable (boolean pickable) {
        if (entityPickable == pickable) return;
        entityPickable = pickable;
        updateCollisionHandling();
    }

    /**
     * Specify whether the entity in this cell needs to have collision handling.
     */
    // TODO: currently not exported through the API
    private void setEntityCollidable (boolean collidable) {
        if (entityCollidable == collidable) return;
        entityCollidable = collidable;
        updateCollisionHandling();
    }

    /**
     * Activate or deactivate collision handling for this cell.
     */
    private void updateCollisionHandling () {
        if (entityPickable || entityCollidable) {
            if (colSys == null) {
                colSys = (JMECollisionSystem) ClientContextJME.getWorldManager().getCollisionManager().
                    loadCollisionSystem(JMECollisionSystem.class);
            }
            if (cc == null) {
                cc = colSys.createCollisionComponent(rootNode);
                rootEntity.addComponent(CollisionComponent.class, cc);
            }
        } else {
            if (cc != null) {
                rootEntity.removeComponent(CollisionComponent.class);
                cc = null;
            }
            colSys = null;
        }
    }

    /**
     * Change whether a node is pickable.
     * @param node The node whose pickability is to be changed.
     * @param pickable Whether the node is to be pickable.
     */
    public void setPickable (Node node, boolean pickable) {
        boolean oldPickable = isPickable(node);
        if (oldPickable == pickable) return;
        
        if (pickable) {
            numPickableNodes++;

            // Make the entity pickable when the first pickable node appears. 
            if (numPickableNodes == 1) {
                setEntityPickable(true);
            }

            colSys.addReportingNode(node, cc);

        } else {

            colSys.removeReportingNode(node);

            numPickableNodes--;

            // Entity no longer needs to be pickable when the last pickable node goes away.
            if (numPickableNodes <= 0) {
                setEntityPickable(false);
            }
        }
    }

    /**
     * Returns whether the given node is pickable.
     */
    public boolean isPickable (Node node) {
        if (colSys == null) return false;
        // TODO: notyet return colSys.isReportingNode(node);
        return false;
    }

    /**
     * Create a material state and associate it with the given spatial.
     */
    public MaterialState spatialCreateMaterialState (final Spatial spatial) {
        MaterialState ms = (MaterialState) spatial.getRenderState(RenderState.RS_MATERIAL);
        if (ms == null) {
            ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
        }

        final MaterialState msFinal = ms;
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                spatial.setRenderState(msFinal);
            }
         }, null); 

        return ms;
    }

    public void materialStateSetAmbientAndDiffuse (final MaterialState ms, final ColorRGBA color) {
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                ms.setAmbient(new ColorRGBA(color));
                ms.setDiffuse(new ColorRGBA(color));
            }
         }, null); 
    }

    public void doJmeOp (Runnable runnable) {
        doJmeOp(runnable, rootNode);
    }

    public void doJmeOp (final Runnable runnable, Node updateNode) {
        final Node updateNodeFinal = updateNode;
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                runnable.run();
                if (updateNodeFinal != null) {
                    ClientContextJME.getWorldManager().addToUpdateList(updateNodeFinal);
                }
            }
         }, null); 
    }

    public void doJmeOpAndWait (Runnable runnable) {
        doJmeOpAndWait(runnable, rootNode);
    }

    public void doJmeOpAndWait (final Runnable runnable, Node updateNode) {
        final Node updateNodeFinal = updateNode;
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                runnable.run();
                if (updateNodeFinal != null) {
                    ClientContextJME.getWorldManager().addToUpdateList(updateNodeFinal);
                }
            }
        }, null, true); 
    }

    /**
     * Add the given event handler to the cell. This handler will respond
     * to any events which occur on the pickable nodes of the scene graph.
     * When such an event occurs, the <code>processEvent</code> method
     * will be called on a generic thread. Does nothing if the handler has already
     * been added. In <code>processEvent</code>, <code>getNode()</code> can be called 
     * to determine the node over which the event occurred.
     */
    public void addEventHandler (EventHandler eventHandler) {
        EventListener eventListener = eventHandlerToListener.get(eventHandler);
        if (eventListener != null) return;

        eventListener = new JothUtilEventListener(eventHandler);
        eventHandlerToListener.put(eventHandler, eventListener);
        eventListener.addToEntity(rootEntity);
    }

    public void addEventClassHandler (EventClassHandler eventHandler) {
        EventListener eventListener = eventHandlerToListener.get(eventHandler);
        if (eventListener != null) return;

        EventClassListener eventClassListener = new JothUtilEventClassListener(eventHandler);
        eventHandlerToListener.put(eventHandler, eventClassListener);
        eventClassListener.addToEntity(rootEntity);
    }

    public void removeEventHandler (BaseEventHandler eventHandler) {
        EventListener eventListener = eventHandlerToListener.get(eventHandler);
        if (eventListener == null) return;

        eventListener.removeFromEntity(rootEntity);
        eventHandlerToListener.remove(eventHandler);
    }

    private class JothUtilEventListener extends EventListenerBaseImpl {
        private EventHandler eventHandler;

        private JothUtilEventListener (EventHandler eventHandler) {
            this.eventHandler = eventHandler;
        }

        @Override
        public boolean consumesEvent (Event event) {
            return eventHandler.consumesEvent(event);
        }

        public void commit (final Event event) {
            eventHandlerThread.addRunnable(new Runnable () {
                public void run () {
                    eventHandler.processEvent(event);
                }
            });
        }
    }

    private class JothUtilEventClassListener extends EventClassListener {
        private EventClassHandler eventClassHandler;

        private JothUtilEventClassListener (EventClassHandler eventClassHandler) {
            this.eventClassHandler = eventClassHandler;
        }

        @Override
	public Class[] eventClassesToConsume () {
            return eventClassHandler.eventClassesToConsume();
        }

        public void commitEvent (final Event event) {
            eventHandlerThread.addRunnable(new Runnable () {
                public void run () {
                    eventClassHandler.processEvent(event);
                }
            });
        }
    }

    // A generic thread on which event handler processEvent methods are called.
    // We use the term "generic" to indicate that there is nothing special 
    // about the thread. In particular, it emphasizes that the thread is *not*
    // the MTGame Render Thread nor is it the AWT Event Dispatch Thread.
    private static class EventHandlerThread extends Thread {

        // A list of work for this thread to perform
        private LinkedBlockingQueue<Runnable> runnables =        
            new LinkedBlockingQueue<Runnable>();

        private boolean stopped;

        public void terminate () {
            stopped = true;
            interrupt();
        }

        private synchronized void addRunnable (Runnable runnable) {
            runnables.add(runnable);
        }

        @Override
        public void run () {
            while (!stopped) {

                // Block until there is work to do
                Runnable nextRunnable = null;
                try {
                     nextRunnable = runnables.take();
                } catch (InterruptedException ex) {
                }

                // If there is work to do, perform it now
                if (nextRunnable != null) {
                    nextRunnable.run();
                }
            }
        }
    }
}
