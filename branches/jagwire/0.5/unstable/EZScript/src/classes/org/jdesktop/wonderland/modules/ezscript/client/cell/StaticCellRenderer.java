/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.cell;

import com.jme.scene.Node;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.CollisionSystem;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * @author Ryan
 */
public class StaticCellRenderer extends BasicRenderer {

    private Node scenegraph = null;

    public StaticCellRenderer(Cell cell, Node scenegraph) {
        super(cell);

        this.scenegraph = scenegraph;
    }

    public StaticCellRenderer(Cell cell) {
        super(cell);
        scenegraph = new Node();
    }

    public Node getScenegraph() {
        return scenegraph;
    }

    
    public CollisionSystem collisionSystem() {
        ServerSessionManager manager = LoginManager.getPrimary();
        return ClientContextJME.getCollisionSystem(manager, "Default");
    }
    public void setScenegraph(final Node freshScenegraph) {

        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                scenegraph.detachAllChildren();
//
//                
//                Entity e = new Entity();
//                
//                
//                
//                JMECollisionSystem cSystem = (JMECollisionSystem)collisionSystem();
//                CollisionComponent cc = cSystem.createCollisionComponent(freshScenegraph);
//                
//                cc.setCollidable(true);
//                cc.setPickable(true);
//                cSystem.addCollisionComponent(cc);
//                entity.addComponent(CollisionComponent.class, cc);
//                WorldManager.getDefaultWorldManager().addEntity(e);
                
                scenegraph.attachChild(freshScenegraph);
                
               
                
                WorldManager.getDefaultWorldManager().addToUpdateList(freshScenegraph);
                WorldManager.getDefaultWorldManager().addToUpdateList(scenegraph);
            }
        });

    }

    @Override
    protected Node createSceneGraph(Entity entity) {
        return scenegraph;
    }
}
