/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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
package org.jdesktop.wonderland.modules.ezscript.client.bulletphysics;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;

/**
 * A visual Entity that displays the bounds of the scene graph root as either
 * a sphere or box  unrelated to any cell.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author JagWire
 */
public class IndependentBoundsViewerEntity extends Entity {

    // The root node of the bounds viewer scene graph
    private Node rootNode = null;

    // True if the bounds viewer Entity is visible, false if not
    private boolean isVisible = false;

    /**
     * Constructor that takes the root Node of the Cell's scene graph
     */
    public IndependentBoundsViewerEntity() {
        super("Bounds Viewer");
    }

    public void showBounds(final BoundingVolume bounds) {
	if (rootNode != null) {
	    dispose();
	}

        rootNode = new Node("Bounds Viewer Node");
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        RenderComponent rc = rm.createRenderComponent(rootNode);
        this.addComponent(RenderComponent.class, rc);
        
        // Set the Z-buffer state on the root node
        ZBufferState zbuf = (ZBufferState)rm.createRendererState(StateType.ZBuffer);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
        rootNode.setRenderState(zbuf);

        // Set the wireframe state on the root node
//        WireframeState wf = (WireframeState)rm.createRendererState(StateType.Wireframe);
//        wf.setEnabled(true);
//        rootNode.setRenderState(wf);
        MaterialState ms = (MaterialState)rm.createRendererState(StateType.Material);
        ms.setAmbient(new ColorRGBA(0.25f, 0, 0.5f, 0.40f));
        ms.setDiffuse(new ColorRGBA(0.25f, 0, 0.5f, 0.40f));
        ms.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);
        //ms.setSpecular(new ColorRGBA(1f, 1, 1f, 1f));
        
        ms.setEnabled(true);
        rootNode.setRenderState(ms);
        
        BlendState bs = (BlendState)rm.createRendererState(StateType.Blend);
        bs.setEnabled(true);
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        bs.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        bs.setTestEnabled(true);
        bs.setTestFunction(BlendState.TestFunction.GreaterThan);
        rootNode.setRenderState(bs);

        
        // Draw some geometry that mimics the bounds, either a sphere or a
        // box. Add to the scene graph of this Entity.
        if (bounds instanceof BoundingSphere) {
            float radius = ((BoundingSphere) bounds).radius;
            Vector3f center = ((BoundingSphere) bounds).getCenter();
            Sphere sphere = new Sphere("Sphere", center, 30, 30, radius);

            rootNode.attachChild(sphere);
            //rootNode.getChild("Sphere").setLocalTranslation(center);
        } else if (bounds instanceof BoundingBox) {
            float xExtent = ((BoundingBox)bounds).xExtent;
            float yExtent = ((BoundingBox)bounds).yExtent;
            float zExtent = ((BoundingBox)bounds).zExtent;
            Vector3f origin = ((BoundingBox)bounds).getCenter();
            Box box = new Box("Box", origin, xExtent, yExtent, zExtent);
            
            rootNode.attachChild(box);
           // rootNode.getChild("Box").setLocalTranslation(origin);
            
        }	

        rootNode.setLocalTranslation(new Vector3f());
	rootNode.setLocalRotation(new Quaternion());
        // OWL issue #61: make sure to take scale into account
        rootNode.setLocalScale(1);
        
	setVisible(true);
    }

    /**
     * Returns the root Node for the bounds viewer Entity.
     *
     * @param The Entity root Node
     */
    public Node getRootNode() {
        return rootNode;
    }

    /**
     * Sets whether the bounds viewer is visible (true) or invisible (false).
     *
     * @param visible True to make the affordance visible, false to not
     */
    public synchronized void setVisible(boolean visible) {
        WorldManager wm = ClientContextJME.getWorldManager();

        // If we want to make the affordance visible and it already is not
        // visible, then make it visible. We do not need to put add/remove
        // Entities on the MT Game render thread, they are already thread safe.
        if (visible == true && isVisible == false) {
            wm.addEntity(this);
            isVisible = true;
            return;
        }

        // If we want to make the affordance invisible and it already is
        // visible, then make it invisible
        if (visible == false && isVisible == true) {
            wm.removeEntity(this);
            isVisible = false;
            return;
        }
    }

    public void dispose() {
        // First, to make sure the affordance is no longer visible
        setVisible(false);

        // Clean up all of the listeners so this class gets properly garbage
        // collected.
	rootNode = null;
    }
    
    public synchronized void updateTransform(final Vector3f position, final Quaternion orientation) {
        SceneWorker.addWorker(new WorkCommit() { 
            public void commit() {
                rootNode.setLocalTranslation(position);
                rootNode.setLocalRotation(orientation);
                
                WorldManager.getDefaultWorldManager().addToUpdateList(rootNode);
            }
        });
    }
    
    public synchronized void updateBounds(final BoundingVolume bounds) {
        SceneWorker.addWorker(new WorkCommit() {

            public void commit() {
                rootNode.detachAllChildren();
                // Draw some geometry that mimics the bounds, either a sphere or a
                // box. Add to the scene graph of this Entity.
                Vector3f current = rootNode.getLocalTranslation();
                if (bounds instanceof BoundingSphere) {
                    float radius = ((BoundingSphere) bounds).radius;
                    Vector3f center = ((BoundingSphere) bounds).getCenter();
                    Sphere sphere = new Sphere("Sphere", center, 30, 30, radius);

                    rootNode.attachChild(sphere);
                    current.y = radius;
                    //rootNode.getChild("Sphere").setLocalTranslation(center);
                } else if (bounds instanceof BoundingBox) {
                    float xExtent = ((BoundingBox) bounds).xExtent;
                    float yExtent = ((BoundingBox) bounds).yExtent;
                    float zExtent = ((BoundingBox) bounds).zExtent;
                    Vector3f origin = ((BoundingBox) bounds).getCenter();
                    Box box = new Box("Box", origin, xExtent, yExtent, zExtent);
                    current.y = yExtent;
                    rootNode.attachChild(box);
                    // rootNode.getChild("Box").setLocalTranslation(origin);

                }                
                 
                rootNode.setLocalTranslation(current);
                //rootNode.setLocalRotation(new Quaternion());
                // OWL issue #61: make sure to take scale into account
                rootNode.setLocalScale(1);
                WorldManager.getDefaultWorldManager().addToUpdateList(rootNode);
            }
        });
    }

}
