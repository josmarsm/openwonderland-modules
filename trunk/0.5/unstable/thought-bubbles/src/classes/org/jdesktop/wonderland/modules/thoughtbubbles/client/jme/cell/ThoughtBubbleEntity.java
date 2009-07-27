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

package org.jdesktop.wonderland.modules.thoughtbubbles.client.jme.cell;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.thoughtbubbles.common.ThoughtRecord;

/**
 * Node that handles the rendering of a ThoughtRecord. 
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class ThoughtBubbleEntity extends Entity {

    private static final Logger logger =
        Logger.getLogger(ThoughtBubbleEntity.class.getName());


    private ThoughtRecord record;

    private Node rootNode;

    protected static ZBufferState zbuf = null;
    static {
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        zbuf = (ZBufferState)rm.createRendererState(StateType.ZBuffer);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
    }


    public ThoughtBubbleEntity(ThoughtRecord record, Entity parent) {
        super("thought_bubble_entity");
        this.record = record;

        // Create the root node of the cell and the render component to attach
        // to the Entity with the node
        rootNode = new Node();
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        RenderComponent rc = rm.createRenderComponent(rootNode);
        this.addComponent(RenderComponent.class, rc);


        rc.setAttachPoint(parent.getComponent(RenderComponent.class).getSceneRoot());
        // Now setup our rendering here.
//        TriMesh bubble = new Sphere("thought_bubble_sphere",Vector3f.ZERO, 10, 10, 0.5f);
        TriMesh bubble = new Box("thought_bubble_box", Vector3f.ZERO,0.5f, 40.0f, 0.5f);

        rc.getSceneRoot().attachChild(bubble);
        rc.getSceneRoot().setLocalTranslation(record.getX(), record.getY() + 10.0f, record.getZ());

        rootNode.setRenderState(zbuf);

        makeEntityPickable(parent, parent.getComponent(RenderComponent.class).getSceneRoot());

    }

        /**
     * Make this entity pickable by adding a collision component to it.
     */
    protected void makeEntityPickable(Entity entity, Node node) {
        JMECollisionSystem collisionSystem = (JMECollisionSystem)
                ClientContextJME.getWorldManager().getCollisionManager().
                loadCollisionSystem(JMECollisionSystem.class);

        CollisionComponent cc = collisionSystem.createCollisionComponent(node);
        entity.addComponent(CollisionComponent.class, cc);
    }


}
