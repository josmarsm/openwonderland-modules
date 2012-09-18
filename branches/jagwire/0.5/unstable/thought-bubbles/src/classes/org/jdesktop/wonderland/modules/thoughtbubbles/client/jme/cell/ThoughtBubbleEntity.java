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

import com.jme.bounding.BoundingSphere;
import com.jme.scene.Node;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;
import org.jdesktop.wonderland.modules.thoughtbubbles.client.ViewThoughtDialog;
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

    private ViewThoughtDialog viewDialog;
    private HUDComponent viewDialogHUD;


    protected static ZBufferState zbuf = null;
    static {
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        zbuf = (ZBufferState)rm.createRendererState(StateType.ZBuffer);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
    }


    public ThoughtBubbleEntity(final ThoughtRecord record, Entity parent) {
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
//        TriMesh bubble = new Sphere("thought_bubble_sphere",Vector3f.ZERO, 20, 20, 0.15f);
//        TriMesh bubble = new Box("thought_bubble_box", Vector3f.ZERO,0.5f, 40.0f, 0.5f);

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

           public void update(Object thoughtRecord) {

                ThoughtRecord rec = (ThoughtRecord)thoughtRecord;
                ThoughtBubbleTextNode textNode = new ThoughtBubbleTextNode(rec.getText());

        //        rc.getSceneRoot().attachChild(bubble);
        //        rc.getSceneRoot().setLocalTranslation(record.getX(), record.getY() + 3.0f, record.getZ());

                rootNode.attachChild(textNode);
                textNode.setLocalTranslation(rec.getX(), rec.getY() + 3.0f, rec.getZ());
                rootNode.setRenderState(zbuf);
                rootNode.setModelBound(new BoundingSphere());
                rootNode.updateModelBound();
           }
        }, record);


        makeEntityPickable(this, rootNode);

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

    public void showThoughtDialog() {

        SwingUtilities.invokeLater(new Runnable() {

        public void run() {

            if(viewDialog==null) {
            logger.warning("Making new viewthoughtdialog");
            viewDialog = new ViewThoughtDialog();
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            viewDialogHUD = mainHUD.createComponent(viewDialog);
            viewDialogHUD.setPreferredLocation(Layout.CENTER);
            viewDialogHUD.setName("View Thought");
            mainHUD.addComponent(viewDialogHUD);
            }


            
                viewDialog.setText(record.getText());
                viewDialog.setFrom(record.getFromUser());
                viewDialog.setTime(record.getTimestamp());
                logger.warning("about to set visibility");
                viewDialogHUD.setVisible(true);
                logger.warning("done setting visibility");
            }
        });
    }
}
