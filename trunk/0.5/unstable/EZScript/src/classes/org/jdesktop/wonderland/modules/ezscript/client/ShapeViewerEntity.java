/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
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
 *
 * @author Jagwire
 */
public class ShapeViewerEntity extends Entity {
    
    private Node rootNode = null;
    private boolean isVisible = false;
    private String shapeType = "";
    
    public ShapeViewerEntity() {
        super("Shape Viewer");
    }
    
    public ShapeViewerEntity(String type) {
        super("Shape Viewer");
        this.shapeType = type;
    }
    
    public void showShape() {
        if(shapeType == null || shapeType.equals(""))
            return;
        
        showShape(shapeType);
    }
    
    
    
    
    public void showShape(String shapeType) {
        if(rootNode != null) {
            dispose();
        }
        
        rootNode = new Node("Shape Viewer Node");
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        RenderComponent rc = rm.createRenderComponent(rootNode);
        this.addComponent(RenderComponent.class, rc);
        
        //Set the z-buffer state on the root node
        ZBufferState zState = (ZBufferState)rm.createRendererState(StateType.ZBuffer);
        zState.setEnabled(true);
        zState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        rootNode.setRenderState(zState);
        
        MaterialState ms = (MaterialState)rm.createRendererState(StateType.Material);
        ms.setAmbient(new ColorRGBA(0.25f, 0, 0.5f, 0.40f));
        ms.setDiffuse(new ColorRGBA(0.25f, 0, 0.5f, 0.40f));
        ms.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);
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

        rootNode.attachChild(ShapeUtils.INSTANCE.buildShape(shapeType));
        
        rootNode.setLocalTranslation(new Vector3f());
        rootNode.setLocalRotation(new Quaternion());
        rootNode.setLocalScale(1);
        
        setVisible(true);
    }
    
    public Node getRootNode() {
        return rootNode;
    }
    
    public synchronized void setVisible(boolean visible) {
        WorldManager manager = ClientContextJME.getWorldManager();
        
        if(visible == true && isVisible == false) {
            manager.addEntity(this);
            isVisible = true;
            return;
        }
        
        if(visible == false && isVisible == true) {
            manager.removeEntity(this);
            isVisible = false;
            return;
        }
    }
    
    public void dispose() {
            setVisible(false);
            
            rootNode = null;
    }
    
    public synchronized void updateTransform(final Vector3f position,
                                             final Quaternion orientation) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                rootNode.setLocalTranslation(position);
                rootNode.setLocalRotation(orientation);
                
                WorldManager.getDefaultWorldManager().addToUpdateList(rootNode);
            }
        });
    }
    
    public synchronized void updateShape(String shapeType) {
        SceneWorker.addWorker(new WorkCommit() { 
            public void commit() {
                rootNode.detachAllChildren();
                Vector3f current = rootNode.getLocalTranslation();
                //TODO: create trimesh
                
                rootNode.setLocalTranslation(current);
                rootNode.setLocalScale(1);
                WorldManager.getDefaultWorldManager().addToUpdateList(rootNode);
                
            }
        });
    }
}
    

