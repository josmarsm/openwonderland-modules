/*
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

package org.jdesktop.wonderland.modules.marbleous.client.ui;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;

import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;

import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.modules.marbleous.client.SampleInfo;

/**
 * An Entity which displays the physics simulationt trace sample data for a particular time.
 * @author mabonner, deronj
 */
public class SampleDisplayEntity extends Entity {

    private static Logger logger = Logger.getLogger(SampleDisplayEntity.class.getName());

    /** The sample info to be displayed. */
    private SampleInfo sampleInfo;

    /** sets font size */
    private float fontSizeModifier;

    private boolean visible;

    /** If entity is pinned, the entity should stay visible. */
    private boolean pinned;

    private boolean current;

    // TODO: true is temp
    private boolean large = true;

    /** 
     * Determines how much information from the sample is displayed.
     */
    public enum DisplayMode { HIDDEN, BASIC, FULL };

    private DisplayMode displayMode;

    // The display node
    private Node node;

    /** Whether this entity has been added to the JME manager */
    private boolean entityAdded = false;

    // mouse listeners on this annotation
    private Set<MouseListener> listeners = new HashSet<MouseListener>();

    protected static ZBufferState zbuf = null;

    private Vector3f localTranslation = new Vector3f();

    static {
      RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
      zbuf = (ZBufferState)rm.createRendererState(StateType.ZBuffer);
      zbuf.setEnabled(true);
      zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
    }

    /**
     * Creates a sample info display entity (initially invisible)
     * @param a the annotation to represent
     * @param mode initial DisplayMode
     * @param fontSize initial fontSizeModifier
     */
    // TODO: parentEntity is not yet used. Entity is attached to world, not the cell entity. This is wrong.
    public SampleDisplayEntity(Entity parentEntity, SampleInfo sampleInfo, float fontSizeModifier) {
        super("SampleDisplayEntity for Time " + sampleInfo.getTime());

        this.sampleInfo = sampleInfo;
        this.fontSizeModifier = fontSizeModifier;

        attachMouseListener(new MouseListener(this));

        setDisplayMode(DisplayMode.HIDDEN);
    }

    public void setLocalTranslation (Vector3f trans) {
        localTranslation = trans;
        if (displayMode != DisplayMode.HIDDEN) {
            node.setLocalTranslation(trans);
        }
    }

    /**
     * Returns the node used to display this annotation
     *
     */
    public Node getNode() {
        return node;
    }

    /**
     * Attach a mouse listener to this entity.
     * @param ml the listener to attach
     */
    private void attachMouseListener(MouseListener ml) {
        ml.addToEntity(this);
        listeners.add(ml);
    }

    /** A basic listener for 3D mouse events */
    protected class MouseListener extends EventClassListener {
        private Entity ent;
        // dragging variables

        public MouseListener(Entity e){
            ent = e;
        }
        /**
         * {@inheritDoc}
         */
        @Override
            public Class[] eventClassesToConsume() {
            return new Class[]{MouseEvent3D.class};
        }

        /**
         * {@inheritDoc}
         */
        @Override
            public void commitEvent(final Event event) {
            // collect events
            MouseEvent3D me3d = (MouseEvent3D) event;
            MouseEvent me = (MouseEvent) me3d.getAwtEvent();
            logger.info("got mouse event!");

            // click events - display context menu, cycle display mode
            if(me.getID() == MouseEvent.MOUSE_CLICKED){
                if(me.getButton() == MouseEvent.BUTTON1){
                    setPinned(! pinned);
                }
            }
        }
    }

    public void setVisible (boolean visible) {
        if (this.visible == visible) return;
        this.visible = visible;

        //float t = sampleInfo.getTime();
        //System.err.println("********** setVisible: t = " + t + ", visible = " + visible);

        evaluateVisibilityAndSize();
    }

    public boolean getVisible () {
        return visible;
    }

    public void setPinned (boolean pinned) {
        if (this.pinned == pinned) return;
        this.pinned = pinned;

        float t = sampleInfo.getTime();
        System.err.println("********** setPinned: t = " + t + ", pinned = " + pinned);

        evaluateVisibilityAndSize();
    }

    public void setCurrent (boolean current) {
        if (this.current == current) return;
        this.current = current;

        //float t = sampleInfo.getTime();
        //System.err.println("********** setCurrent: t = " + t + ", current = " + current);

        evaluateVisibilityAndSize();
    }


    public void setLarge (boolean large) {
        if (this.large == large) return;
        this.large = large;

        float t = sampleInfo.getTime();
        System.err.println("********** setLarge: t = " + t + ", large = " + large);

        evaluateVisibilityAndSize();
    }

    private void evaluateVisibilityAndSize () {
        if (visible && (current || pinned)) {
            if (large) {
                setDisplayMode(DisplayMode.FULL);
            } else {
                // TODO
            }
        } else {
            setDisplayMode(DisplayMode.HIDDEN);
        }
    }

    /**
     * Sets how the annotation is displayed. To hide the annotation, set this to
     * DisplayMode.HIDDEN
     *
     * @param newDisplayMode what style to display the node as
     */
    private synchronized void setDisplayMode(DisplayMode newDisplayMode) {
        if (displayMode == newDisplayMode) return;

        logger.info(" setting display mode: " + newDisplayMode);
        logger.info(" current display mode: " + displayMode);

        // if node is currently HIDDEN and the new mode is not HIDDEN (is visible),
        //     make the node visible in the world
        if (newDisplayMode != DisplayMode.HIDDEN) {
            logger.info(" will display");
            displayMode = newDisplayMode;
            logger.info(" display mode is now:" + displayMode);

            // refreshes the node to the new DisplayMode's version
            node = new SampleDisplayNode(sampleInfo, displayMode, fontSizeModifier);
            // this is unnecessary but it can't hurt, it guarantees we are operating
            // on the nodes we think we are in the updater thread
            final Node newNode = node;

            RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
            RenderComponent rComp = rm.createRenderComponent(node);
            this.removeComponent(RenderComponent.class);
            this.addComponent(RenderComponent.class, rComp);

            node.setRenderState(zbuf);
            node.setModelBound(new BoundingBox());
            node.updateModelBound();

            node.setLocalTranslation(localTranslation);

            makeEntityPickable(this, node);

            RenderUpdater updater = new RenderUpdater() {
                    public void update(Object arg0) {
                        SampleDisplayEntity ent = (SampleDisplayEntity)arg0;
                        ClientContextJME.getWorldManager().removeEntity(ent);
                        entityAdded = false;
                        logger.info(" adding entity");
                        ClientContextJME.getWorldManager().addEntity(ent);
                        entityAdded = true;
                        ClientContextJME.getWorldManager().addToUpdateList(newNode);
                    }
                };

            WorldManager wm = ClientContextJME.getWorldManager();
            wm.addRenderUpdater(updater, this);

            return;
        }
        // If we want to make the affordance invisible and it already is
        // visible, then make it invisible
        if (newDisplayMode == displayMode.HIDDEN && displayMode != displayMode.HIDDEN) {
            logger.info(" will be hidden");
            RenderUpdater updater = new RenderUpdater() {
                    public void update(Object arg0) {
                        SampleDisplayEntity ent = (SampleDisplayEntity)arg0;
                        ClientContextJME.getWorldManager().removeEntity(ent);
                        entityAdded = false;
                        //                    logger.info("making non-pickable");
                        //                    ent.removeComponent(CollisionComponent.class);
                    }
                };
            WorldManager wm = ClientContextJME.getWorldManager();
            wm.addRenderUpdater(updater, this);
            displayMode = displayMode.HIDDEN;
        }
    }

    /**
     * Adjust the font size
     *
     * @param newDisplayMode what style to display the node as
     */
    public synchronized void setFontSizeModifier(float newMod) {

        logger.info(" setting new font size mod: " + newMod);
        logger.info(" current display mode: " + fontSizeModifier);
        fontSizeModifier = newMod;
        revalidateNode();
    }

    /**
     * helper function, refreshes this Entity's Node. This will cause any changes
     * like a new display mode or a new fontSizeModifier to take effect. Also used
     * to re-calculate bounds, e.g. after node is moved.
     */
    private void revalidateNode(){
        node = new SampleDisplayNode(sampleInfo, displayMode, fontSizeModifier);
        logger.info(" revalidate node");
        // this is unnecessary but it can't hurt, it guarantees we are operating
        // on the nodes we think we are in the updater thread
        final Node newNode = node;

        RenderUpdater updater = new RenderUpdater() {
                public void update(Object arg0) {
                    SampleDisplayEntity ent = (SampleDisplayEntity)arg0;

                    logger.info(" get render manager");
                    RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
                    logger.info(" got manager");
                    RenderComponent rComp = rm.createRenderComponent(newNode);
                    logger.info(" made render compo");
                    ent.removeComponent(RenderComponent.class);
                    logger.info(" removed render compo");
                    ent.addComponent(RenderComponent.class, rComp);
                    logger.info(" swapped render compo");

                    node.setRenderState(zbuf);
                    node.setModelBound(new BoundingBox());
                    node.updateModelBound();

                    makeEntityPickable(SampleDisplayEntity.this, newNode);

                    //        ClientContextJME.getWorldManager().removeEntity(ent);
                    //        entityAdded = false;

                    logger.info(" adding entity");
                    //        ClientContextJME.getWorldManager().addEntity(ent);
                    entityAdded = true;
                    ClientContextJME.getWorldManager().addToUpdateList(newNode);
                }
            };



        WorldManager wm = ClientContextJME.getWorldManager();
        logger.info(" add render updater");
        wm.addRenderUpdater(updater, this);
        logger.info(" finished");
    }

    /**
     * Check the current display mode of this annotation entity
     */
    public DisplayMode getDisplayMode(){
        return displayMode;
    }

    public boolean isVisible () {
        return displayMode != SampleDisplayEntity.DisplayMode.HIDDEN;
    }

    /**
     * Make this entity pickable by adding a collision component to it.
     */
    protected void makeEntityPickable(Entity entity, Node node) {
        JMECollisionSystem collisionSystem = (JMECollisionSystem)
            ClientContextJME.getWorldManager().getCollisionManager().
            loadCollisionSystem(JMECollisionSystem.class);
        entity.removeComponent(CollisionComponent.class);
        CollisionComponent cc = collisionSystem.createCollisionComponent(node);
        entity.addComponent(CollisionComponent.class, cc);
    }

    /**
     * Clean up listeners etc so this annotation can be properly garbage
     * collected.
     */
    public void dispose() {
        setDisplayMode(DisplayMode.HIDDEN);
        for(MouseListener ml : listeners){
            ml.removeFromEntity(this);
            ml = null;
            listeners.clear();
        }
        RenderUpdater updater = new RenderUpdater() {
                public void update(Object arg0) {
                    SampleDisplayEntity ent = (SampleDisplayEntity)arg0;
                    ClientContextJME.getWorldManager().removeEntity(ent);
                }
            };
    }
}
